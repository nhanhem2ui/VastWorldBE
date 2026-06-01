package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.account.AccountDTO;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ServiceResult<List<AccountDTO>> getAllAccounts() {
        try {
            var accountList = accountRepository.findAll();
            if (accountList.isEmpty()) {
                return ServiceResult.failure("No account found");
            }

            var dtoList = accountList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Account retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving account", ex);
        }
    }

    public ServiceResult<AccountDTO> getAccountById(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Account id is invalid");
            }

            var account = accountRepository.findById(id);
            if (account.isEmpty()) {
                return ServiceResult.failure("Account not found");
            }

            return ServiceResult.success("Account retrieved successfully", toDto(account.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving account", ex);
        }
    }

    public ServiceResult<AccountDTO> createAccount(AccountDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (accountRepository.existsByEmailIgnoreCase(dto.email().trim())) {
                return ServiceResult.failure("Email already exists");
            }

            if (accountRepository.existsByUsernameIgnoreCase(dto.username().trim())) {
                return ServiceResult.failure("Username already exists");
            }

            Account account = new Account();
            applyDto(account, dto);

            var savedAccount = accountRepository.save(account);
            return ServiceResult.success("Account created successfully", toDto(savedAccount));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating account", ex);
        }
    }

    public ServiceResult<AccountDTO> updateAccount(UUID id, AccountDTO dto) {
        try {
            if (id == null) {
                return ServiceResult.failure("Account id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var accountOptional = accountRepository.findById(id);
            if (accountOptional.isEmpty()) {
                return ServiceResult.failure("Account not found");
            }

            if (accountRepository.existsByEmailIgnoreCaseAndIdNot(dto.email().trim(), id)) {
                return ServiceResult.failure("Email already exists");
            }

            if (accountRepository.existsByUsernameIgnoreCaseAndIdNot(dto.username().trim(), id)) {
                return ServiceResult.failure("Username already exists");
            }

            var account = accountOptional.get();
            applyDto(account, dto);

            var updatedAccount = accountRepository.save(account);
            return ServiceResult.success("Account updated successfully", toDto(updatedAccount));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating account", ex);
        }
    }

    public ServiceResult<Void> deleteAccount(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Account id is invalid");
            }

            if (!accountRepository.existsById(id)) {
                return ServiceResult.failure("Account not found");
            }

            accountRepository.deleteById(id);
            return ServiceResult.success("Account deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting account", ex);
        }
    }

    private void applyDto(Account account, AccountDTO dto) {
        account.setEmail(dto.email().trim());
        account.setUsername(dto.username().trim());
        account.setRole(dto.role() != null && !dto.role().trim().isEmpty() ? dto.role().trim() : account.getRole());
        account.setAuthProvider(dto.authProvider() != null && !dto.authProvider().trim().isEmpty() ? dto.authProvider().trim() : account.getAuthProvider());
        account.setProviderId(dto.providerId());
        account.setEmailVerified(Boolean.TRUE.equals(dto.emailVerified()));
        account.setIsBanned(Boolean.TRUE.equals(dto.isBanned()));
        account.setLastLoginAt(dto.lastLoginAt());
    }

    private AccountDTO toDto(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                account.getRole(),
                account.getAuthProvider(),
                account.getProviderId(),
                account.getEmailVerified(),
                account.getIsBanned(),
                account.getLastLoginAt(),
                account.getCreatedAt()
        );
    }

    private ServiceResult<AccountDTO> validateDto(AccountDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Account data is required");
        }

        if (dto.email() == null || dto.email().trim().isEmpty()) {
            return ServiceResult.failure("Email is required");
        }

        if (dto.username() == null || dto.username().trim().isEmpty()) {
            return ServiceResult.failure("Username is required");
        }

        return null;
    }
}
