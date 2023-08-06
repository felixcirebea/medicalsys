package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.VacationEntity;

@Repository
public interface VacationRepository extends CrudRepository<VacationEntity, Long> {

}
