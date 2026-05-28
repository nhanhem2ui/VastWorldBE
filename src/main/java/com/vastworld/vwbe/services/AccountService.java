package com.vastworld.vwbe.services;

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

    public List<Account> GetAllAccounts() {
        return accountRepository.findAll();
    }

}