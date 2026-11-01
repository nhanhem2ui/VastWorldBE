package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.GetPlayerMeditationByIdResponse;
import com.vastworld.vwbe.entities.PlayerMeditation;
import com.vastworld.vwbe.repositories.PlayerMeditationRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public ServiceResult<GetPlayerMeditationByIdResponse> getPlayerMeditationByPlayerId(UUID playerId) {
        try {
            if (playerId == null) {
                return ServiceResult.failure("Player meditation id is invalid", HttpStatus.BAD_REQUEST);
            }

            var cacheKey = CacheKeys.playerMeditations("playerId", playerId);
            var cached = redisService.get(cacheKey, new TypeReference<GetPlayerMeditationByIdResponse>(){});

            if(cached != null){
                return ServiceResult.success("Player meditation retrieved successfully", cached, HttpStatus.OK);
            }

            var playerMeditationOptional = playerMeditationRepository.findByPlayer_IdAndIsClaimed(playerId, false);

            if (playerMeditationOptional.isEmpty()) {
                return ServiceResult.failure("Player meditation not found", HttpStatus.NOT_FOUND);
            }

            var playerMeditation = playerMeditationOptional.get();

            var data = new GetPlayerMeditationByIdResponse(playerMeditation.getStartTime(),playerMeditation.getEndTime(),
                    playerMeditation.getCultivationPerMinute(), playerMeditation.getTotalCultivationReward(),
                    playerMeditation.getIsClaimed());

            return ServiceResult.success("Player meditation retrieved successfully", data, HttpStatus.OK);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player meditation", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<Void> beginMeditate(UUID playerId, Integer durationMinutes){

        var playerResult = playerService.getPlayerEntityById(playerId);

        if (!playerResult.isSuccess()) {
            return ServiceResult.failure("Player not exists", HttpStatus.NOT_FOUND);
        }

        var player = playerResult.getData();

        var existMediation = playerMeditationRepository.findByPlayer_IdAndIsClaimed(playerId, false);

        if (existMediation.isPresent()){
            return ServiceResult.failure("Your mediation session already exists",  HttpStatus.CONFLICT);
        }

        var pointsPerMins = calculateCultivationPointPerMinutes(playerId);

        var playerMediation = new PlayerMeditation();
        playerMediation.setPlayer(player);

        var endTime = LocalDateTime.now().plusMinutes(durationMinutes);
        playerMediation.setEndTime(endTime);

        playerMediation.setDurationMinutes(durationMinutes);
        playerMediation.setCultivationPerMinute(pointsPerMins);

        var totalReward = pointsPerMins * durationMinutes;
        playerMediation.setTotalCultivationReward(totalReward);

        playerMeditationRepository.save(playerMediation);

        return ServiceResult.success("Start meditation session success");
    }

    public ServiceResult<Void> claimMeditate(UUID playerId){

        if(playerId == null) {
            return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
        }
        var playerOptional = playerRepository.findById(playerId);
        if(playerOptional.isEmpty()){
            return ServiceResult.failure("Player not exists", HttpStatus.NOT_FOUND);
        }
        var player = playerOptional.get();

        var playerMeditationOptional = playerMeditationRepository.findByPlayer_IdAndIsClaimed(playerId, false);
        if(playerMeditationOptional.isEmpty()){
            return ServiceResult.failure("Your mediation session are not exists", HttpStatus.NOT_FOUND);
        }
        var playerMeditation = playerMeditationOptional.get();

        if(playerMeditation.getIsClaimed()){
            return ServiceResult.failure("You already claimed the reward", HttpStatus.CONFLICT);
        }

        playerMeditation.setIsClaimed(true);
        player.setCultivationPoint(player.getCultivationPoint() + playerMeditation.getTotalCultivationReward());

        playerMeditationRepository.save(playerMeditation);
        playerRepository.save(player);

        return ServiceResult.success("Session claimed", HttpStatus.OK);
    }

    private Long calculateCultivationPointPerMinutes(UUID playerId) {
        var sumPointPerMins = 1L;

        sumPointPerMins += Math.round(sumPointPerMins + getCpPlayerCultivationSpeed(playerId));

        var cpSpiritRoot = getPlayerSpiritRoot(playerId);
        sumPointPerMins *= cpSpiritRoot;

        return sumPointPerMins;
    }

    private Long getCpPlayerCultivationSpeed(UUID playerId){
       return (long) Math.ceil(playerService.getPlayerById(playerId).getData().cultivationSpeed());
    }

    private Double getPlayerSpiritRoot(UUID playerId){
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
