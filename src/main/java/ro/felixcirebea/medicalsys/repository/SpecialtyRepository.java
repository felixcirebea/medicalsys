package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends CrudRepository<SpecialtyEntity, Long> {

    Optional<SpecialtyEntity> findByNameAndIsActive(String name, boolean isActive);

    Optional<SpecialtyEntity> findByIdAndIsActive(Long id, boolean isActive);

    List<SpecialtyEntity> findAllByIsActive(boolean isActive);

}
