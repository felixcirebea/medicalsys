package ro.felixcirebea.medicalsys.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.service.AppointmentService;
import ro.felixcirebea.medicalsys.service.WorkingHoursService;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.InvestigationUtil;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;
import ro.felixcirebea.medicalsys.util.VacationUtil;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteUtilityTests {

    public static final String FIELD = "TestSpecialty";
    private static final Long ID = 1L;

    private static final String FAIL_MESSAGE = "Fail";

    private static final String SUCCESS_MESSAGE = "Success";

    private static final String EXCEPTION_MESSAGE = "Exception";
    public static final String DELETE_WORKING_HOURS_MSG = "Working hours for %s deleted";
    public static final String CANCEL_APPOINTMENTS_MSG = "Appointments for %s canceled";

    private SpecialtyEntity specialtyEntity;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private Contributor infoContributor;

    @Mock
    private WorkingHoursService workingHoursService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private InvestigationRepository investigationRepository;

    @InjectMocks
    private DeleteUtilityImpl utility;

    @BeforeEach
    public void setUp() {
        specialtyEntity = SpecialtyUtil.createSpecialtyEntity(ID);
    }

    @Test
    public void testSofDeleteById_whenOptionalNotEmpty_thenReturnEntity() {
        //Arrange
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.of(specialtyEntity);

        //Act
        SpecialtyEntity returnValue = utility.softDeleteById(
                ID, specialtyEntityOptional, FAIL_MESSAGE, SUCCESS_MESSAGE, infoContributor);

        //Assert
        Assertions.assertThat(returnValue.getIsActive()).isEqualTo(false);
        Assertions.assertThat(returnValue.getName()).isEqualTo(specialtyEntityOptional.get().getName());
    }

    @Test
    public void testSofDeleteById_whenOptionalEmpty_thenReturnNull() {
        //Arrange
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.empty();

        doNothing().when(infoContributor).incrementFailedDeleteOperations();

        //Act
        SpecialtyEntity returnValue = utility.softDeleteById(
                ID, specialtyEntityOptional, FAIL_MESSAGE, SUCCESS_MESSAGE, infoContributor);

        //Assert
        Assertions.assertThat(returnValue).isNull();

        //Verify
        verify(infoContributor).incrementFailedDeleteOperations();
    }

    @Test
    public void testSoftDeleteByField_whenOptionalNotEmpty_thenReturnEntity() throws DataNotFoundException {
        //Arrange
        String field = "TestSpecialty";
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.of(specialtyEntity);

        when(specialtyRepository.save(specialtyEntityOptional.get()))
                .thenReturn(specialtyEntity);

        //Act
        SpecialtyEntity returnValue = utility.softDeleteByField(
                field, specialtyEntityOptional, specialtyRepository,
                FAIL_MESSAGE, SUCCESS_MESSAGE, EXCEPTION_MESSAGE, infoContributor);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(specialtyEntity);
        Assertions.assertThat(returnValue.getIsActive()).isFalse();

        //Verify
        verify(specialtyRepository).save(specialtyEntity);
    }

    @Test
    public void testSoftDeleteByField_whenOptionalEmpty_thenThrowException() {
        //Arrange
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.empty();

        doNothing().when(infoContributor).incrementFailedDeleteOperations();

        //Act && assert
        Assertions.assertThatThrownBy(() -> utility.softDeleteByField(
                        FIELD, specialtyEntityOptional, specialtyRepository,
                        FAIL_MESSAGE, SUCCESS_MESSAGE, EXCEPTION_MESSAGE, infoContributor))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(infoContributor).incrementFailedDeleteOperations();
    }

    @Test
    public void testSoftCascadeDelete() {
        //Arrange
        DoctorEntity doctor1 = DoctorUtil.createDoctorEntity(1L);
        DoctorEntity doctor2 = DoctorUtil.createDoctorEntity(2L);
        specialtyEntity.setDoctors(List.of(doctor1, doctor2));

        VacationEntity vacation1 = VacationUtil.createVacationEntity(1L);
        vacation1.setStatus(VacationStatus.DONE);
        VacationEntity vacation2 = VacationUtil.createVacationEntity(2L);
        doctor1.setVacation(List.of(vacation1, vacation2));
        doctor2.setVacation(List.of(vacation1, vacation2));

        InvestigationEntity investigation1 = InvestigationUtil.createInvestigationEntity(1L);
        InvestigationEntity investigation2 = InvestigationUtil.createInvestigationEntity(2L);
        specialtyEntity.setInvestigations(List.of(investigation1, investigation2));

        when(workingHoursService.deleteAllWorkingHoursForDoctor(doctor1))
                .thenReturn(String.format(DELETE_WORKING_HOURS_MSG, doctor1.getName()));
        when(workingHoursService.deleteAllWorkingHoursForDoctor(doctor2))
                .thenReturn(String.format(DELETE_WORKING_HOURS_MSG, doctor2.getName()));

        when(appointmentService.cancelAllAppointmentForDoctor(doctor1))
                .thenReturn(String.format(CANCEL_APPOINTMENTS_MSG, doctor1.getName()));
        when(appointmentService.cancelAllAppointmentForDoctor(doctor2))
                .thenReturn(String.format(CANCEL_APPOINTMENTS_MSG, doctor2.getName()));

        when(doctorRepository.saveAll(List.of(doctor1, doctor2)))
                .thenReturn(List.of(doctor1, doctor2));

        when(investigationRepository.saveAll(List.of(investigation1, investigation2)))
                .thenReturn(List.of(investigation1, investigation2));

        //Act
        utility.softCascadeDelete(specialtyEntity, workingHoursService, appointmentService,
                doctorRepository, investigationRepository, SUCCESS_MESSAGE);

        //Assert
        Assertions.assertThat(doctor1.getIsActive()).isFalse();
        Assertions.assertThat(doctor2.getIsActive()).isFalse();

        Assertions.assertThat(doctor1.getVacation().get(0).getStatus()).isEqualTo(VacationStatus.DONE);
        Assertions.assertThat(doctor1.getVacation().get(1).getStatus()).isEqualTo(VacationStatus.CANCELED);
        Assertions.assertThat(doctor2.getVacation().get(0).getStatus()).isEqualTo(VacationStatus.DONE);
        Assertions.assertThat(doctor2.getVacation().get(1).getStatus()).isEqualTo(VacationStatus.CANCELED);

        Assertions.assertThat(investigation1.getIsActive()).isFalse();
        Assertions.assertThat(investigation2.getIsActive()).isFalse();

        //Verify
        verify(workingHoursService).deleteAllWorkingHoursForDoctor(doctor1);
        verify(workingHoursService).deleteAllWorkingHoursForDoctor(doctor2);
        verify(appointmentService).cancelAllAppointmentForDoctor(doctor1);
        verify(appointmentService).cancelAllAppointmentForDoctor(doctor2);
        verify(doctorRepository).saveAll(List.of(doctor1, doctor2));
        verify(investigationRepository).saveAll(List.of(investigation1, investigation2));
    }

}
