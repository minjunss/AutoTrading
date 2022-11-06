package AutoTrading.repository;

import AutoTrading.entity.Uuid;
import org.springframework.data.repository.CrudRepository;

public interface UuidRepository extends CrudRepository<Uuid, String> {
}
