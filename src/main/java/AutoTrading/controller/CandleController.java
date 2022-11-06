package AutoTrading.controller;

import AutoTrading.service.CandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CandleController {

    private final CandleService candleService;

    @GetMapping("/candles/indicator/minutes/{market}")
    public ResponseEntity getMinuteCandleRSI(@PathVariable String market, @RequestParam String indicator, @RequestParam int unit) {

        double result = candleService.viewMinuteCandleRSIorCCI(market, indicator, unit);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/presentPrice")
    public ResponseEntity getPresentPrice() {
        double price = candleService.viewPresentPrice();

        return new ResponseEntity<>(price, HttpStatus.OK);
    }
}
