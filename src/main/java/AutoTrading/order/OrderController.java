package AutoTrading.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders")
    public ResponseEntity postOrders(@RequestParam String market, @RequestParam String side,
                                     @RequestParam String volume, @RequestParam String price,
                                     @RequestParam String ord_type) throws IOException, NoSuchAlgorithmException {

        orderService.order(market, side, volume, price, ord_type);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/autoTrade/{minute}")
    public ResponseEntity autoTrade(@PathVariable int minute) {
        orderService.autoTrade(minute);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/possibleOrder")
    public ResponseEntity getPossibleOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        BalanceDto balanceDto = orderService.getPossibleOrder();

        return new ResponseEntity<>(balanceDto, HttpStatus.OK);
    }

    @GetMapping("/api/v1/checkOrder")
    public ResponseEntity getOrderState() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        OrderStateDto orderStateDto = orderService.checkOrder();

        return new ResponseEntity<>(orderStateDto, HttpStatus.OK);
    }

    @DeleteMapping("/api/v1/cancelOrder")
    public ResponseEntity deleteOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        orderService.cancelOrder();

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
