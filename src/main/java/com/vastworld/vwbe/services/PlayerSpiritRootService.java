package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerspiritroot.PlayerSpiritRootDTO;
import com.vastworld.vwbe.dto.playerspiritroot.RollSpiritRootDTO;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerSpiritRoot;
import com.vastworld.vwbe.entites.SpiritRoot;
import com.vastworld.vwbe.repositories.PlayerRepository;
import com.vastworld.vwbe.repositories.PlayerSpiritRootRepository;
import com.vastworld.vwbe.repositories.SpiritRootRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class PlayerSpiritRootService {
    private final PlayerSpiritRootRepository playerSpiritRootRepository;
    private final PlayerRepository playerRepository;
    private final SpiritRootRepository spiritRootRepository;
    private final Random random = new Random();
    private final RedisService redisService;

    public PlayerSpiritRootService(PlayerSpiritRootRepository playerSpiritRootRepository, PlayerRepository playerRepository, SpiritRootRepository spiritRootRepository, RedisService redisService) {
        this.playerSpiritRootRepository = playerSpiritRootRepository;
        this.playerRepository = playerRepository;
        this.spiritRootRepository = spiritRootRepository;
        this.redisService = redisService;
    }

    public ServiceResult<List<PlayerSpiritRootDTO>> getAllPlayerSpiritRoots() {
        try {
            var playerSpiritRootList = playerSpiritRootRepository.findAll();
            if (playerSpiritRootList.isEmpty()) {
                return ServiceResult.failure("No player spirit root found");
            }

            var dtoList = playerSpiritRootList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player spirit root retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player spirit root", ex);
        }
    }

    public ServiceResult<List<String>> getPlayerSpiritRootById(UUID playerId) {
        try {
            if (playerId == null) {
                return ServiceResult.failure("Player spirit root id is invalid");
            }

            var cacheKey = CacheKeys.playerSpiritRoots(playerId);
//            var cached = redisService.get(cacheKey, TypeReference<>)

            var playerSpiritRoots = playerSpiritRootRepository.findByPlayer_Id(playerId);
            if (playerSpiritRoots.isEmpty()) {
                return ServiceResult.failure("Player spirit root not found");
            }

            var data = playerSpiritRoots.stream()
                    .map(spiritRoot -> spiritRoot.getSpiritRoot().getName())
                    .toList();
            redisService.set(cacheKey, data);
            return ServiceResult.success("Player spirit root retrieved successfully", data);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player spirit root", ex);
        }
    }

    public ServiceResult<List<PlayerSpiritRootDTO>> getPlayerSpiritRootEntityById(UUID playerId){
        try {
            if (playerId == null) {
                return ServiceResult.failure("Player spirit root id is invalid");
            }

            var cacheKey = CacheKeys.playerSpiritRoots(playerId);
//            var cached = redisService.get(cacheKey, TypeReference<>)

            var playerSpiritRoots = playerSpiritRootRepository.findByPlayer_Id(playerId);
            if (playerSpiritRoots.isEmpty()) {
                return ServiceResult.failure("Player spirit root not found");
            }

            var data = playerSpiritRoots.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player spirit root retrieved successfully", data);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player spirit root", ex);
        }
    }

    public ServiceResult<RollSpiritRootDTO> rollSpiritRoot(UUID playerID){
        try {
            if (playerID == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            var player = playerRepository.findById(playerID);
            if (player.isEmpty()) {
                return ServiceResult.failure("Player not exist");
            }

            var rollingPlayer = player.get();
            if (rollingPlayer.getRollNum() == null || rollingPlayer.getRollNum() <= 0) {
                return ServiceResult.failure("Player has no spirit root rolls remaining");
            }

            var spiritRoots = spiritRootRepository.findAll();

            var normalSpiritRoots = spiritRoots.stream()
                    .filter(spiritRoot -> Boolean.FALSE.equals(spiritRoot.getIsVariant()))
                    .toList();

            var variantSpiritRoots = spiritRoots.stream()
                    .filter(spiritRoot -> Boolean.TRUE.equals(spiritRoot.getIsVariant()))
                    .toList();

            if (normalSpiritRoots.isEmpty() && variantSpiritRoots.isEmpty()) {
                return ServiceResult.failure("No spirit root found");
            }

            int normalRootCount = random.nextInt(5) + 1;

            boolean variantRoll = normalRootCount == 2
                    && !variantSpiritRoots.isEmpty()
                    && random.nextBoolean();

            List<SpiritRoot> rolledSpiritRoots;
            if (variantRoll) {
                rolledSpiritRoots = List.of(randomFrom(variantSpiritRoots));
            } else {
                if (normalSpiritRoots.size() < normalRootCount) {
                    return ServiceResult.failure("Not enough normal spirit roots to roll");
                }

                rolledSpiritRoots = randomDistinct(normalSpiritRoots, normalRootCount);
            }

            playerSpiritRootRepository.deleteByPlayer_Id(rollingPlayer.getId());

            var savedSpiritRoots = rolledSpiritRoots.stream()
                    .map(spiritRoot -> {
                        var playerSpiritRoot = new PlayerSpiritRoot();
                        playerSpiritRoot.setPlayer(rollingPlayer);
                        playerSpiritRoot.setSpiritRoot(spiritRoot);
                        return playerSpiritRootRepository.save(playerSpiritRoot);
                    })
                    .map(this::toDto)
                    .toList();

            rollingPlayer.setRollNum(rollingPlayer.getRollNum() - 1);
            playerRepository.save(rollingPlayer);

            var result = new RollSpiritRootDTO(
                    rollingPlayer.getId(),
                    rollingPlayer.getRollNum(),
                    variantRoll,
                    savedSpiritRoots
            );

            return ServiceResult.success("Spirit root rolled successfully", result);
        }
        catch (Exception ex) {
            return ServiceResult.failure("Error rolling spirit root", ex);
        }
    }

    public ServiceResult<PlayerSpiritRootDTO> createPlayerSpiritRoot(PlayerSpiritRootDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (playerSpiritRootRepository.existsByPlayer_IdAndSpiritRoot_Id(dto.playerId(), dto.spiritRootId())) {
                return ServiceResult.failure("Player spirit root already exists");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            PlayerSpiritRoot playerSpiritRoot = new PlayerSpiritRoot();
            playerSpiritRoot.setPlayer(referencesResult.getData().player());
            playerSpiritRoot.setSpiritRoot(referencesResult.getData().spiritRoot());

            var savedPlayerSpiritRoot = playerSpiritRootRepository.save(playerSpiritRoot);
            return ServiceResult.success("Player spirit root created successfully", toDto(savedPlayerSpiritRoot));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating player spirit root", ex);
        }
    }

    public ServiceResult<PlayerSpiritRootDTO> updatePlayerSpiritRoot(Integer id, PlayerSpiritRootDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player spirit root id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var playerSpiritRootOptional = playerSpiritRootRepository.findById(id);
            if (playerSpiritRootOptional.isEmpty()) {
                return ServiceResult.failure("Player spirit root not found");
            }

            if (playerSpiritRootRepository.existsByPlayer_IdAndSpiritRoot_IdAndIdNot(dto.playerId(), dto.spiritRootId(), id)) {
                return ServiceResult.failure("Player spirit root already exists");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            var playerSpiritRoot = playerSpiritRootOptional.get();
            playerSpiritRoot.setPlayer(referencesResult.getData().player());
            playerSpiritRoot.setSpiritRoot(referencesResult.getData().spiritRoot());

            var updatedPlayerSpiritRoot = playerSpiritRootRepository.save(playerSpiritRoot);
            return ServiceResult.success("Player spirit root updated successfully", toDto(updatedPlayerSpiritRoot));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating player spirit root", ex);
        }
    }

    public ServiceResult<Void> deletePlayerSpiritRoot(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player spirit root id is invalid");
            }

            if (!playerSpiritRootRepository.existsById(id)) {
                return ServiceResult.failure("Player spirit root not found");
            }

            playerSpiritRootRepository.deleteById(id);
            return ServiceResult.success("Player spirit root deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting player spirit root", ex);
        }
    }


    private ServiceResult<PlayerSpiritRootReferences> resolveReferences(PlayerSpiritRootDTO dto) {
        var player = playerRepository.findById(dto.playerId());
        if (player.isEmpty()) {
            return ServiceResult.failure("Player not found");
        }

        var spiritRoot = spiritRootRepository.findById(dto.spiritRootId());
        if (spiritRoot.isEmpty()) {
            return ServiceResult.failure("Spirit root not found");
        }

        return ServiceResult.success("Player spirit root references resolved", new PlayerSpiritRootReferences(player.get(), spiritRoot.get()));
    }

    private PlayerSpiritRootDTO toDto(PlayerSpiritRoot playerSpiritRoot) {
        return new PlayerSpiritRootDTO(
                playerSpiritRoot.getPlayer().getId(),
                playerSpiritRoot.getSpiritRoot().getId(),
                playerSpiritRoot.getSpiritRoot().getName()
        );
    }

    private ServiceResult<PlayerSpiritRootDTO> validateDto(PlayerSpiritRootDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Player spirit root data is required");
        }

        if (dto.playerId() == null) {
            return ServiceResult.failure("Player id is invalid");
        }

        if (dto.spiritRootId() == null || dto.spiritRootId() <= 0) {
            return ServiceResult.failure("Spirit root id is invalid");
        }

        return null;
    }

    private SpiritRoot randomFrom(List<SpiritRoot> spiritRoots) {
        return spiritRoots.get(random.nextInt(spiritRoots.size()));
    }

    private List<SpiritRoot> randomDistinct(List<SpiritRoot> spiritRoots, int count) {
        var shuffledSpiritRoots = new ArrayList<>(spiritRoots);
        Collections.shuffle(shuffledSpiritRoots, random);
        return shuffledSpiritRoots.subList(0, count);
    }

    private record PlayerSpiritRootReferences(Player player, SpiritRoot spiritRoot) {
    }
}
