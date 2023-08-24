package ro.felixcirebea.medicalsys.helper;

import org.springframework.data.repository.CrudRepository;
import ro.felixcirebea.medicalsys.entity.BaseEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.service.AppointmentService;
import ro.felixcirebea.medicalsys.service.WorkingHoursService;

import java.util.Optional;

@SuppressWarnings("all")
public interface DeleteUtility {
    <T extends BaseEntity> T softDeleteById(Long id, Optional<T> entityOptional,
                                            String failLogMessage, String successLogMessage,
                                            Contributor infoContributor);

    <T extends BaseEntity> T softDeleteByField(String field,
                            Optional<T> entityOptional,
                            CrudRepository<T, Long> repository,
                            String failLogMessage,
                            String successLogMessage,
                            String exceptionMessage,
                            Contributor infoContributor) throws DataNotFoundException;

    public void softCascadeDelete(SpecialtyEntity specialtyEntity,
                                  WorkingHoursService workingHoursService,
                                  AppointmentService appointmentService,
                                  DoctorRepository doctorRepository,
                                  InvestigationRepository investigationRepository,
                                  String logSuccessMessage);
}
