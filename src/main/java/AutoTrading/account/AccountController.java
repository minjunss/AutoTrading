package com.example.UbitAutoTrading.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

   @GetMapping("/api/v1/accounts")
    public ResponseEntity<?> showAllAccounts() {
       List<AccountResponse> accounts = accountService.getAccounts();
       log.info("내 계좌 조회");
       return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

}