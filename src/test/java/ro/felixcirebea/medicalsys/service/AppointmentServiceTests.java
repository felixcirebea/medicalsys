package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.AppointmentConverter;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.*;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.AppointmentRepository;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.helper.*;
import ro.felixcirebea.medicalsys.util.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTests {

    @Mock
    private Contributor infoContributor;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private HolidayService holidayService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentConverter appointmentConverter;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    public void testGetAvailableHours_whenAllValid_thenReturnListOfLocalTime()
            throws DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        doctorEntity.setWorkingHours(List.of(workingHoursEntity1, workingHoursEntity2));

        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);
        doctorEntity.setVacation(List.of(vacationEntity));

        AppointmentEntity appointmentEntity1 = AppointmentUtil.createAppointmentEntity(
                1L, desiredDate, LocalTime.of(8, 30), LocalTime.of(9, 0));
        AppointmentEntity appointmentEntity2 = AppointmentUtil.createAppointmentEntity(
                1L, desiredDate, LocalTime.of(9, 30), LocalTime.of(10, 30));

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(holidayService.isDateHoliday(desiredDate)).thenReturn(false);
        when(appointmentRepository.findAllByDoctorAndDate(doctorEntity, desiredDate))
                .thenReturn(List.of(appointmentEntity1, appointmentEntity2));

        //Act
        List<LocalTime> returnValue = appointmentService.getAvailableHours(doctorName, investigationName, desiredDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(5);
        Assertions.assertThat(returnValue.get(0)).isEqualTo(LocalTime.of(8, 0));
        Assertions.assertThat(returnValue.get(1)).isEqualTo(LocalTime.of(9, 0));
        Assertions.assertThat(returnValue.get(2)).isEqualTo(LocalTime.of(10, 30));
        Assertions.assertThat(returnValue.get(3)).isEqualTo(LocalTime.of(11, 0));
        Assertions.assertThat(returnValue.get(4)).isEqualTo(LocalTime.of(11, 30));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
        verify(holidayService).isDateHoliday(desiredDate);
        verify(appointmentRepository).findAllByDoctorAndDate(doctorEntity, desiredDate);
    }

    @Test
    public void testGetAvailableHours_whenFullyBooked_thenReturnEmptyList()
            throws DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        doctorEntity.setWorkingHours(List.of(workingHoursEntity1, workingHoursEntity2));

        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);
        doctorEntity.setVacation(List.of(vacationEntity));

        AppointmentEntity appointmentEntity1 = AppointmentUtil.createAppointmentEntity(
                1L, desiredDate, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(holidayService.isDateHoliday(desiredDate)).thenReturn(false);
        when(appointmentRepository.findAllByDoctorAndDate(doctorEntity, desiredDate))
                .thenReturn(List.of(appointmentEntity1));

        //Act
        List<LocalTime> returnValue = appointmentService.getAvailableHours(doctorName, investigationName, desiredDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
        verify(holidayService).isDateHoliday(desiredDate);
        verify(appointmentRepository).findAllByDoctorAndDate(doctorEntity, desiredDate);
    }

    @Test
    public void testGetAvailableHours_whenDateIsVacation_thenReturnEmptyList()
            throws DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 3);
        final LocalDate currentDate = LocalDate.of(2023, 1, 1);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        doctorEntity.setWorkingHours(List.of(workingHoursEntity1, workingHoursEntity2));

        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);
        doctorEntity.setVacation(List.of(vacationEntity));

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(holidayService.isDateHoliday(desiredDate)).thenReturn(false);

        //Act
        List<LocalTime> returnValue = appointmentService.getAvailableHours(doctorName, investigationName, desiredDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
        verify(holidayService).isDateHoliday(desiredDate);
    }

    @Test
    public void testGetAvailableHours_whenDateIsHoliday_thenReturnEmptyList()
            throws DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        doctorEntity.setWorkingHours(List.of(workingHoursEntity1, workingHoursEntity2));

        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);
        doctorEntity.setVacation(List.of(vacationEntity));

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(holidayService.isDateHoliday(desiredDate)).thenReturn(true);

        //Act
        List<LocalTime> returnValue = appointmentService.getAvailableHours(doctorName, investigationName, desiredDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
        verify(holidayService).isDateHoliday(desiredDate);
    }

    @Test
    public void testGetAvailableHours_whenDateNotHaveWorkingHours_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 11);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        doctorEntity.setWorkingHours(List.of(workingHoursEntity1, workingHoursEntity2));

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        appointmentService.getAvailableHours(doctorName, investigationName, desiredDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("Working hours not found for %s", desiredDate));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
    }

    @Test
    public void testGetAvailableHours_whenInvestigationNotFound_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String investigationName = "FakeInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 11);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        appointmentService.getAvailableHours(doctorName, investigationName, desiredDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", investigationName));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
    }

    @Test
    public void testGetAvailableHours_whenDoctorNotFound_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 11);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        appointmentService.getAvailableHours(doctorName, investigationName, desiredDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetAvailableHours_whenDateIsBeforeCurrentDate_thenThrowException() {
        //Arrange
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 4);
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        appointmentService.getAvailableHours(doctorName, investigationName, desiredDate))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage("Can't create appointments for dates in the past");

        //Verify
        verify(infoContributor).getCurrentDate();
    }

    @Test
    public void testBookAppointment_whenAllValid_thenReturnLong() throws DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        LocalTime startHour = appointmentDto.getStartHour();
        LocalTime endHour = appointmentDto.getStartHour().plusMinutes(investigationEntity.getDuration());
        AppointmentEntity appointmentEntity =
                AppointmentUtil.createAppointmentEntity(id, desiredDate, startHour, endHour);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(appointmentDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(appointmentDto.getInvestigation(), isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(appointmentRepository.existsByDoctorDateAndTimeRange(
                doctorEntity, appointmentDto.getDate(), startHour, endHour))
                .thenReturn(false);
        when(appointmentConverter.fromDtoToEntity(appointmentDto, doctorEntity, investigationEntity))
                .thenReturn(appointmentEntity);
        when(appointmentRepository.save(appointmentEntity))
                .thenReturn(appointmentEntity);

        //Act
        Long returnValue = appointmentService.bookAppointment(appointmentDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(appointmentDto.getDoctor(), isActive);
        verify(investigationRepository).findByNameAndIsActive(appointmentDto.getInvestigation(), isActive);
        verify(appointmentRepository).existsByDoctorDateAndTimeRange(
                doctorEntity, appointmentDto.getDate(), startHour, endHour);
        verify(appointmentConverter).fromDtoToEntity(appointmentDto, doctorEntity, investigationEntity);
        verify(appointmentRepository).save(appointmentEntity);
    }

    @Test
    public void testBookAppointment_whenHourNotAvailable_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        LocalTime startHour = appointmentDto.getStartHour();
        LocalTime endHour = appointmentDto.getStartHour().plusMinutes(investigationEntity.getDuration());

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(appointmentDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(appointmentDto.getInvestigation(), isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(appointmentRepository.existsByDoctorDateAndTimeRange(
                doctorEntity, appointmentDto.getDate(), startHour, endHour))
                .thenReturn(true);

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.bookAppointment(appointmentDto))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage(String.format("%s not available, please select a different hour", startHour));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(appointmentDto.getDoctor(), isActive);
        verify(investigationRepository).findByNameAndIsActive(appointmentDto.getInvestigation(), isActive);
        verify(appointmentRepository).existsByDoctorDateAndTimeRange(
                doctorEntity, appointmentDto.getDate(), startHour, endHour);
    }

    @Test
    public void testBookAppointment_whenInvestigationNotExist_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(appointmentDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(appointmentDto.getInvestigation(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.bookAppointment(appointmentDto))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", appointmentDto.getInvestigation()));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(appointmentDto.getDoctor(), isActive);
        verify(investigationRepository).findByNameAndIsActive(appointmentDto.getInvestigation(), isActive);
    }

    @Test
    public void testBookAppointment_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);
        final LocalDate desiredDate = LocalDate.of(2023, 1, 10);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(appointmentDto.getDoctor(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.bookAppointment(appointmentDto))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", appointmentDto.getDoctor()));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(appointmentDto.getDoctor(), isActive);
    }

    @Test
    public void testBookAppointment_whenDateIsBeforeCurrentDate_thenThrowException() {
        //Arrange
        final LocalDate currentDate = LocalDate.of(2023, 1, 5);
        final LocalDate desiredDate = LocalDate.of(2023, 1, 4);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);

        when(infoContributor.getCurrentDate()).thenReturn(currentDate);

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.bookAppointment(appointmentDto))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage("Can't create appointments for dates in the past");

        //Verify
        verify(infoContributor).getCurrentDate();
    }

    @Test
    public void testGetAppointmentById_whenIdExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final LocalDate desiredDate = LocalDate.of(2023, 1, 4);
        final LocalTime desiredTime = LocalTime.of(8, 30);

        AppointmentEntity appointmentEntity = AppointmentUtil.createAppointmentEntity(
                id, desiredDate, desiredTime, desiredTime.plusMinutes(30));
        AppointmentDto appointmentDto = AppointmentUtil.createAppointmentDto(desiredDate, desiredTime);
        appointmentDto.setId(id);

        when(appointmentRepository.findById(id))
                .thenReturn(Optional.of(appointmentEntity));
        when(appointmentConverter.fromEntityToDto(appointmentEntity))
                .thenReturn(appointmentDto);

        //Act
        AppointmentDto returnValue = appointmentService.getAppointmentById(id);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(appointmentEntity.getId());
        Assertions.assertThat(returnValue.getDate()).isEqualTo(appointmentEntity.getDate());
        Assertions.assertThat(returnValue.getStartHour()).isEqualTo(appointmentEntity.getStartTime());

        //Verify
        verify(appointmentRepository).findById(id);
        verify(appointmentConverter).fromEntityToDto(appointmentEntity);
    }

    @Test
    public void testGetAppointmentById_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long id = 1L;

        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.getAppointmentById(id))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(appointmentRepository).findById(id);
    }

    @Test
    public void testCancelAppointmentByIdAndName_whenAllValid_thenReturnString() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String clientName = "TestClient";
        final LocalDate desiredDate = LocalDate.of(2023, 1, 4);
        final LocalTime desiredTime = LocalTime.of(8, 30);
        AppointmentEntity appointmentEntity = AppointmentUtil.createAppointmentEntity(
                id, desiredDate, desiredTime, desiredTime.plusMinutes(30));

        when(appointmentRepository.findByIdAndClientNameAndStatus(id, clientName, AppointmentStatus.NEW))
                .thenReturn(Optional.of(appointmentEntity));
        when(appointmentRepository.save(appointmentEntity))
                .thenReturn(appointmentEntity);

        //Act
        String returnValue = appointmentService.cancelAppointmentByIdAndName(id, clientName);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo("Appointment successfully canceled");
        Assertions.assertThat(appointmentEntity.getStatus()).isEqualTo(AppointmentStatus.CANCELED);

        //Verify
        verify(appointmentRepository).findByIdAndClientNameAndStatus(id, clientName, AppointmentStatus.NEW);
        verify(appointmentRepository).save(appointmentEntity);
    }

    @Test
    public void testCancelAppointmentByIdAndName_whenAppointmentNotExist_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final String clientName = "TestClient";

        when(appointmentRepository.findByIdAndClientNameAndStatus(id, clientName, AppointmentStatus.NEW))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> appointmentService.cancelAppointmentByIdAndName(id, clientName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(appointmentRepository).findByIdAndClientNameAndStatus(id, clientName, AppointmentStatus.NEW);
    }

    @Test
    public void testCancelAllAppointmentsForDoctor_whenAllValid_thenReturnString() {
        //Arrange
        final Long id = 1L;
        final LocalDate desiredDate = LocalDate.of(2023, 1, 4);
        final LocalTime desiredTime1 = LocalTime.of(8, 30);
        final LocalTime desiredTime2 = LocalTime.of(9, 30);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        AppointmentEntity appointmentEntity1 = AppointmentUtil.createAppointmentEntity(
                1L, desiredDate, desiredTime1, desiredTime1.plusMinutes(30));
        AppointmentEntity appointmentEntity2 = AppointmentUtil.createAppointmentEntity(
                2L, desiredDate, desiredTime2, desiredTime2.plusMinutes(30));

        when(appointmentRepository.findAllByDoctor(doctorEntity))
                .thenReturn(List.of(appointmentEntity1, appointmentEntity2));
        when(appointmentRepository.saveAll(List.of(appointmentEntity1, appointmentEntity2)))
                .thenReturn(List.of(appointmentEntity1, appointmentEntity2));

        //Act
        String returnValue = appointmentService.cancelAllAppointmentForDoctor(doctorEntity);

        //Assert
        Assertions.assertThat(returnValue)
                .isEqualTo(String.format("Appointments for %s canceled", doctorEntity.getName()));
        Assertions.assertThat(appointmentEntity1.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        Assertions.assertThat(appointmentEntity2.getStatus()).isEqualTo(AppointmentStatus.CANCELED);

        //Verify
        verify(appointmentRepository).findAllByDoctor(doctorEntity);
        verify(appointmentRepository).saveAll(List.of(appointmentEntity1, appointmentEntity2));
    }
}
