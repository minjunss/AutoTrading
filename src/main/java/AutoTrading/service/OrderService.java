package AutoTrading.service;

import AutoTrading.api.OpenApi;
import AutoTrading.dto.BalanceDto;
import AutoTrading.entity.AccountBalance;
import AutoTrading.dto.AccountResponseDto;
import AutoTrading.dto.OrderStateDto;
import AutoTrading.dto.PossibleOrderDto;
import AutoTrading.exception.BusinessException;
import AutoTrading.exception.ExceptionCode;
import AutoTrading.jwt.JwtTokenProvider;
import AutoTrading.paths.UpbitApiPaths;
import AutoTrading.entity.Uuid;
import AutoTrading.repository.UuidRepository;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class OrderService {
    private final CandleService candleService;
    private final UuidRepository uuidRepository;
    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Gson gson;
    private final OpenApi openApi;
    public static final String ORDER_URL = UpbitApiPaths.BASE_SERVER_URL + "/orders";
    @Value("${security.access-key}")
    private String accessKey;
    @Value("${security.secret-key}")
    private String secretKey;

    public void order(String market, String side,
                      String volume, String price, String ord_type) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            String result = openApi.order(market, side, volume, price, ord_type);

            JsonObject obj = gson.fromJson(result, JsonObject.class);

            Uuid uuid = Uuid.builder()
                    .uuid(obj.get("uuid").getAsString())
                    .id("id")
                    .build();

            uuidRepository.save(uuid);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public BalanceDto getPossibleOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            String result = openApi.possibleOrder();

            BalanceDto balanceDto = viewAccountBalance(result);

            log.info("판매가능수량: {}, 구매가능금액: {}", balanceDto.getAskBalance(), balanceDto.getBidBalance());

            return balanceDto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        HashMap<String, String> params = getUuidParams();

        try {
            openApi.cancelOrder(params);
            uuidRepository.deleteAll();
            log.info("주문취소");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OrderStateDto checkOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        HashMap<String, String> params = getUuidParams();

        try {
            String result = openApi.checkOrder(params);

            JsonArray objArr = gson.fromJson(result, JsonArray.class);
            if (!objArr.isEmpty()) {
                JsonElement jsonElement = objArr.get(0);

                JsonObject obj = jsonElement.getAsJsonObject();

                OrderStateDto orderStateDto = OrderStateDto.builder()
                        .side(obj.get("side").getAsString())
                        .market(obj.get("market").getAsString())
                        .price(obj.get("price").getAsString())
                        .state(obj.get("state").getAsString()).build();

                if (!orderStateDto.getState().equals("done")) {
                    cancelOrder();
                }
                return orderStateDto;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BalanceDto viewAccountBalance(String result) {

        JsonObject obj = gson.fromJson(result, JsonObject.class);

        PossibleOrderDto possibleOrderDto = PossibleOrderDto.builder()
                .askAccount(obj.get("ask_account").getAsJsonObject())
                .bidAccount(obj.get("bid_account").getAsJsonObject())
                .build();

        AccountBalance accountBalance = AccountBalance.builder()
                .askBalance(possibleOrderDto.getAskAccount().get("balance").getAsString())
                .bidBalance(possibleOrderDto.getBidAccount().get("balance").getAsString())
                .build();

        String askBalance = accountBalance.getAskBalance();
        String bidBalance = accountBalance.getBidBalance();

        BalanceDto balanceDto = new BalanceDto(askBalance, bidBalance);

        return balanceDto;
    }

    @Async
    public void autoTrade(String market, String indicator, int minute, int low, int high) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkLoss();
                double value = candleService.viewMinuteCandleRSIorCCI(market, indicator, minute);

                if (value <= low) {
                    try {
                        buyOrSell("buy");
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    orderStateDoneCheck30sec();
                }

                if (value >= high) {
                    try {
                        buyOrSell("sell");
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    orderStateDoneCheck30sec();
                }
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    private HashMap<String, String> getUuidParams() {
        HashMap<String, String> params = new HashMap<>();
        Uuid findUuid = uuidRepository.findById("id").orElseThrow(
                () -> new BusinessException(ExceptionCode.NOT_EXIST_UUID));

        String uuid = findUuid.getUuid();

        params.put("uuid", uuid);
        return params;
    }

    @Async
    public void orderStateDoneCheck30sec() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                if (count++ < 1) {
                    try {
                        checkOrder();
                    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                } else timer.cancel();
            }
        };
        timer.schedule(timerTask, 30000);
    }

    public void checkLoss() {
        List<AccountResponseDto> accounts = accountService.getAccounts();
        double avgBuyPrice = Double.parseDouble(accounts.get(0).getAvgBuyPrice());
        double presentPrice = candleService.viewPresentPrice();
        double loss = (presentPrice - avgBuyPrice) / avgBuyPrice;

        if (loss < -0.03) {
            try {
                BalanceDto balanceDto = getPossibleOrder();
                if (Integer.parseInt(balanceDto.getAskBalance()) > 0) {
                    order("KRW-BTC", "ASK", balanceDto.getAskBalance(), "", "market");
                    log.info("판매신청완료");
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            orderStateDoneCheck30sec();
        }
    }

    private void buyOrSell(String order) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if(order.equalsIgnoreCase("buy")) {
            BalanceDto balanceDto = getPossibleOrder();
            if (Integer.parseInt(balanceDto.getBidBalance()) >= 5000) {
                order("KRW-BTC", "BID", "", balanceDto.getBidBalance(), "price");
                log.info("구매신청완료");
            }
        } else if (order.equalsIgnoreCase("sell")) {
            BalanceDto balanceDto = getPossibleOrder();
            if (Integer.parseInt(balanceDto.getAskBalance()) > 0) {
                order("KRW-BTC", "ASK", balanceDto.getAskBalance(), "", "market");
                log.info("판매신청완료");
            }
        }
    }
}

