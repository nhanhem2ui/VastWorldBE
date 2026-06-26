package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.map.AssetTile;
import com.vastworld.vwbe.dto.map.GetMapResponse;
import com.vastworld.vwbe.dto.map.TilesOfMaps;
import com.vastworld.vwbe.repositories.MapRepository;
import com.vastworld.vwbe.repositories.MapTileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MapService {
    private final MapTileRepository mapTileRepository;
    private final MapRepository mapRepository;
    private final RedisService redisService;
    public MapService(MapTileRepository mapTileRepository, MapRepository mapRepository,
                      RedisService redisService) {
        this.mapTileRepository = mapTileRepository;
        this.mapRepository = mapRepository;
        this.redisService = redisService;
    }

    public ServiceResult<GetMapResponse> getMap(Integer mapId) {
        try {
            var cachedKey = CacheKeys.maps(mapId);
            var cached = redisService.get(cachedKey, new TypeReference<GetMapResponse>() {});

            if(cached != null) {
                return ServiceResult.success("Map retrieve successfully", cached);
            }

            var mapOptional = mapRepository.findById(mapId);
            if (mapOptional.isEmpty()) {
                return ServiceResult.failure("Map not found");
            }

            var map = mapOptional.get();

            var mapTiles = mapTileRepository.findByGameMap_MapId(mapId);

            var tiles = mapTiles.stream()
                    .map(tile -> {

                        var asset = tile.getAsset();

                        var assetDto = new AssetTile(
                                asset.getIsAnimated(),
                                asset.getAssetUrl(),
                                asset.getFrameConfig(),
                                asset.getOffsetX(),
                                asset.getOffsetY(),
                                asset.getWidth(),
                                asset.getHeight(),
                                asset.getAssetType()
                        );

                        return new TilesOfMaps(
                                tile.getX(),
                                tile.getY(),
                                tile.getSpriteFrame() == null
                                        ? null
                                        : tile.getSpriteFrame(),
                                assetDto
                        );
                    })
                    .toList();

            var response = new GetMapResponse(
                    map.getMapName(),
                    map.getWidth(),
                    map.getHeight(),
                    tiles
            );
            return ServiceResult.success("Map retrieve successfully", response);

        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving map.", ex);
        }
    }
}
