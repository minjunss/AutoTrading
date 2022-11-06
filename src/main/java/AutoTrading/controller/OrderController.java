package AutoTrading.controller;

import AutoTrading.dto.AutoTradeDto;
import AutoTrading.dto.BalanceDto;
import AutoTrading.service.OrderService;
import AutoTrading.dto.OrderStateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity postOrders(@RequestParam String market, @RequestParam String side,
                                     @RequestParam String volume, @RequestParam String price,
                                     @RequestParam String ord_type) throws IOException, NoSuchAlgorithmException {

        orderService.order(market, side, volume, price, ord_type);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/autoTrade/{market}")
    public ResponseEntity autoTrade(@PathVariable String market, @RequestBody AutoTradeDto autoTradeDto) {
        orderService.autoTrade(market, autoTradeDto.getIndicator(), autoTradeDto.getMinute(), autoTradeDto.getLow(), autoTradeDto.getHigh());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/possibleOrder")
    public ResponseEntity getPossibleOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        BalanceDto balanceDto = orderService.getPossibleOrder();

        return new ResponseEntity<>(balanceDto, HttpStatus.OK);
    }

    @GetMapping("/checkOrder")
    public ResponseEntity getOrderState() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        OrderStateDto orderStateDto = orderService.checkOrder();

        return new ResponseEntity<>(orderStateDto, HttpStatus.OK);
    }

    @DeleteMapping("/cancelOrder")
    public ResponseEntity deleteOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        orderService.cancelOrder();

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
