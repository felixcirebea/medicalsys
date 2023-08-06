package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;

@Repository
public interface HolidayRepository extends CrudRepository<HolidayEntity, Long> {

}
