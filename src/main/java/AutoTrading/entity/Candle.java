package AutoTrading.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@Builder
@RedisHash(value = "candle")
public class Candle {

    @Id
    private Long candleId;
    private String market;
    private Integer tradePrice;
    private Integer highPrice;
    private Integer lowPrice;
    private String candleDateTimeKst;
}
