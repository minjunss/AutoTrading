package AutoTrading.order;

import AutoTrading.account.Account;
import com.google.gson.JsonObject;
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
@RedisHash(value = "possibleOrder")
public class PossibleOrder {

    @Id
    private Long id;
    private JsonObject askAccount;
    private JsonObject bidAccount;
}
