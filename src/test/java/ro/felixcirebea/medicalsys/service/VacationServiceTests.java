package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.VacationConverter;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.VacationRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.VacationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VacationServiceTests {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private Contributor infoContributor;

    @Mock
    private VacationRepository vacationRepository;

    @Mock
    private VacationConverter vacationConverter;

    @InjectMocks
    private VacationService vacationService;

    @Test
    public void testInsertVacation_whenAllValid_thenReturnId() throws DataMismatchException, DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final LocalDate currentDate = LocalDate.of(2022, 1, 1);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationDto vacationDto = VacationUtil.createVacationDto();
        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);
        when(vacationRepository.existsByDateBetweenDates(vacationDto.getStartDate(), vacationDto.getEndDate()))
                .thenReturn(false);
        when(vacationConverter.fromDtoToEntity(vacationDto, doctorEntity))
                .thenReturn(vacationEntity);
        when(vacationRepository.save(vacationEntity))
                .thenReturn(vacationEntity);

        //Act
        Long returnValue = vacationService.insertVacation(vacationDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(infoContributor).getCurrentDate();
        verify(vacationRepository).existsByDateBetweenDates(vacationDto.getStartDate(), vacationDto.getEndDate());
        verify(vacationConverter).fromDtoToEntity(vacationDto, doctorEntity);
        verify(vacationRepository).save(vacationEntity);
    }

    @Test
    public void testInsertVacation_whenVacationExists_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final LocalDate currentDate = LocalDate.of(2022, 1, 1);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationDto vacationDto = VacationUtil.createVacationDto();

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);
        when(vacationRepository.existsByDateBetweenDates(vacationDto.getStartDate(), vacationDto.getEndDate()))
                .thenReturn(true);

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.insertVacation(vacationDto))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage(String.format(
                        "Vacation already planned for %s - %s", vacationDto.getStartDate(), vacationDto.getEndDate()));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(infoContributor).getCurrentDate();
        verify(vacationRepository).existsByDateBetweenDates(vacationDto.getStartDate(), vacationDto.getEndDate());
    }

    @Test
    public void testInsertVacation_whenStartDateIsBeforeCurrDate_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final LocalDate currentDate = LocalDate.of(2023, 2, 2);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationDto vacationDto = VacationUtil.createVacationDto();

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.insertVacation(vacationDto))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage("Can't operate vacations from the past");

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(infoContributor).getCurrentDate();
    }

    @Test
    public void testInsertVacation_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        VacationDto vacationDto = VacationUtil.createVacationDto();
        vacationDto.setDoctor(doctorName);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.insertVacation(vacationDto))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testCancelVacation_whenAllValid_thenReturnId() throws DataMismatchException, DataNotFoundException, ConcurrencyException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "2023-02-01";
        final LocalDate startDateValue = LocalDate.parse("2023-02-01");
        final LocalDate currentDate = LocalDate.of(2023, 1, 1);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity = VacationUtil.createVacationEntity(id);

        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, true))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findByDoctorAndStartDate(doctorEntity, startDateValue))
                .thenReturn(Optional.of(vacationEntity));
        when(vacationRepository.save(vacationEntity))
                .thenReturn(vacationEntity);

        //Act
        Long returnValue = vacationService.cancelVacation(doctorName, startDate);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);
        Assertions.assertThat(vacationEntity.getStatus()).isEqualTo(VacationStatus.CANCELED);

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findByDoctorAndStartDate(doctorEntity, startDateValue);
        verify(vacationRepository).save(vacationEntity);
    }

    @Test
    public void testCancelVacation_whenVacationNotExist_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "2023-02-01";
        final LocalDate startDateValue = LocalDate.parse("2023-02-01");
        final LocalDate currentDate = LocalDate.of(2023, 1, 1);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, true))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findByDoctorAndStartDate(doctorEntity, startDateValue))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.cancelVacation(doctorName, startDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Vacation not found");

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findByDoctorAndStartDate(doctorEntity, startDateValue);
    }

    @Test
    public void testCancelVacation_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "2023-02-01";
        final LocalDate currentDate = LocalDate.of(2023, 1, 1);

        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);
        when(doctorRepository.findByNameAndIsActive(doctorName, true))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.cancelVacation(doctorName, startDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(infoContributor).getCurrentDate();
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testCancelVacation_whenStartDateIsBeforeCurrDate_thenThrowException() {
        //Arrange
        final String doctorName = "TestDoctor";
        final String startDate = "2023-02-01";
        final LocalDate currentDate = LocalDate.of(2023, 3, 1);

        when(infoContributor.getCurrentDate())
                .thenReturn(currentDate);

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.cancelVacation(doctorName, startDate))
                .isInstanceOf(ConcurrencyException.class)
                .hasMessage("Can't operate vacations from the past");

        //Verify
        verify(infoContributor).getCurrentDate();
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenAllValid_thenReturnListOfDtos() throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "2023-01-01";
        final String endDate = "2023-01-07";
        final LocalDate startDateValue = LocalDate.parse(startDate);
        final LocalDate endDateValue = LocalDate.parse(endDate);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findAllByDoctorAndStartDateAfterAndEndDateBefore(
                doctorEntity, startDateValue, endDateValue))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository)
                .findAllByDoctorAndStartDateAfterAndEndDateBefore(doctorEntity, startDateValue, endDateValue);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartDateBlank_thenReturnListOfDtos() throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "";
        final String endDate = "2023-01-07";
        final LocalDate endDateValue = LocalDate.parse(endDate);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findAllByDoctorAndEndDateBefore(doctorEntity, endDateValue))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findAllByDoctorAndEndDateBefore(doctorEntity, endDateValue);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenEndDateBlank_thenReturnListOfDtos() throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "2023-01-01";
        final String endDate = "";
        final LocalDate startDateValue = LocalDate.parse(startDate);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findAllByDoctorAndStartDateAfter(doctorEntity, startDateValue))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findAllByDoctorAndStartDateAfter(doctorEntity, startDateValue);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenBothDatesBlank_thenReturnListOfDtos() throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final String startDate = "";
        final String endDate = "";

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        doctorEntity.setVacation(List.of(vacationEntity1, vacationEntity2));
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final String startDate = "2023-01-01";
        final String endDate = "2023-01-07";

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetVacationByDoctorAndType_whenAllValid_thenReturnListOfDtos() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final VacationType inputType = VacationType.VACATION;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findAllByDoctorAndType(doctorEntity, inputType))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndType(doctorName, inputType);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);
        Assertions.assertThat(returnValue.get(0).getType()).isEqualTo(inputType);
        Assertions.assertThat(returnValue.get(1).getType()).isEqualTo(inputType);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findAllByDoctorAndType(doctorEntity, inputType);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorBlank_thenReturnListOfDtos() throws DataNotFoundException {
        //Arrange
        final String doctorName = "";
        final VacationType inputType = VacationType.VACATION;

        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(vacationRepository.findAllByType(inputType))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByDoctorAndType(doctorName, inputType);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);
        Assertions.assertThat(returnValue.get(0).getType()).isEqualTo(inputType);
        Assertions.assertThat(returnValue.get(1).getType()).isEqualTo(inputType);

        //Verify
        verify(vacationRepository).findAllByType(inputType);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final VacationType inputType = VacationType.VACATION;

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.getVacationByDoctorAndType(doctorName, inputType))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testIsDateVacation_whenAllValid_thenReturnTrue() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final LocalDate inputDate = LocalDate.of(2023, 1, 1);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.isDateBetweenVacation(doctorEntity, inputDate))
                .thenReturn(true);

        //Act
        Boolean returnValue = vacationService.isDateVacation(doctorName, inputDate);

        //Assert
        Assertions.assertThat(returnValue).isTrue();

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).isDateBetweenVacation(doctorEntity, inputDate);
    }

    @Test
    public void testIsDateVacation_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final LocalDate inputDate = LocalDate.of(2023, 1, 1);


        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.isDateVacation(doctorName, inputDate))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetVacationByStatus_whenAllValid_thenReturnLisOfDtos() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final VacationStatus inputStatus = VacationStatus.PLANNED;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        VacationDto vacationDto1 = VacationUtil.createVacationDto();
        vacationDto1.setId(1L);
        VacationDto vacationDto2 = VacationUtil.createVacationDto();
        vacationDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(vacationRepository.findAllByDoctorAndStatus(doctorEntity, inputStatus))
                .thenReturn(List.of(vacationEntity1, vacationEntity2));
        when(vacationConverter.fromEntityToDto(vacationEntity1))
                .thenReturn(vacationDto1);
        when(vacationConverter.fromEntityToDto(vacationEntity2))
                .thenReturn(vacationDto2);

        //Act
        List<VacationDto> returnValue = vacationService.getVacationByStatus(doctorName, inputStatus);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(vacationRepository).findAllByDoctorAndStatus(doctorEntity, inputStatus);
        verify(vacationConverter).fromEntityToDto(vacationEntity1);
        verify(vacationConverter).fromEntityToDto(vacationEntity2);
    }

    @Test
    public void testGetVacationByStatus_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final VacationStatus inputStatus = VacationStatus.PLANNED;

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> vacationService.getVacationByStatus(doctorName, inputStatus))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }
}
