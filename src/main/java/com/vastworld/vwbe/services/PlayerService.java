package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.common.enums.RealmEnum;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.player.GetPlayerNextBreakthroughResponse;
import com.vastworld.vwbe.dto.player.NewPlayableDTO;
import com.vastworld.vwbe.dto.player.PlayerDTO;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerLocation;
import com.vastworld.vwbe.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final AccountRepository accountRepository;
    private final CultivationRealmService cultivationRealmService;
    private final CultivationRealmRepository cultivationRealmRepository;
    private final RealmStageService realmStageService;
    private final RealmStageRepository realmStageRepository;
    private final PlayerLocationRepository playerLocationRepository;
    private final RedisService redisService;
    private final MapRepository mapRepository;

    public PlayerService(PlayerRepository playerRepository, AccountRepository accountRepository,
                         CultivationRealmRepository cultivationRealmRepository, RealmStageRepository realmStageRepository,
                         RedisService redisService, CultivationRealmService cultivationRealmService,
                         RealmStageService realmStageService, PlayerLocationRepository playerLocationRepository,
                         MapRepository mapRepository) {
        this.playerRepository = playerRepository;
        this.accountRepository = accountRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.realmStageRepository = realmStageRepository;
        this.redisService = redisService;
        this.cultivationRealmService = cultivationRealmService;
        this.realmStageService = realmStageService;
        this.playerLocationRepository = playerLocationRepository;
        this.mapRepository = mapRepository;
    }

    public ServiceResult<List<PlayerDTO>> getAllPlayers() {
        try {
            var playerList = playerRepository.findAll();

            if (playerList.isEmpty()) {
                return ServiceResult.failure("No player found");
            }

            var dtoList = playerList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player", ex);
        }
    }

    public ServiceResult<PlayerDTO> getPlayerById(UUID id) {
        if (id == null) return ServiceResult.failure("Player id is invalid");

        try {
            var cacheKey = CacheKeys.players("dto", id);

            var cachedDto = redisService.get(cacheKey, new TypeReference<PlayerDTO>() {});
            if (cachedDto != null) {
                return ServiceResult.success("Player retrieved successfully", cachedDto);
            }

            var playerOpt = playerRepository.findById(id);
            if (playerOpt.isEmpty()) {
                return ServiceResult.failure("Player not found");
            }

            PlayerDTO dto = toDto(playerOpt.get());
            redisService.set(cacheKey, dto);

            return ServiceResult.success("Player retrieved successfully", dto);

        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player from cache", ex);
        }
    }

    public ServiceResult<Player> getPlayerEntityById(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            // Database findById natively uses Hibernate First-Level Cache.
            // If it's already in the current transaction session, it won't hit the DB anyway.
            return playerRepository.findById(id)
                    .map(value -> ServiceResult.success("Player retrieved successfully", value))
                    .orElseGet(() -> ServiceResult.failure("Player not found"));

        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player", ex);
        }
    }

    public ServiceResult<GetPlayerNextBreakthroughResponse> getPlayerNextBreakthrough(UUID playerId) {
        if (playerId == null) {
            return ServiceResult.failure("PlayerId not found");
        }
        try {
            var playerResult = getPlayerEntityById(playerId);

            if (!playerResult.isSuccess()) {
                return ServiceResult.failure(playerResult.getMessage());
            }

            var player = playerResult.getData();

            if (player.getRealmId() >= RealmEnum.values().length
                    && player.getRealmStage() >= GameBalance.STAGES_PER_REALM) {

                return ServiceResult.failure("Player has reached the maximum realm");
            }

            var nextRealmAndStage = getNextRealmAndStageId(player.getRealmId(), player.getRealmStage());

            var isTribulation = player.getRealmStage() == GameBalance.STAGES_PER_REALM;

            long breakthroughPoints = GameBalance.getBreakthroughCpPoints(nextRealmAndStage.realmId, nextRealmAndStage.stageId);

            var chanceOfSuccess = 0F;

            var response = new GetPlayerNextBreakthroughResponse(
                    isTribulation,
                    breakthroughPoints,
                    chanceOfSuccess
            );
            return ServiceResult.success("Next breakthrough retrieved", response);
        } catch (Exception ex) {
            return ServiceResult.failure(ex.getMessage());
        }
    }

    private RealmAndStageId getNextRealmAndStageId(int currentRealm, int currentStage) {
        int nextRealmId = GameBalance.getNextRealmId(currentRealm, currentStage);
        int nextStageId = GameBalance.getNextStageId(currentStage);
        return new RealmAndStageId(nextRealmId, nextStageId);
    }

    public ServiceResult<Void> startBreakthrough(UUID playerId) {
        if (playerId == null) {
            return ServiceResult.failure("PlayerId not found");
        }
        var cacheKey = CacheKeys.players("dto", playerId);
        try {
            var playerResult = getPlayerEntityById(playerId);
            if (!playerResult.isSuccess() || playerResult.getData() == null) {
                return ServiceResult.failure("Player not found");
            }
            Player player = playerResult.getData();

            var breakthroughResult = getPlayerNextBreakthrough(playerId);
            if (!breakthroughResult.isSuccess() || breakthroughResult.getData() == null) {
                return ServiceResult.failure("Next breakthrough data not available");
            }
            var nextBreakthrough = breakthroughResult.getData();

            if (player.getCultivationPoint() < nextBreakthrough.breakthroughPoints()) {
                return ServiceResult.failure("Not enough cultivation points");
            }

            //cp deduction
            player.setCultivationPoint(player.getCultivationPoint() - nextBreakthrough.breakthroughPoints());

            var nextRealmAndStage = getNextRealmAndStageId(player.getRealmId(), player.getRealmStage());

            if (Boolean.TRUE.equals(nextBreakthrough.isTribulation())) {
                // TODO: Trigger Tribulation combat/event logic here
                 playerRepository.save(player);
                redisService.delete(cacheKey);
                return ServiceResult.success("Tribulation triggered! Prepare for lightning strikes.");
            } else {
                if (Math.random() <= nextBreakthrough.chanceOfSuccess()) {
                    player.setRealmId(nextRealmAndStage.realmId());
                    player.setRealmStage(nextRealmAndStage.stageId());

                    playerRepository.save(player);
                    redisService.delete(cacheKey);
                    return ServiceResult.success("Breakthrough successfully completed!");
                } else {
                    playerRepository.save(player);
                    redisService.delete(cacheKey);
                    return ServiceResult.failure("Breakthrough failed!");
                }
            }

        } catch (Exception ex) {
            return ServiceResult.failure("An error occurred during breakthrough: " + ex.getMessage());
        }
    }

    public ServiceResult<PlayerDTO> createNewPlayable(NewPlayableDTO dto) {
        try {
            if (dto == null) {
                return ServiceResult.failure("Playable character data is required");
            }

            if (dto.accountId() == null) {
                return ServiceResult.failure("Account id is invalid");
            }

            var account = accountRepository.findById(dto.accountId());
            if (account.isEmpty()) {
                return ServiceResult.failure("Account not found");
            }

            var player = new Player();
            player.setAccount(account.get());
            player.setGender(dto.gender());
            player.setRealmId(RealmEnum.LUYEN_KHI.getValue());
            player.setRealmStage(1);

            var savedPlayer = playerRepository.save(player);

            var defaultMap = mapRepository.findById(1);

            if(defaultMap.isEmpty()) {
                return ServiceResult.failure("Internal server error");
            }

            //set default location
            var playerLocation = new PlayerLocation();
            playerLocation.setPlayer(player);
            playerLocation.setX(0);
            playerLocation.setY(0);
            playerLocation.setCurrentMap(defaultMap.get());
            playerLocationRepository.save(playerLocation);

            return ServiceResult.success("Playable character created successfully", toDto(savedPlayer));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating playable character", ex);
        }
    }

    public ServiceResult<PlayerDTO> updatePlayer(UUID id, PlayerDTO dto) {
        try {
            if (id == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            var playerOptional = playerRepository.findById(id);
            if (playerOptional.isEmpty()) {
                return ServiceResult.failure("Player not found");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            var player = playerOptional.get();
            player.setAccount(referencesResult.getData().account());
            applyDto(player, dto);

            var updatedPlayer = playerRepository.save(player);
            return ServiceResult.success("Player updated successfully", toDto(updatedPlayer));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating player", ex);
        }
    }

    private void applyDto(Player player, PlayerDTO dto) {
        player.setRealmId(dto.realmId());
        player.setGender(dto.gender());
        player.setRollNum(defaultInteger(dto.rollNum(), 5));
        player.setRealmStage(dto.realmStage());
        player.setHp(defaultLong(dto.hp(), 100L));
        player.setAttack(defaultLong(dto.attack(), 10L));
        player.setDefense(defaultLong(dto.defense(), 5L));
        player.setCritRate(defaultDouble(dto.critRate(), 0D));
        player.setCritDamage(defaultDouble(dto.critDamage(), 150D));
        player.setSpeed(defaultDouble(dto.speed(), 1D));
        player.setLifeSteal(defaultDouble(dto.lifeSteal(), 0D));
        player.setCultivationSpeed(defaultDouble(dto.cultivationSpeed(), 1D));
        player.setCultivationPoint(defaultLong(dto.cultivationPoint(), 0L));
        player.setReputation(defaultLong(dto.reputation(), 0L));
        player.setSpiritStone(defaultLong(dto.spiritStone(), 0L));
    }

    private PlayerDTO toDto(Player player) {
        var realm = cultivationRealmService.getCultivationRealmEntityById(player.getRealmId()).getData();
        var realmStage = realmStageService.getRealmStageEntityById(player.getRealmId()).getData();
        var account = player.getAccount();

        return new PlayerDTO(
                player.getId(),
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                player.getRealmId(),
                realm != null ? realm.getName() : null,
                player.getGender(),
                player.getRollNum(),
                player.getRealmStage(),
                realmStage != null ? realmStage.getStageName() + "(" + realmStage.getStageLevel() + ")" : null,
                player.getHp(),
                player.getAttack(),
                player.getDefense(),
                player.getCritRate(),
                player.getCritDamage(),
                player.getSpeed(),
                player.getLifeSteal(),
                player.getCultivationSpeed(),
                player.getCultivationPoint(),
                player.getReputation(),
                player.getSpiritStone(),
                player.getCreatedAt()
        );
    }

    private ServiceResult<PlayerReferences> resolveReferences(PlayerDTO dto) {
        var accountOptional = accountRepository.findById(dto.accountId());
        if (accountOptional.isEmpty()) {
            return ServiceResult.failure("Account not found");
        }

        if (!cultivationRealmRepository.existsById(dto.realmId())) {
            return ServiceResult.failure("Realm not found");
        }

        if (!realmStageRepository.existsByStageLevel(dto.realmStage())) {
            return ServiceResult.failure("Realm stage not found");
        }

        return ServiceResult.success("Player references resolved", new PlayerReferences(accountOptional.get()));
    }

    private Long defaultLong(Long value, Long defaultValue) {
        return value != null ? value : defaultValue;
    }

    private Double defaultDouble(Double value, Double defaultValue) {
        return value != null ? value : defaultValue;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value != null ? value : defaultValue;
    }

    private boolean isNegative(Number value) {
        return value != null && value.doubleValue() < 0;
    }

    private record PlayerReferences(Account account) {
    }

    private record RealmAndStageId(Integer realmId, Integer stageId) {
    }
}
