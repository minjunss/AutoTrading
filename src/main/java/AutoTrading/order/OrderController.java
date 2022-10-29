package AutoTrading.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/api/v1/autoTrade")
    public ResponseEntity autoTrade() {

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/possibleOrder")
    public ResponseEntity getPossibleOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String possibleOrder = orderService.getPossibleOrder();

        return new ResponseEntity<>(possibleOrder, HttpStatus.OK);
    }


}
