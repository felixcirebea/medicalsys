package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.WorkingHoursConverter;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.WorkingHoursRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.WorkingHoursUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkingHoursServiceTests {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @Mock
    private WorkingHoursConverter workingHoursConverter;

    @Mock
    private Contributor infoContributor;

    @InjectMocks
    private WorkingHoursService workingHoursService;

    @Test
    public void testUpsertWorkingHours_whenDtoIsValid_thenReturnId() throws
            DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;

        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(1);
        DayOfWeek dayOfWeek = DayOfWeek.of(workingHoursDto.getDayOfWeek());
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        WorkingHoursEntity workingHoursEntity = WorkingHoursUtil.createWorkingHoursEntity(id, 1);

        when(doctorRepository.findByNameAndIsActive(workingHoursDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(workingHoursRepository.existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek))
                .thenReturn(false);
        when(workingHoursConverter.fromDtoToEntity(workingHoursDto, doctorEntity))
                .thenReturn(workingHoursEntity);
        when(workingHoursRepository.save(workingHoursEntity))
                .thenReturn(workingHoursEntity);

        //Act
        Long returnValue = workingHoursService.upsertWorkingHours(workingHoursDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(workingHoursDto.getDoctor(), isActive);
        verify(workingHoursRepository).existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        verify(workingHoursConverter).fromDtoToEntity(workingHoursDto, doctorEntity);
        verify(workingHoursRepository).save(workingHoursEntity);
    }

    @Test
    public void testUpsertWorkingHours_whenWorkingHoursExist_thenReturnId() throws
            DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final LocalTime updatedStartHour = LocalTime.of(9, 0);

        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(1);
        workingHoursDto.setId(id);
        workingHoursDto.setStartHour(updatedStartHour);
        DayOfWeek dayOfWeek = DayOfWeek.of(workingHoursDto.getDayOfWeek());
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        WorkingHoursEntity workingHoursEntity = WorkingHoursUtil.createWorkingHoursEntity(id, 1);

        when(doctorRepository.findByNameAndIsActive(workingHoursDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(workingHoursRepository.existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek))
                .thenReturn(true);
        when(workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek))
                .thenReturn(Optional.of(workingHoursEntity));
        when(workingHoursRepository.save(workingHoursEntity))
                .thenReturn(workingHoursEntity);

        //Act
        Long returnValue = workingHoursService.upsertWorkingHours(workingHoursDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);
        Assertions.assertThat(workingHoursEntity.getStartHour()).isEqualTo(updatedStartHour);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(workingHoursDto.getDoctor(), isActive);
        verify(workingHoursRepository).existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        verify(workingHoursRepository).findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        verify(workingHoursRepository).save(workingHoursEntity);
    }

    @Test
    public void testUpsertWorkingHours_whenWorkingHoursNotExist_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final LocalTime updatedStartHour = LocalTime.of(9, 0);

        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(1);
        workingHoursDto.setId(id);
        workingHoursDto.setStartHour(updatedStartHour);
        DayOfWeek dayOfWeek = DayOfWeek.of(workingHoursDto.getDayOfWeek());
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(workingHoursDto.getDoctor(), isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(workingHoursRepository.existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek))
                .thenReturn(true);
        when(workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> workingHoursService.upsertWorkingHours(workingHoursDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(workingHoursDto.getDoctor(), isActive);
        verify(workingHoursRepository).existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        verify(workingHoursRepository).findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
    }

    @Test
    public void testUpsertWorkingHours_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;

        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(1);

        when(doctorRepository.findByNameAndIsActive(workingHoursDto.getDoctor(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> workingHoursService.upsertWorkingHours(workingHoursDto))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", workingHoursDto.getDoctor()));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(workingHoursDto.getDoctor(), isActive);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenAllValid_thenReturnListOfDtos()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final Integer dayOfWeek = 1;
        final DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        WorkingHoursEntity workingHoursEntity = WorkingHoursUtil.createWorkingHoursEntity(id, 1);
        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(dayOfWeek);
        workingHoursDto.setId(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue))
                .thenReturn(Optional.of(workingHoursEntity));
        when(workingHoursConverter.fromEntityToDto(workingHoursEntity))
                .thenReturn(workingHoursDto);

        //Act
        List<WorkingHoursDto> returnValue = workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, dayOfWeek);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(1);
        Assertions.assertThat(returnValue.get(0)).isEqualTo(workingHoursDto);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(workingHoursRepository).findByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue);
        verify(workingHoursConverter).fromEntityToDto(workingHoursEntity);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDayOfWeekNull_thenReturnListOfDtos()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        WorkingHoursEntity workingHoursEntity1 = WorkingHoursUtil.createWorkingHoursEntity(1L, 1);
        WorkingHoursEntity workingHoursEntity2 = WorkingHoursUtil.createWorkingHoursEntity(2L, 2);
        WorkingHoursDto workingHoursDto1 = WorkingHoursUtil.createWorkingHoursDto(1);
        WorkingHoursDto workingHoursDto2 = WorkingHoursUtil.createWorkingHoursDto(2);
        workingHoursDto1.setId(1L);
        workingHoursDto2.setId(2L);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(workingHoursRepository.findByDoctor(doctorEntity))
                .thenReturn(List.of(workingHoursEntity1, workingHoursEntity2));
        when(workingHoursConverter.fromEntityToDto(workingHoursEntity1))
                .thenReturn(workingHoursDto1);
        when(workingHoursConverter.fromEntityToDto(workingHoursEntity2))
                .thenReturn(workingHoursDto2);

        //Act
        List<WorkingHoursDto> returnValue =
                workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, null);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);
        Assertions.assertThat(returnValue.get(0)).isEqualTo(workingHoursDto1);
        Assertions.assertThat(returnValue.get(1)).isEqualTo(workingHoursDto2);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(workingHoursRepository).findByDoctor(doctorEntity);
        verify(workingHoursConverter).fromEntityToDto(workingHoursEntity1);
        verify(workingHoursConverter).fromEntityToDto(workingHoursEntity2);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorNull_thenReturnListOfDtos()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final Integer dayOfWeek = 1;
        final DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);

        WorkingHoursEntity workingHoursEntity = WorkingHoursUtil.createWorkingHoursEntity(id, dayOfWeek);
        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(dayOfWeek);
        workingHoursDto.setId(1L);

        when(workingHoursRepository.findByDayOfWeek(dayOfWeekValue))
                .thenReturn(List.of(workingHoursEntity));
        when(workingHoursConverter.fromEntityToDto(workingHoursEntity))
                .thenReturn(workingHoursDto);

        //Act
        List<WorkingHoursDto> returnValue =
                workingHoursService.getWorkingHoursByDoctorAndDay(null, dayOfWeek);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(1);
        Assertions.assertThat(returnValue.get(0)).isEqualTo(workingHoursDto);

        //Verify
        verify(workingHoursRepository).findByDayOfWeek(dayOfWeekValue);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorAndDayOfWeekNull_thenReturnListOfDtos()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;

        WorkingHoursEntity workingHoursEntity = WorkingHoursUtil.createWorkingHoursEntity(id, 3);
        WorkingHoursDto workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(3);
        workingHoursDto.setId(1L);

        when(workingHoursRepository.findAll())
                .thenReturn(List.of(workingHoursEntity));
        when(workingHoursConverter.fromEntityToDto(workingHoursEntity))
                .thenReturn(workingHoursDto);

        //Act
        List<WorkingHoursDto> returnValue =
                workingHoursService.getWorkingHoursByDoctorAndDay(null, null);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(1);
        Assertions.assertThat(returnValue.get(0)).isEqualTo(workingHoursDto);

        //Verify
        verify(workingHoursRepository).findAll();
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorExistsAndDayNotValid_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final Integer dayOfWeek = 30;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));

        //Act && assert
        Assertions.assertThatThrownBy(() -> workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, dayOfWeek))
                .isInstanceOf(DataMismatchException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final Integer dayOfWeek = 1;

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, dayOfWeek))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorNotExistAndDayOfWeekNull_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, null))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenAllValid_thenReturnId()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";
        final Integer dayOfWeek = 1;
        final DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        doNothing().when(workingHoursRepository)
                .deleteByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue);

        //Act
        Long returnValue = workingHoursService.deleteWorkingHoursByDoctorAndDay(doctorName, dayOfWeek);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(workingHoursRepository).deleteByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue);
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDayOfWeekNull_thenReturnId()
            throws DataMismatchException, DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String doctorName = "TestDoctor";

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        doNothing().when(workingHoursRepository).deleteByDoctor(doctorEntity);

        //Act
        Long returnValue = workingHoursService.deleteWorkingHoursByDoctorAndDay(doctorName, null);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(workingHoursRepository).deleteByDoctor(doctorEntity);
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDoctorNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String doctorName = "FakeDoctor";
        final Integer dayOfWeek = 1;

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());
        doNothing().when(infoContributor).incrementFailedDeleteOperations();

        //Act && assert
        Assertions.assertThatThrownBy(() ->
                        workingHoursService.deleteWorkingHoursByDoctorAndDay(doctorName, dayOfWeek))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(String.format("%s not found", doctorName));

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testDeleteAllWorkingHoursForDoctor_whenAllValid_thenReturnString() {
        //Arrange
        final Long id = 1L;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        doNothing().when(workingHoursRepository).deleteByDoctor(doctorEntity);

        //Act
        String returnValue = workingHoursService.deleteAllWorkingHoursForDoctor(doctorEntity);

        //Assert
        Assertions.assertThat(returnValue)
                .isEqualTo(String.format("Working hours for %s deleted", doctorEntity.getName()));

        //Verify
        verify(workingHoursRepository).deleteByDoctor(doctorEntity);
    }


}
