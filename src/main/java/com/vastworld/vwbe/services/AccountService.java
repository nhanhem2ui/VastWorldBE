package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ServiceResult<List<Account>> GetAllAccounts() {
        try {
            var data = accountRepository.findAll();
            if (!data.isEmpty()) {
                return ServiceResult.success("Account retrieving successfully", data);
            }
            return ServiceResult.failure("No accounts in system");
        } catch (Exception e) {
            return ServiceResult.failure("Error retrieving accounts", e);
        }
    }
}