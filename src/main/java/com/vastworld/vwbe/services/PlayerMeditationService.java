package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.BeginMeditateRequest;
import com.vastworld.vwbe.dto.playermeditation.GetCultivationPointPerMinsResponse;
import com.vastworld.vwbe.dto.playermeditation.GetPlayerMeditationByIdResponse;
import com.vastworld.vwbe.dto.playermeditation.PlayerMeditationDTO;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerMeditation;
import com.vastworld.vwbe.repositories.PlayerMeditationRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerMeditationService {
    private final PlayerMeditationRepository playerMeditationRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSpiritRootService playerSpiritRootService;
    private final SpiritRootService spiritRootService;
    private final PlayerService playerService;
    private final RedisService redisService;

    public PlayerMeditationService(PlayerMeditationRepository playerMeditationRepository, PlayerRepository playerRepository,
                                   PlayerSpiritRootService playerSpiritRootService, SpiritRootService spiritRootService,
                                   PlayerService playerService, RedisService redisService) {
        this.playerMeditationRepository = playerMeditationRepository;
        this.playerRepository = playerRepository;
        this.playerSpiritRootService = playerSpiritRootService;
        this.spiritRootService = spiritRootService;
        this.playerService = playerService;
        this.redisService = redisService;
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

    public ServiceResult<GetPlayerMeditationByIdResponse> getPlayerMeditationByPlayerId(UUID playerId) {
        try {
            if (playerId == null) {
                return ServiceResult.failure("Player meditation id is invalid");
            }

            var cacheKey = CacheKeys.playerMeditation("playerId", playerId);
            var cached = redisService.get(cacheKey, new TypeReference<GetPlayerMeditationByIdResponse>(){});

            if(cached != null){
                return ServiceResult.success("Player meditation retrieved successfully", cached);
            }

            var playerMeditationOptional = playerMeditationRepository.findByPlayer_IdAndIsClaimed(playerId, false);

            if (playerMeditationOptional.isEmpty()) {
                return ServiceResult.failure("Player meditation not found");
            }

            var playerMeditation = playerMeditationOptional.get();

            var data = new GetPlayerMeditationByIdResponse(playerMeditation.getStartTime(),playerMeditation.getEndTime(),
                    playerMeditation.getCultivationPerMinute(), playerMeditation.getTotalCultivationReward(),
                    playerMeditation.getIsClaimed());

            return ServiceResult.success("Player meditation retrieved successfully", data);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player meditation", ex);
        }
    }

    public ServiceResult<GetCultivationPointPerMinsResponse> getCultivationPointPerMins(UUID playerId){
        if(playerId == null){
            return ServiceResult.failure("PlayerId not found");
        }
        var cultivationPoint = calculateCultivationPointPerMinutes(playerId);
        var data = new GetCultivationPointPerMinsResponse(cultivationPoint);
        return ServiceResult.success("Get cp per minutes success",data);
    }

    public ServiceResult<Void> beginMeditate(BeginMeditateRequest request){
        var playerDTO = playerService.getPlayerById(request.playerId()).getData();

        if (playerDTO == null) {
            return ServiceResult.failure("Player not exists");
        }

        var player = playerRepository.getReferenceById(request.playerId());

        var existMediation = playerMeditationRepository.findByPlayer_IdAndIsClaimed(request.playerId(), false);

        if (existMediation.isPresent()){
            return ServiceResult.failure("Your mediation session already exists");
        }

        var pointsPerMins = calculateCultivationPointPerMinutes(request.playerId());

        var playerMediation = new PlayerMeditation();
        playerMediation.setPlayer(player);

        var endTime = LocalDateTime.now().plusMinutes(request.durationMinutes());
        playerMediation.setEndTime(endTime);

        playerMediation.setDurationMinutes(request.durationMinutes());
        playerMediation.setCultivationPerMinute(pointsPerMins);

        var totalReward = pointsPerMins * request.durationMinutes();
        playerMediation.setTotalCultivationReward(totalReward);

        playerMeditationRepository.save(playerMediation);

        return ServiceResult.success("Start meditation session success");
    }

    public ServiceResult<Void> claimMeditate(UUID playerId){

        if(playerId == null) {
            return ServiceResult.failure("Player not found");
        }
        var playerOptional = playerRepository.findById(playerId);
        if(playerOptional.isEmpty()){
            return ServiceResult.failure("Player not exists");
        }
        var player = playerOptional.get();

        var playerMeditationOptional = playerMeditationRepository.findByPlayer_IdAndIsClaimed(playerId, false);
        if(playerMeditationOptional.isEmpty()){
            return ServiceResult.failure("Your mediation session are not exists");
        }
        var playerMeditation = playerMeditationOptional.get();

        if(playerMeditation.getIsClaimed()){
            return ServiceResult.failure("You already claimed the reward");
        }

        playerMeditation.setIsClaimed(true);
        player.setCultivationPoint(player.getCultivationPoint() + playerMeditation.getTotalCultivationReward());

        playerMeditationRepository.save(playerMeditation);
        playerRepository.save(player);

        return ServiceResult.success("Session claimed");
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
    private Long calculateCultivationPointPerMinutes(UUID playerId) {
        var sumPointPerMins = 1L;

        sumPointPerMins += Math.round(sumPointPerMins + getCpPlayerCultivationSpeed(playerId));

        var cpSpiritRoot = getCpPlayerSpiritRoot(playerId);
        sumPointPerMins += cpSpiritRoot;

        return sumPointPerMins;
    }

    private Long getCpPlayerCultivationSpeed(UUID playerId){
       return (long) Math.ceil(playerService.getPlayerById(playerId).getData().cultivationSpeed());
    }

    private Double getCpPlayerSpiritRoot(UUID playerId){
        var spiritRoots = playerSpiritRootService
                .getPlayerSpiritRootEntityById(playerId)
                .getData();

        switch (spiritRoots.size()) {
            case 5:
                return GameBalance.FIVE_SPIRIT_ROOT;
            case 4:
                return GameBalance.FOUR_SPIRIT_ROOT;
            case 2:
                return GameBalance.TWO_SPIRIT_ROOT;
            case 1:
                var root = spiritRootService.getSpiritRootById(spiritRoots.getFirst().spiritRootId());
                var data = root.getData();
                if (data.isVariant()) {
                    return GameBalance.VARIANT_SPIRIT_ROOT;
                } else {
                    return GameBalance.HEAVENLY_SPIRIT_ROOT;
                }
            default:
                return GameBalance.THREE_SPIRIT_ROOT;
        }
    }
}
