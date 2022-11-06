package AutoTrading.dto;

import lombok.Getter;

@Getter
public class AutoTradeDto {
    private String indicator;
    private int minute;
    private int low;
    private int high;
}
