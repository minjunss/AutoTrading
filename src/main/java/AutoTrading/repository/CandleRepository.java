package AutoTrading.repository;

import AutoTrading.entity.Candle;
import org.springframework.data.repository.CrudRepository;

public interface CandleRepository extends CrudRepository<Candle, String> {
}
