package ro.felixcirebea.medicalsys.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationRepository extends CrudRepository<InvestigationEntity, Long> {

    Optional<InvestigationEntity> findByName(String name);

    List<InvestigationEntity> findAllByDuration(Integer duration);

}
