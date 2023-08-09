package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends CrudRepository<WorkingHoursEntity, Long> {

    Boolean existsByDoctorAndDayOfWeek(DoctorEntity doctor, DayOfWeek dayOfWeek);

    Optional<WorkingHoursEntity> findByDoctorAndDayOfWeek(DoctorEntity doctor, DayOfWeek dayOfWeek);

    List<WorkingHoursEntity> findByDoctor(DoctorEntity doctor);

    List<WorkingHoursEntity> findByDayOfWeek(DayOfWeek dayOfWeek);

    @Transactional
    void deleteByDoctor(DoctorEntity doctor);

    @Transactional
    void deleteByDoctorAndDayOfWeek(DoctorEntity doctor, DayOfWeek dayOfWeek);

}
