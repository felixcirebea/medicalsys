package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends CrudRepository<DoctorEntity, Long> {

    Optional<DoctorEntity> findByNameAndIsActive(String name, boolean isActive);

    Optional<DoctorEntity> findByIdAndIsActive(Long id, boolean isActive);

    List<DoctorEntity> findAllByIsActive(boolean isActive);

}
