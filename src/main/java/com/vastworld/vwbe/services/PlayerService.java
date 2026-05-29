package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
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
    private final CultivationRealmRepository cultivationRealmRepository;
    private final RealmStageRepository realmStageRepository;

    public PlayerService(
            PlayerRepository playerRepository,
            AccountRepository accountRepository,
            CultivationRealmRepository cultivationRealmRepository,
            RealmStageRepository realmStageRepository) {
        this.playerRepository = playerRepository;
        this.accountRepository = accountRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.realmStageRepository = realmStageRepository;
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
        try {
            if (id == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            var player = playerRepository.findById(id);
            if (player.isEmpty()) {
                return ServiceResult.failure("Player not found");
            }

            return ServiceResult.success("Player retrieved successfully", toDto(player.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player", ex);
        }
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

    public ServiceResult<Void> deletePlayer(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Player id is invalid");
            }

            if (!playerRepository.existsById(id)) {
                return ServiceResult.failure("Player not found");
            }

            playerRepository.deleteById(id);
            return ServiceResult.success("Player deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting player", ex);
        }
    }

    private void applyDto(Player player, PlayerDTO dto) {
        player.setRealmId(dto.realmId());
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
                || isNegative(dto.spiritStone())) {
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

    private boolean isNegative(Number value) {
        return value != null && value.doubleValue() < 0;
    }

    private record PlayerReferences(Account account) {
    }
}
