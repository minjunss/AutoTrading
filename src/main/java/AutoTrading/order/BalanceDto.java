package AutoTrading.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BalanceDto {
    private String askBalance;
    private String bidBalance;
}
