package AutoTrading.entity;

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
@RedisHash(value = "uuid")
public class Uuid {
    @Id
    private String id;
    private String uuid;
}
