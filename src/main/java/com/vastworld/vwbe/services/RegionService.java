package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.region.DecorationsOfRegion;
import com.vastworld.vwbe.dto.region.GetPlayerRegionResponse;
import com.vastworld.vwbe.dto.region.GetRegionResponse;
import com.vastworld.vwbe.repositories.PlayerLocationRepository;
import com.vastworld.vwbe.repositories.RegionDecorationRepository;
import com.vastworld.vwbe.repositories.RegionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RegionService {

    private final PlayerLocationRepository playerLocationRepository;
    private final PlayerService playerService;
    private final RegionRepository regionRepository;
    private final RegionDecorationRepository regionDecorationRepository;
    private final RedisService redisService;

    public RegionService(RegionRepository regionRepository,
                         RedisService redisService, RegionDecorationRepository regionDecorationRepository,
                         PlayerLocationRepository playerLocationRepository, PlayerService playerService) {
        this.regionRepository = regionRepository;
        this.redisService = redisService;
        this.regionDecorationRepository = regionDecorationRepository;
        this.playerLocationRepository = playerLocationRepository;
        this.playerService = playerService;
    }

    public ServiceResult<GetRegionResponse> getRegion(Integer id) {
        try {
            var cacheKey = CacheKeys.maps("region", id);
            var cached = redisService.get(cacheKey, new TypeReference<GetRegionResponse>() {
            });
            if (cached != null) {
                return ServiceResult.success("success", cached, HttpStatus.OK);
            }

            var regionOptional = regionRepository.findById(id);
            if (regionOptional.isEmpty()) {
                return ServiceResult.failure("Region not found", HttpStatus.NOT_FOUND);
            }
            var region = regionOptional.get();
            var decorationList = regionDecorationRepository.findByRegion_RegionId(region.getRegionId());

            var decorations = decorationList.stream().map(decoration -> new DecorationsOfRegion(
                    decoration.getMap() == null ? null : decoration.getMap().getMapId(),
                    decoration.getLabel(),
                    decoration.getX(),
                    decoration.getY(),
                    decoration.getWidth(),
                    decoration.getHeight(),
                    decoration.getTopHeight(),
                    decoration.getRightWidth(),
                    decoration.getBottomHeight(),
                    decoration.getLeftWidth(),
                    decoration.getBackgroundTexture(),
                    decoration.getTextColor()
            )).toList();
            var data = new GetRegionResponse(
                    region.getRegionName(),
                    region.getWidth(),
                    region.getHeight(),
                    region.getBackgroundImage(),
                    decorations
            );
            return ServiceResult.success("Get region success", data, HttpStatus.OK);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving region.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<GetPlayerRegionResponse> getPlayerRegion(UUID playerId) {
        try {
            var player = playerService.getPlayerById(playerId);
            if (player == null) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerLocationOptional = playerLocationRepository.findByPlayer_Id(playerId);
            if (playerLocationOptional.isEmpty()) {
                return ServiceResult.failure("Player location not found", HttpStatus.NOT_FOUND);
            }
            var playerLocation = playerLocationOptional.get();
            var mapId = playerLocation.getCurrentMap().getMapId();

            var regionOfMapOptional = regionDecorationRepository.findByMap_MapId(mapId);
            if (regionOfMapOptional.isEmpty()) {
                return ServiceResult.failure("Region not found", HttpStatus.NOT_FOUND);
            }

            var regionOfMap = regionOfMapOptional.get();
            var data = new GetPlayerRegionResponse(
                    mapId,
                    regionOfMap.getRegion().getRegionId()
            );
            return ServiceResult.success("Get player region success", data, HttpStatus.OK);
        }
        catch (Exception ex) {
            return ServiceResult.failure("Error retrieving region.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
