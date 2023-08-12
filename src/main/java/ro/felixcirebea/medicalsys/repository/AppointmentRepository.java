package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.felixcirebea.medicalsys.entity.AppointmentEntity;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends CrudRepository<AppointmentEntity, Long> {

    List<AppointmentEntity> findAllByDoctorAndDate(DoctorEntity doctor, LocalDate date);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM appointments a WHERE a.doctor = :doctor " +
            "AND a.date = :date " +
            "AND a.startTime < :endTime " +
            "AND a.endTime > :startTime")
    Boolean existsByDoctorDateAndTimeRange(@Param("doctor") DoctorEntity doctor,
                                           @Param("date") LocalDate date,
                                           @Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime);

    Boolean existsByIdAndClientName(Long id, String clientName);

    @Transactional
    void deleteByIdAndClientName(Long id, String clientName);

}
