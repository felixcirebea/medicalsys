package ro.felixcirebea.medicalsys.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.entity.BaseEntity;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.service.AppointmentService;
import ro.felixcirebea.medicalsys.service.WorkingHoursService;

import java.util.List;
import java.util.Optional;

@Slf4j
@SuppressWarnings("all")
@Component
public class DeleteUtilityImpl implements DeleteUtility {

    public <T extends BaseEntity> T softDeleteById(Long id,
                                                   Optional<T> entityOptional,
                                                   String failLogMessage,
                                                   String successLogMessage,
                                                   Contributor infoContributor) {
        if (entityOptional.isEmpty()) {
            log.warn(String.format(failLogMessage, id));
            infoContributor.incrementFailedDeleteOperations();
            return null;
        }

        entityOptional.get().setIsActive(false);
        log.info(String.format(successLogMessage, id));
        return entityOptional.get();
    }

    public <T> T softDeleteByField(String field,
                                          Optional<T> entityOptional,
                                          CrudRepository<T, Long> repository,
                                          String failLogMessage,
                                          String successLogMessage,
                                          String exceptionMessage,
                                          Contributor infoContributor)
            throws DataNotFoundException {

        if (entityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(failLogMessage, field));
            throw new DataNotFoundException(String.format(exceptionMessage, field));
        }

        T entity = entityOptional.get();
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).setIsActive(false);
            repository.save(entity);
        } else {
            repository.delete(entity);
        }

        log.info(String.format(successLogMessage, field));
        return entity;
    }

    public void softCascadeDelete(SpecialtyEntity specialtyEntity,
                                         WorkingHoursService workingHoursService,
                                         AppointmentService appointmentService,
                                         DoctorRepository doctorRepository,
                                         InvestigationRepository investigationRepository,
                                         String logSuccessMessage) {
        List<DoctorEntity> doctors = specialtyEntity.getDoctors();
        doctors.forEach(doc -> {
            doc.setIsActive(false);
            doc.getVacation().stream()
                    .filter(vac -> !vac.getStatus().equals(VacationStatus.DONE))
                    .forEach(undoneVac -> undoneVac.setStatus(VacationStatus.CANCELED));
            log.info(workingHoursService.deleteAllWorkingHoursForDoctor(doc));
            log.info(appointmentService.cancelAllAppointmentForDoctor(doc));
        });

        doctorRepository.saveAll(doctors);
        log.info(String.format(logSuccessMessage, doctors));

        List<InvestigationEntity> investigations = specialtyEntity.getInvestigations();
        investigations.forEach(inv -> inv.setIsActive(false));
        investigationRepository.saveAll(investigations);
        log.info(String.format(logSuccessMessage, doctors));
    }

}
