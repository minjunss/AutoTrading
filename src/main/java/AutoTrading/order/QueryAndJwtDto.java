package AutoTrading.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueryAndJwtDto {
    private String query;
    private String jwtToken;
}
