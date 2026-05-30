package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerspiritroot.PlayerSpiritRootDTO;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerSpiritRoot;
import com.vastworld.vwbe.entites.SpiritRoot;
import com.vastworld.vwbe.repositories.PlayerRepository;
import com.vastworld.vwbe.repositories.PlayerSpiritRootRepository;
import com.vastworld.vwbe.repositories.SpiritRootRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlayerSpiritRootService {
    private final PlayerSpiritRootRepository playerSpiritRootRepository;
    private final PlayerRepository playerRepository;
    private final SpiritRootRepository spiritRootRepository;

    public PlayerSpiritRootService(
            PlayerSpiritRootRepository playerSpiritRootRepository,
            PlayerRepository playerRepository,
            SpiritRootRepository spiritRootRepository) {
        this.playerSpiritRootRepository = playerSpiritRootRepository;
        this.playerRepository = playerRepository;
        this.spiritRootRepository = spiritRootRepository;
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

    public ServiceResult<PlayerSpiritRootDTO> getPlayerSpiritRootById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player spirit root id is invalid");
            }

            var playerSpiritRoot = playerSpiritRootRepository.findById(id);
            if (playerSpiritRoot.isEmpty()) {
                return ServiceResult.failure("Player spirit root not found");
            }

            return ServiceResult.success("Player spirit root retrieved successfully", toDto(playerSpiritRoot.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player spirit root", ex);
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
                playerSpiritRoot.getId(),
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

    private record PlayerSpiritRootReferences(Player player, SpiritRoot spiritRoot) {
    }
}
