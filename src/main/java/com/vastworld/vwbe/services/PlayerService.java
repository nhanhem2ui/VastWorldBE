package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.common.RealmEnum;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.player.GetPlayerNextBreakthroughResponse;
import com.vastworld.vwbe.dto.player.NewPlayableDTO;
import com.vastworld.vwbe.dto.player.PlayerDTO;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.repositories.AccountRepository;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import com.vastworld.vwbe.repositories.RealmStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final AccountRepository accountRepository;
    private final CultivationRealmService cultivationRealmService;
    private final CultivationRealmRepository cultivationRealmRepository;
    private final RealmStageService realmStageService;
    private final RealmStageRepository realmStageRepository;
    private final RedisService redisService;

    public PlayerService(PlayerRepository playerRepository, AccountRepository accountRepository,
                         CultivationRealmRepository cultivationRealmRepository, RealmStageRepository realmStageRepository,
                         RedisService redisService, CultivationRealmService cultivationRealmService,
                         RealmStageService realmStageService) {
        this.playerRepository = playerRepository;
        this.accountRepository = accountRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.realmStageRepository = realmStageRepository;
        this.redisService = redisService;
        this.cultivationRealmService = cultivationRealmService;
        this.realmStageService = realmStageService;
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
        var player = getPlayerEntityById(id);

        if (!player.isSuccess()) {
            return ServiceResult.failure(player.getMessage());
        }

        return ServiceResult.success("Player retrieved successfully", toDto(player.getData()));
    }

    public ServiceResult<Player> getPlayerEntityById(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            var cacheKey = CacheKeys.players("entity", id);
            var cached = redisService.get(cacheKey, new TypeReference<Player>() {});

            if (cached != null) {
                return ServiceResult.success("Player retrieved successfully", cached);
            }

            var player = playerRepository.findById(id);
            return player.map(
                    value -> ServiceResult.success("Player retrieved successfully", value))
                    .orElseGet(() -> ServiceResult.failure("Player not found"));

        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player", ex);
        }
    }

    public ServiceResult<GetPlayerNextBreakthroughResponse> getPlayerNextBreakthrough(UUID playerId) {
        if (playerId == null) {
            return ServiceResult.failure("PlayerId not found");
        }

        var playerResult = getPlayerEntityById(playerId);

        if (!playerResult.isSuccess()) {
            return ServiceResult.failure(playerResult.getMessage());
        }

        var player = playerResult.getData();

        if (player.getRealmId() >= RealmEnum.values().length
                && player.getRealmStage() >= GameBalance.STAGES_PER_REALM) {

            return ServiceResult.failure("Player has reached the maximum realm");
        }

        int nextRealmId = GameBalance.getNextRealmId(player.getRealmId(), player.getRealmStage());
        int nextStageId = GameBalance.getNextStageId(player.getRealmStage());

        var isTribulation = player.getRealmStage() == GameBalance.STAGES_PER_REALM;

        long breakthroughPoints = GameBalance.getBreakthroughCpPoints(nextRealmId, nextStageId);

        var realmResult = cultivationRealmService.getCultivationRealmEntityById(nextRealmId);
        var stageResult = realmStageService.getRealmStageEntityById(nextStageId);

        if (!realmResult.isSuccess() || !stageResult.isSuccess()) {
            return ServiceResult.failure("Internal Server error");
        }
        var nextRealmName = realmResult.getData().getName();
        var nextStage = stageResult.getData();

        var nextStageName = nextStage.getStageName() +"(" + nextStage.getStageLevel() + ")";

        var chanceOfSuccess = 1F;

        var response = new GetPlayerNextBreakthroughResponse(
                nextRealmName,
                nextStageName,
                isTribulation,
                breakthroughPoints,
                chanceOfSuccess
        );

        return ServiceResult.success("Next breakthrough retrieved", response);
    }

    public ServiceResult<PlayerDTO> createPlayer(PlayerDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            Player player = new Player();
            player.setAccount(referencesResult.getData().account());
            applyDto(player, dto);

            var savedPlayer = playerRepository.save(player);
            return ServiceResult.success("Player created successfully", toDto(savedPlayer));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating player", ex);
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
            player.setRealmId(1);
            player.setRealmStage(1);

            var savedPlayer = playerRepository.save(player);
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

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
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
        var realm = cultivationRealmRepository.findById(player.getRealmId()).orElse(null);
        var realmStage = realmStageRepository.findByStageLevel(player.getRealmStage()).orElse(null);
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
                realmStage != null ? realmStage.getStageName() : null,
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

    private ServiceResult<PlayerDTO> validateDto(PlayerDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Player data is required");
        }

        if (dto.accountId() == null) {
            return ServiceResult.failure("Account id is invalid");
        }

        if (dto.realmId() == null || dto.realmId() <= 0) {
            return ServiceResult.failure("Realm id is invalid");
        }

        if (dto.realmStage() == null || dto.realmStage() <= 0) {
            return ServiceResult.failure("Realm stage is invalid");
        }

        if (isNegative(dto.hp())
                || isNegative(dto.attack())
                || isNegative(dto.defense())
                || isNegative(dto.critRate())
                || isNegative(dto.critDamage())
                || isNegative(dto.speed())
                || isNegative(dto.lifeSteal())
                || isNegative(dto.cultivationSpeed())
                || isNegative(dto.cultivationPoint())
                || isNegative(dto.reputation())
                || isNegative(dto.spiritStone())
                || isNegative(dto.rollNum())) {
            return ServiceResult.failure("Player stats must be greater than or equal to 0");
        }

        return null;
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
}
