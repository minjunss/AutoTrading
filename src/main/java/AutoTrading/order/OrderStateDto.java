package AutoTrading.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderStateDto {
    private String side;
    private String price;
    private String state;
    private String market;

}
