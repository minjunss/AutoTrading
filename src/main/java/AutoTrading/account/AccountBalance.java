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
@RedisHash(value = "accountBalance")
public class AccountBalance {
    @Id
    private Long id;
    private String askBalance;
    private String bidBalance;
}
