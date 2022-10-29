package com.example.UbitAutoTrading.candle;

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
@RedisHash(value = "candle")
public class Candle {

    @Id
    private Long candleId;
    private String market;
    private Double tradePrice;


}
