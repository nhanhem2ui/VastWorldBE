package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.region.DecorationsOfRegion;
import com.vastworld.vwbe.dto.region.GetAccessibleRegion;
import com.vastworld.vwbe.dto.region.GetPlayerRegionResponse;
import com.vastworld.vwbe.dto.region.GetRegionResponse;
import com.vastworld.vwbe.entities.Player;
import com.vastworld.vwbe.entities.Region;
import com.vastworld.vwbe.entities.RegionRequirement;
import com.vastworld.vwbe.enums.quests.PlayerQuestStatus;
import com.vastworld.vwbe.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegionService {

    private final PlayerLocationRepository playerLocationRepository;
    private final PlayerService playerService;
    private final RegionRepository regionRepository;
    private final RegionDecorationRepository regionDecorationRepository;
    private final PlayerRepository playerRepository;
    private final RegionRequirementRepository regionRequirementRepository;
    private final RedisService redisService;
    private final PlayerQuestRepository playerQuestRepository;
    private final PlayerSpiritRootRepository playerSpiritRootRepository;
    private final PlayerInventoryRepository playerInventoryRepository;

    public RegionService(RegionRepository regionRepository,
                         RedisService redisService, RegionDecorationRepository regionDecorationRepository,
                         PlayerLocationRepository playerLocationRepository, PlayerService playerService,
                         PlayerRepository playerRepository, RegionRequirementRepository regionRequirementRepository,
                         PlayerQuestRepository playerQuestRepository,  PlayerSpiritRootRepository playerSpiritRootRepository,
                         PlayerInventoryRepository playerInventoryRepository) {
        this.regionRepository = regionRepository;
        this.redisService = redisService;
        this.regionDecorationRepository = regionDecorationRepository;
        this.playerLocationRepository = playerLocationRepository;
        this.playerService = playerService;
        this.playerRepository = playerRepository;
        this.regionRequirementRepository = regionRequirementRepository;
        this.playerQuestRepository = playerQuestRepository;
        this.playerSpiritRootRepository = playerSpiritRootRepository;
        this.playerInventoryRepository = playerInventoryRepository;
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
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving region.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<List<GetAccessibleRegion>> getAccessibleRegion(UUID playerId) {

        if (playerId == null) {
            return ServiceResult.failure("Player ID is required", HttpStatus.BAD_REQUEST);
        }

        var player = playerRepository.findById(playerId).orElse(null);

        if (player == null) {
            return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
        }

        List<Region> regions = regionRepository.findAll();

        if (regions.isEmpty()) {
            return ServiceResult.success("Nothing here", List.of(), HttpStatus.NO_CONTENT);
        }

        List<Integer> regionIds = regions.stream()
                .map(Region::getRegionId)
                .toList();

        // Load all requirements for these regions
        List<RegionRequirement> requirements = regionRequirementRepository.findAllByRegion_RegionIdIn(regionIds);

        // Group requirements by region ID
        Map<Integer, List<RegionRequirement>> requirementsMap =
                requirements.stream()
                        .collect(Collectors.groupingBy(regionRequirement -> regionRequirement.getRegion().getRegionId()));

        List<GetAccessibleRegion> accessibleRegions = regions.stream()
                .filter(region -> {
                    List<RegionRequirement> regionRequirements =
                            requirementsMap.getOrDefault(
                                    region.getRegionId(),
                                    List.of()
                            );

                    // Every requirement must be satisfied
                    return regionRequirements.stream()
                            .allMatch(requirement ->
                                    isRequirementSatisfied(player, requirement)
                            );
                })
                .map(region -> new GetAccessibleRegion(
                        region.getRegionId(),
                        region.getRegionName()
                ))
                .toList();

        return ServiceResult.success("Retrieve success", accessibleRegions, HttpStatus.OK);
    }

    private boolean isRequirementSatisfied(Player player, RegionRequirement requirement) {

        return switch (requirement.getRequirementType()) {

            case REALM ->
                    requirement.getRequiredRealm().getId() != null && player.getRealmId() >= requirement.getRequiredRealm().getId();

            case QUEST -> requirement.getRequiredRealm().getId() != null && playerQuestRepository
                    .existsByPlayer_IdAndQuest_IdAndStatus(
                            player.getId(),
                            requirement.getRequiredQuest().getId(),
                            PlayerQuestStatus.CLAIMED
                    );

            case ITEM -> requirement.getRequiredItem().getId() != null &&
                    playerInventoryRepository.existsByPlayer_IdAndItem_IdAndQuantityGreaterThan(
                            player.getId(),
                            requirement.getRequiredItem().getId(),
                            0
                    );

            case SPIRIT_ROOT -> requirement.getRequiredSpiritRoot().getId() != null
                    && playerSpiritRootRepository.existsByPlayer_IdAndSpiritRoot_Id(
                            player.getId(),
                            requirement.getRequiredSpiritRoot().getId()
                    );

            case REPUTATION -> requirement.getRequiredReputation() != null
                    && player.getReputation() >=
                    requirement.getRequiredReputation();
        };
    }
}
