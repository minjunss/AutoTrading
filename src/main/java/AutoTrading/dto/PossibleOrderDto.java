package AutoTrading.dto;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
public class PossibleOrderDto {

    private JsonObject askAccount;
    private JsonObject bidAccount;
}
