package com.vastworld.vwbe.services;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
            return ServiceResult.success("Player spirit root retrieved successfully", data, HttpStatus.OK);
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

            return ServiceResult.success("Player spirit root retrieved successfully", data, HttpStatus.OK);
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

            return ServiceResult.success("Spirit root rolled successfully", result, HttpStatus.OK);
        }
        catch (Exception ex) {
            return ServiceResult.failure("Error rolling spirit root", ex);
        }
    }

    private PlayerSpiritRootDTO toDto(PlayerSpiritRoot playerSpiritRoot) {
        return new PlayerSpiritRootDTO(
                playerSpiritRoot.getPlayer().getId(),
                playerSpiritRoot.getSpiritRoot().getId(),
                playerSpiritRoot.getSpiritRoot().getName()
        );
    }

    private SpiritRoot randomFrom(List<SpiritRoot> spiritRoots) {
        return spiritRoots.get(random.nextInt(spiritRoots.size()));
    }

    private List<SpiritRoot> randomDistinct(List<SpiritRoot> spiritRoots, int count) {
        var shuffledSpiritRoots = new ArrayList<>(spiritRoots);
        Collections.shuffle(shuffledSpiritRoots, random);
        return shuffledSpiritRoots.subList(0, count);
    }
}
