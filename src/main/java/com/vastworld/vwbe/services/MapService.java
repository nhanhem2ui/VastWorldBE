package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.listeners.PlayerTravelEvent;
import com.vastworld.vwbe.dto.map.*;
import com.vastworld.vwbe.listeners.QuestEventListener;
import com.vastworld.vwbe.repositories.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MapService {
    private final MapTileRepository mapTileRepository;
    private final MapRepository mapRepository;
    private final MapDecorationRepository mapDecorationRepository;
    private final PlayerLocationRepository playerLocationRepository;
    private final MapInteractableRepository mapInteractableRepository;
    private final PlayerService playerService;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;

    public MapService(MapTileRepository mapTileRepository, MapRepository mapRepository,
                      RedisService redisService, PlayerService playerService,
                      PlayerLocationRepository playerLocationRepository, MapDecorationRepository mapDecorationRepository,
                      MapInteractableRepository mapInteractableRepository, ApplicationEventPublisher eventPublisher) {
        this.mapTileRepository = mapTileRepository;
        this.mapRepository = mapRepository;
        this.redisService = redisService;
        this.playerService = playerService;
        this.playerLocationRepository = playerLocationRepository;
        this.mapDecorationRepository = mapDecorationRepository;
        this.mapInteractableRepository = mapInteractableRepository;
        this.eventPublisher = eventPublisher;
    }

    public ServiceResult<GetMapResponse> getMap(Integer mapId) {
        try {
            var cachedKey = CacheKeys.maps(mapId);
            var cached = redisService.get(cachedKey, new TypeReference<GetMapResponse>() {
            });

            if (cached != null) {
                return ServiceResult.success("Map retrieve successfully", cached, HttpStatus.OK);
            }

            var mapOptional = mapRepository.findById(mapId);
            if (mapOptional.isEmpty()) {
                return ServiceResult.failure("Map not found", HttpStatus.NOT_FOUND);
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

            var mapDecorations = mapDecorationRepository.findByMap_MapId(mapId);

            var decorations = mapDecorations.stream().map(decor -> {
                var asset = decor.getGameAsset();

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
                        decor.getX(),
                        decor.getY(),
                        decor.getSpriteFrame() == null
                                ? null
                                : decor.getSpriteFrame(),
                        assetDto
                );
            }).toList();

            var mapInteractable = mapInteractableRepository.findByMap_MapId(mapId);

            var interactable = mapInteractable.stream().map(iter -> new InteractableOfMap(
                    iter.getX(),
                    iter.getY(),
                    iter.getType()
            )).toList();

            var response = new GetMapResponse(
                    map.getMapName(),
                    map.getWidth(),
                    map.getHeight(),
                    tiles,
                    decorations,
                    interactable
            );
            return ServiceResult.success("Map retrieve successfully", response, HttpStatus.OK);

        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving map.", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<GetPlayerLocationResponse> getPlayerLocation(UUID playerId) {
        try {
            var player = playerService.getPlayerById(playerId).getData();
            if (player == null) {
                return ServiceResult.failure("Player not found",  HttpStatus.NOT_FOUND);
            }
            var playerPositionOptional = playerLocationRepository.findByPlayer_Id(playerId);

            if (playerPositionOptional.isEmpty()) {
                return ServiceResult.failure("Internal server error",   HttpStatus.INTERNAL_SERVER_ERROR);
            }
            var playerPosition = playerPositionOptional.get();
            var data = new GetPlayerLocationResponse(
                    playerPosition.getCurrentMap().getMapId(),
                    playerPosition.getX(),
                    playerPosition.getY()
            );
            return ServiceResult.success("Player location retrieve successfully", data, HttpStatus.OK);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player location.", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<Void> travel(TravelRequest request) {
        try {
            var playerLocationOptional = playerLocationRepository.findByPlayer_Id(request.playerId());
            if (playerLocationOptional.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerLocation = playerLocationOptional.get();

            if (playerLocation.getX().equals(request.x()) && playerLocation.getY().equals(request.y()) && playerLocation.getCurrentMap().getMapId().equals(request.mapId())) {
                return ServiceResult.failure("Coordinates are the same", HttpStatus.BAD_REQUEST);
            }

            var mapOptional = mapRepository.findById(request.mapId());
            if (mapOptional.isEmpty()) {
                return ServiceResult.failure("Map not found",  HttpStatus.NOT_FOUND);
            }

            if (!playerLocation.getCurrentMap().equals(mapOptional.get())) {
                playerLocation.setCurrentMap(mapOptional.get());
            }

            playerLocation.setX(request.x());
            playerLocation.setY(request.y());
            playerLocationRepository.save(playerLocation);

            eventPublisher.publishEvent(new PlayerTravelEvent(request.playerId(), request.mapId(), request.x(), request.y()));

            return ServiceResult.success("Travelled to destination", HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player location.", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}