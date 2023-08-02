package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;

import java.util.Optional;

@Repository
public interface DoctorRepository extends CrudRepository<DoctorEntity, Long> {

    Optional<DoctorEntity> findByName(String name);

}
