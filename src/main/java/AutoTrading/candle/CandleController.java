package AutoTrading.candle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class CandleController {

    private final CandleService candleService;

    // 미완성
    @GetMapping("/api/v1/candles/minutes/rsi/{unit}")
    public ResponseEntity getMinuteCandleRSI(@PathVariable int unit) {

        double RSI = candleService.viewMinuteCandleRSI(unit);

        return new ResponseEntity<>(RSI, HttpStatus.OK);
    }

    @GetMapping("api/v1/candles/minutes/cci/{unit}")
    public ResponseEntity getMinuteCandleCCI(@PathVariable int unit) {

        double CCI = candleService.viewMinuteCandleCCI(unit);

        return new ResponseEntity<>(CCI, HttpStatus.OK);
    }
}
