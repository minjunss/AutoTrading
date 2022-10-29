package AutoTrading.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash(value = "account")
public class Account {
    @Id
    private Long id;
    private String currency;
    private String balance;
    private String locked;
    private String avgBuyPrice;
    private Boolean avgBuyPriceModified;
    private String unitCurrency;

}
