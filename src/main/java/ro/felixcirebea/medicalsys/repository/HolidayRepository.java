package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HolidayRepository extends CrudRepository<HolidayEntity, Long> {

    Optional<HolidayEntity> findByDescription(String description);

    @Query("SELECT COUNT(h) > 0 FROM holidays h WHERE :date BETWEEN h.startDate AND h.endDate")
    Boolean isDateBetweenHolidays(@Param("date") LocalDate date);



}
