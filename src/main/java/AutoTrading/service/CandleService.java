package AutoTrading.service;

import AutoTrading.api.OpenApi;
import AutoTrading.repository.CandleRepository;
import AutoTrading.entity.Candle;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CandleService {

    private final CandleRepository candleRepository;
    private final OpenApi openApi;
    private final Gson gson;


    public double viewMinuteCandleRSIorCCI(String market, String indicator, int unit) {

        try {
            String result = openApi.getMinuteCandle(market, indicator, unit);

            saveCandles(result);
            if(indicator.equalsIgnoreCase("rsi")) {

                return getRSI();
            }
            else if(indicator.equalsIgnoreCase("cci")) {

                return getCCI();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public double viewPresentPrice() {
        try {
            String result = openApi.getPresentPrice();

            JsonArray jsonElements = gson.fromJson(result, JsonArray.class);
            JsonObject obj = jsonElements.get(0).getAsJsonObject();

            return obj.get("trade_price").getAsDouble();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getRSI() {
        List<Candle> candleList = getCandleList();

        double AU = 0;
        double AD = 0;
        double temp = 0;

        List<Double> auList = new ArrayList<>();
        List<Double> adList = new ArrayList<>();
        List<Double> realAuList = new ArrayList<>();
        List<Double> realAdList = new ArrayList<>();

        int i = 1;
        for (Candle candle : candleList) {
            int currentPrice = candle.getTradePrice();
            if (i >= 2 && i <= 14) {
                if (currentPrice >= temp) {
                    auList.add(currentPrice - temp);
                    adList.add(0.0);
                } else {
                    auList.add(0.0);
                    adList.add(temp - currentPrice);
                }
                if(i == 14) {
                    AU = auList.stream().mapToDouble(s -> s).sum() / 14;
                    AD = adList.stream().mapToDouble(s -> s).sum() / 14;
                }
            }
            if (i == 15) {
                realAuList.add(AU);
                realAdList.add(AD);
            }
            if (i >= 16) {
                if (currentPrice >= temp) {
                    AU = ((13 * AU) + (currentPrice - temp)) / 14;
                    AD = (13 * AD) / 14;
                } else {
                    AU = (13 * AU) / 14;
                    AD = ((13 * AD) + (temp - currentPrice)) / 14;
                }
                realAuList.add(AU);
                realAdList.add(AD);
            }
            temp = currentPrice;
            i++;
        }
        double RS = realAuList.get(realAuList.size() - 1) / realAdList.get(realAdList.size() - 1);
        double RSI = (RS / (1 + RS)) * 100;

        candleRepository.deleteAll();

        log.info("RSI: {}", RSI);

        return RSI;
    }

    private double getCCI() {
        List<Candle> candleList = getCandleList();
        double TP = 0;
        double SMA;
        double MAD;
        double CV = 0.015;
        List<Double> TPList = new ArrayList<>();
        List<Double> MADList = new ArrayList<>();


        for (Candle candle : candleList) {
            TP = (candle.getHighPrice() + candle.getLowPrice() + candle.getTradePrice()) / 3.0;
            TPList.add(TP);
        }
        DoubleSummaryStatistics tpStats = TPList.stream().mapToDouble(Double::doubleValue).summaryStatistics();
        SMA = tpStats.getAverage();

        for (Double tp : TPList) {
            MADList.add(Math.abs(tp - SMA));
        }
        DoubleSummaryStatistics madStats = MADList.stream().mapToDouble(Double::doubleValue).summaryStatistics();
        MAD = madStats.getAverage();
        double CCI = (TP - SMA) / (CV * MAD);

        candleRepository.deleteAll();
        log.info("CCI: {}", CCI);

        return CCI;
    }

    private List<Candle> getCandleList() {
        Iterable<Candle> candles = candleRepository.findAll();
        List<Candle> candleList = new ArrayList<>();


        for (Candle candle : candles) {
            candleList.add(candle);
        }
        candleList.sort(new Comparator<Candle>() {
            @Override
            public int compare(Candle o1, Candle o2) {
                String o1Time = o1.getCandleDateTimeKst().replace("-", "");
                o1Time = o1Time.replace("T", "");
                o1Time = o1Time.replace(":", "");
                String o2Time = o2.getCandleDateTimeKst().replace("-", "");
                o2Time = o2Time.replace("T", "");
                o2Time = o2Time.replace(":", "");
                if (Long.parseLong(o1Time) < Long.parseLong(o2Time)) return -1;
                else if (Long.parseLong(o1Time) > Long.parseLong(o2Time)) return 1;
                return 0;
            }
        });
        return candleList;
    }

    private void saveCandles(String result) {
        JsonArray obj = gson.fromJson(result, JsonArray.class);


        for (JsonElement jsonElement : obj) {
            JsonObject temp = jsonElement.getAsJsonObject();

            Candle candle = Candle.builder()
                    .market(temp.get("market").getAsString())
                    .tradePrice(temp.get("trade_price").getAsInt())
                    .highPrice(temp.get("high_price").getAsInt())
                    .lowPrice(temp.get("low_price").getAsInt())
                    .candleDateTimeKst(temp.get("candle_date_time_kst").getAsString())
                    .build();

            candleRepository.save(candle);
        }
    }

}
