package AutoTrading.controller;

import AutoTrading.service.AccountService;
import AutoTrading.dto.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

   @GetMapping("/accounts")
    public ResponseEntity<?> showAllAccounts() throws IOException {
       List<AccountResponseDto> accounts = accountService.getAccounts();
       log.info("내 계좌 조회");
       log.info("매수평균가: {}", accounts.get(0).getAvgBuyPrice());
       return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

}