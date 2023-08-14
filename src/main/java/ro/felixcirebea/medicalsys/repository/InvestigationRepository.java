package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationRepository extends CrudRepository<InvestigationEntity, Long> {
    Optional<InvestigationEntity> findByIdAndIsActive(Long id, boolean isActive);

    Optional<InvestigationEntity> findByNameAndIsActive(String name, boolean isActive);

    List<InvestigationEntity> findAllByDurationAndIsActive(Integer duration, boolean isActive);

    List<InvestigationEntity> findAllByIsActive(boolean isActive);

}
