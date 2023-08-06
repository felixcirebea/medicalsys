package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;

@Repository
public interface WorkingHoursRepository extends CrudRepository<WorkingHoursEntity, Long> {

}
