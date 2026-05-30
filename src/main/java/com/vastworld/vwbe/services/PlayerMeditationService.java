package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.PlayerMeditationDTO;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerMeditation;
import com.vastworld.vwbe.repositories.PlayerMeditationRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PlayerMeditationService {
    private final PlayerMeditationRepository playerMeditationRepository;
    private final PlayerRepository playerRepository;

    public PlayerMeditationService(PlayerMeditationRepository playerMeditationRepository, PlayerRepository playerRepository) {
        this.playerMeditationRepository = playerMeditationRepository;
        this.playerRepository = playerRepository;
    }

    public ServiceResult<List<PlayerMeditationDTO>> getAllPlayerMeditations() {
        try {
            var playerMeditationList = playerMeditationRepository.findAll();
            if (playerMeditationList.isEmpty()) {
                return ServiceResult.failure("No player meditation found");
            }

            var dtoList = playerMeditationList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player meditation retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player meditation", ex);
        }
    }

    public ServiceResult<PlayerMeditationDTO> getPlayerMeditationById(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player meditation id is invalid");
            }

            var playerMeditation = playerMeditationRepository.findById(id);
            if (playerMeditation.isEmpty()) {
                return ServiceResult.failure("Player meditation not found");
            }

            return ServiceResult.success("Player meditation retrieved successfully", toDto(playerMeditation.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player meditation", ex);
        }
    }

    public ServiceResult<PlayerMeditationDTO> createPlayerMeditation(PlayerMeditationDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var playerResult = resolvePlayer(dto);
            if (!playerResult.isSuccess()) {
                return ServiceResult.failure(playerResult.getMessage());
            }

            PlayerMeditation playerMeditation = new PlayerMeditation();
            playerMeditation.setPlayer(playerResult.getData());
            applyDto(playerMeditation, dto);

            var savedPlayerMeditation = playerMeditationRepository.save(playerMeditation);
            return ServiceResult.success("Player meditation created successfully", toDto(savedPlayerMeditation));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating player meditation", ex);
        }
    }

    public ServiceResult<PlayerMeditationDTO> updatePlayerMeditation(Long id, PlayerMeditationDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player meditation id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var playerMeditationOptional = playerMeditationRepository.findById(id);
            if (playerMeditationOptional.isEmpty()) {
                return ServiceResult.failure("Player meditation not found");
            }

            var playerResult = resolvePlayer(dto);
            if (!playerResult.isSuccess()) {
                return ServiceResult.failure(playerResult.getMessage());
            }

            var playerMeditation = playerMeditationOptional.get();
            playerMeditation.setPlayer(playerResult.getData());
            applyDto(playerMeditation, dto);

            var updatedPlayerMeditation = playerMeditationRepository.save(playerMeditation);
            return ServiceResult.success("Player meditation updated successfully", toDto(updatedPlayerMeditation));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating player meditation", ex);
        }
    }

    public ServiceResult<Void> deletePlayerMeditation(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player meditation id is invalid");
            }

            if (!playerMeditationRepository.existsById(id)) {
                return ServiceResult.failure("Player meditation not found");
            }

            playerMeditationRepository.deleteById(id);
            return ServiceResult.success("Player meditation deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting player meditation", ex);
        }
    }

    private ServiceResult<Player> resolvePlayer(PlayerMeditationDTO dto) {
        var player = playerRepository.findById(dto.playerId());
        if (player.isEmpty()) {
            return ServiceResult.failure("Player not found");
        }

        return ServiceResult.success("Player resolved", player.get());
    }

    private void applyDto(PlayerMeditation playerMeditation, PlayerMeditationDTO dto) {
        var startTime = dto.startTime() != null ? dto.startTime() : LocalDateTime.now();
        var endTime = dto.endTime() != null ? dto.endTime() : startTime.plusMinutes(dto.durationMinutes());

        playerMeditation.setStartTime(startTime);
        playerMeditation.setDurationMinutes(dto.durationMinutes());
        playerMeditation.setEndTime(endTime);
        playerMeditation.setCultivationPerMinute(dto.cultivationPerMinute());
        playerMeditation.setTotalCultivationReward(dto.totalCultivationReward() != null ? dto.totalCultivationReward() : 0L);
        playerMeditation.setIsCompleted(Boolean.TRUE.equals(dto.isCompleted()));
        playerMeditation.setIsClaimed(Boolean.TRUE.equals(dto.isClaimed()));
    }

    private PlayerMeditationDTO toDto(PlayerMeditation playerMeditation) {
        return new PlayerMeditationDTO(
                playerMeditation.getId(),
                playerMeditation.getPlayer().getId(),
                playerMeditation.getStartTime(),
                playerMeditation.getDurationMinutes(),
                playerMeditation.getEndTime(),
                playerMeditation.getCultivationPerMinute(),
                playerMeditation.getTotalCultivationReward(),
                playerMeditation.getIsCompleted(),
                playerMeditation.getIsClaimed()
        );
    }

    private ServiceResult<PlayerMeditationDTO> validateDto(PlayerMeditationDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Player meditation data is required");
        }

        if (dto.playerId() == null) {
            return ServiceResult.failure("Player id is invalid");
        }

        if (dto.durationMinutes() == null || dto.durationMinutes() <= 0) {
            return ServiceResult.failure("Duration minutes must be greater than 0");
        }

        if (dto.cultivationPerMinute() == null || dto.cultivationPerMinute() < 0) {
            return ServiceResult.failure("Cultivation per minute must be greater than or equal to 0");
        }

        if (dto.totalCultivationReward() != null && dto.totalCultivationReward() < 0) {
            return ServiceResult.failure("Total cultivation reward must be greater than or equal to 0");
        }

        if (dto.startTime() != null && dto.endTime() != null && dto.endTime().isBefore(dto.startTime())) {
            return ServiceResult.failure("End time must be after start time");
        }

        return null;
    }
}
