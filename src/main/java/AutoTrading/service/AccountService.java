package AutoTrading.service;

import AutoTrading.api.OpenApi;
import AutoTrading.dto.AccountResponseDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AccountService {
    private final OpenApi openApi;

    public List<AccountResponseDto> getAccounts() {

        List<AccountResponseDto> accounts = openApi.getAccounts();

        return accounts;
    }
}
