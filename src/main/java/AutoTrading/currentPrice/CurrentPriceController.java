package com.example.UbitAutoTrading.currentPrice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class CurrentPriceController {

    private final CurrentPriceService currentPriceService;

    @GetMapping("/api/v1/ticker")
    public ResponseEntity getCurrentPrice(@RequestParam String markets) throws IOException {

        String s = currentPriceService.viewCurrentPrice(markets);

        return new ResponseEntity<>(s, HttpStatus.OK);
    }
}
