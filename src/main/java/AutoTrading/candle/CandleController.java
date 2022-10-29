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

    @GetMapping("/api/v1/candles/minutes/{unit}")
    public ResponseEntity getMinuteCandleRSI (@PathVariable int unit) {

        double RSI = candleService.viewMinuteCandleRSI(unit);

        return new ResponseEntity<>(RSI, HttpStatus.OK);
    }
}
