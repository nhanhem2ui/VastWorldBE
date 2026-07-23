package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.region.DecorationsOfRegion;
import com.vastworld.vwbe.dto.region.GetRegionResponse;
import com.vastworld.vwbe.repositories.MapRepository;
import com.vastworld.vwbe.repositories.RegionDecorationRepository;
import com.vastworld.vwbe.repositories.RegionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegionService {

    public MapRepository mapRepository;
    public RegionRepository regionRepository;
    public RegionDecorationRepository  regionDecorationRepository;
    public RedisService redisService;
    public RegionService(MapRepository mapRepository,  RegionRepository regionRepository,
                         RedisService redisService, RegionDecorationRepository regionDecorationRepository) {
        this.mapRepository = mapRepository;
        this.regionRepository = regionRepository;
        this.redisService = redisService;
        this.regionDecorationRepository = regionDecorationRepository;
    }

    public ServiceResult<GetRegionResponse> getRegion(Integer id) {
        try {
            var cacheKey = CacheKeys.maps("region", id);
            var cached = redisService.get(cacheKey, new TypeReference<GetRegionResponse>() {
            });
            if(cached != null) {
                return ServiceResult.success("success", cached, HttpStatus.OK);
            }

            var regionOptional = regionRepository.findById(id);
            if(regionOptional.isEmpty()) {
                return  ServiceResult.failure("Region not found", HttpStatus.NOT_FOUND);
            }
            var region = regionOptional.get();
            var decorationList = regionDecorationRepository.findByRegion_RegionId(region.getRegionId());

            var decorations = decorationList.stream().map(decoration -> new DecorationsOfRegion(
                    decoration.getMap().getMapId(),
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
            return ServiceResult.success("success", data, HttpStatus.OK);
        }
        catch (Exception ex) {
            return ServiceResult.failure("Error retrieving region.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
