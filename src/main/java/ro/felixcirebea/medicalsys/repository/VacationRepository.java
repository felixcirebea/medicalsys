package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRepository extends CrudRepository<VacationEntity, Long> {

    List<VacationEntity> findAllByDoctorAndStartDateAfter(DoctorEntity doctorEntity, LocalDate startDate);

    List<VacationEntity> findAllByDoctorAndEndDateBefore(DoctorEntity doctorEntity, LocalDate endDate);

    List<VacationEntity> findAllByDoctorAndStartDateAfterAndEndDateBefore(
            DoctorEntity doctorEntity, LocalDate startDate, LocalDate endDate);

    List<VacationEntity> findAllByDoctorAndType(DoctorEntity doctorEntity, VacationType type);

    List<VacationEntity> findAllByType(VacationType type);

    @Query("SELECT COUNT(v) > 0 FROM vacations v WHERE v.doctor = :doctor AND :date BETWEEN v.startDate AND v.endDate")
    Boolean isDateBetweenVacation(@Param("doctor") DoctorEntity doctor, @Param("date") LocalDate date);

    @Transactional
    void deleteByDoctorAndStartDate(DoctorEntity doctor, LocalDate startDate);
}
