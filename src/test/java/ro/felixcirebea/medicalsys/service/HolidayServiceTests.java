package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.HolidayConverter;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.HolidayRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DeleteUtility;
import ro.felixcirebea.medicalsys.util.HolidayUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HolidayServiceTests {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private HolidayConverter holidayConverter;

    @Mock
    private Contributor infoContributor;

    @Mock
    private DeleteUtility deleteUtility;

    @InjectMocks
    private HolidayService holidayService;

    @Test
    public void testUpsertHolidayInsert_whenDtoIsValid_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        HolidayDto holidayDto = HolidayUtil.createHolidayDto();
        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(expectedId);

        when(holidayConverter.fromDtoToEntity(holidayDto)).thenReturn(holidayEntity);
        when(holidayRepository.save(holidayEntity)).thenReturn(holidayEntity);

        //Act
        Long returnedValue = holidayService.upsertHoliday(holidayDto);

        //Assert
        Assertions.assertThat(returnedValue).isEqualTo(expectedId);

        //Verify
        verify(holidayConverter).fromDtoToEntity(holidayDto);
        verify(holidayRepository).save(holidayEntity);

    }

    @Test
    public void testUpsertHolidayUpdate_whenDtoIdNotNull_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        LocalDate updatedStartDate = LocalDate.of(2023, 4, 1);
        LocalDate updatedEndDate = LocalDate.of(2023, 4, 2);
        HolidayDto holidayDto = HolidayUtil.createHolidayDto();
        holidayDto.setId(expectedId);
        holidayDto.setStartDate(updatedStartDate);
        holidayDto.setEndDate(updatedEndDate);

        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(expectedId);

        when(holidayRepository.findByIdAndIsActive(expectedId, isActive)).thenReturn(Optional.of(holidayEntity));
        when(holidayRepository.save(holidayEntity)).thenReturn(holidayEntity);

        //Act
        Long returnedValue = holidayService.upsertHoliday(holidayDto);

        //Assert
        Assertions.assertThat(returnedValue).isEqualTo(expectedId);
        Assertions.assertThat(holidayEntity.getStartDate()).isEqualTo(holidayDto.getStartDate());
        Assertions.assertThat(holidayEntity.getEndDate()).isEqualTo(holidayDto.getEndDate());
        Assertions.assertThat(holidayEntity.getIsActive()).isEqualTo(isActive);

        //Verify
        verify(holidayRepository).findByIdAndIsActive(expectedId, isActive);
        verify(holidayRepository).save(holidayEntity);
    }

    @Test
    public void testUpsertHolidayUpdate_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        final String exceptionMessage = "Wrong ID";

        HolidayDto holidayDto = HolidayUtil.createHolidayDto();
        holidayDto.setId(nonExistentId);

        when(holidayRepository.findByIdAndIsActive(nonExistentId, isActive)).thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> holidayService.upsertHoliday(holidayDto))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(exceptionMessage);

        //Verify
        verify(holidayRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetHolidayById_whenIdExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;

        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(id);
        HolidayDto holidayDto = HolidayUtil.createHolidayDto();
        holidayDto.setId(id);

        when(holidayRepository.findByIdAndIsActive(id, isActive)).thenReturn(Optional.of(holidayEntity));
        when(holidayConverter.fromEntityToDto(holidayEntity)).thenReturn(holidayDto);

        //Act
        HolidayDto returnValue = holidayService.getHolidayById(id);

        //Assert
        Assertions.assertThat(id).isEqualTo(returnValue.getId());
        Assertions.assertThat(holidayEntity.getDescription()).isEqualTo(returnValue.getDescription());

        //Verify
        verify(holidayRepository).findByIdAndIsActive(id, isActive);
        verify(holidayConverter).fromEntityToDto(holidayEntity);
    }

    @Test
    public void testGetHolidayById_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        final String exceptionMessage = "Wrong ID";

        when(holidayRepository.findByIdAndIsActive(nonExistentId, isActive)).thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> holidayService.getHolidayById(nonExistentId))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(exceptionMessage);

        //Verify
        verify(holidayRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetHolidayByDescription_whenDescriptionExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final String description = "TestHoliday";
        final Long id = 1L;
        final boolean isActive = true;

        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(id);
        HolidayDto holidayDto = HolidayUtil.createHolidayDto();
        holidayDto.setId(id);

        when(holidayRepository.findByDescriptionAndIsActive(description, isActive))
                .thenReturn(Optional.of(holidayEntity));
        when(holidayConverter.fromEntityToDto(holidayEntity)).thenReturn(holidayDto);

        //Act
        HolidayDto returnValue = holidayService.getHolidayByDescription(description);

        //Assert
        Assertions.assertThat(id).isEqualTo(returnValue.getId());
        Assertions.assertThat(holidayEntity.getDescription()).isEqualTo(returnValue.getDescription());

        //Verify
        verify(holidayRepository).findByDescriptionAndIsActive(description, isActive);
        verify(holidayConverter).fromEntityToDto(holidayEntity);
    }

    @Test
    public void testGetHolidayByDescription_whenDescriptionNotExist_thenThrowException() {
        //Arrange
        final String nonExistentDescription = "FakeHoliday";
        final boolean isActive = true;
        final String exceptionMessage = " not found";

        when(holidayRepository.findByDescriptionAndIsActive(nonExistentDescription, isActive))
                .thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> holidayService.getHolidayByDescription(nonExistentDescription))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(nonExistentDescription + exceptionMessage);

        //Verify
        verify(holidayRepository).findByDescriptionAndIsActive(nonExistentDescription, isActive);
    }

    @Test
    public void testGetAllHolidays_whenHolidaysExist_thenReturnListOfHolidays() {
        //Arrange
        final boolean isActive = true;

        HolidayDto holidayDto1 = HolidayDto.builder().id(1L).build();
        HolidayDto holidayDto2 = HolidayDto.builder().id(2L).build();

        HolidayEntity holidayEntity1 = new HolidayEntity();
        HolidayEntity holidayEntity2 = new HolidayEntity();
        holidayEntity1.setId(1L);
        holidayEntity2.setId(2L);

        when(holidayRepository.findAllByIsActive(isActive)).thenReturn(List.of(holidayEntity1, holidayEntity2));
        when(holidayConverter.fromEntityToDto(holidayEntity1)).thenReturn(holidayDto1);
        when(holidayConverter.fromEntityToDto(holidayEntity2)).thenReturn(holidayDto2);

        //Act
        List<HolidayDto> returnValue = holidayService.getAllHolidays();

        //Assert
        Assertions.assertThat(returnValue).isNotEmpty();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(holidayRepository).findAllByIsActive(isActive);
        verify(holidayConverter).fromEntityToDto(holidayEntity1);
        verify(holidayConverter).fromEntityToDto(holidayEntity2);
    }

    @Test
    public void testGetAllHolidays_whenHolidaysNotExist_thenReturnEmptyList() {
        //Arrange
        final boolean isActive = true;

        when(holidayRepository.findAllByIsActive(isActive)).thenReturn(Collections.emptyList());

        //Act
        List<HolidayDto> returnValue = holidayService.getAllHolidays();

        //Assert
        Assertions.assertThat(returnValue).isEmpty();

        //Verify
        verify(holidayRepository).findAllByIsActive(isActive);
    }

    @Test
    public void testDeleteHolidayById_whenIdExists_thenReturnId() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(id);
        Optional<HolidayEntity> holidayEntityOptional = Optional.of(holidayEntity);

        when(holidayRepository.findByIdAndIsActive(id, isActive)).thenReturn(holidayEntityOptional);
        when(holidayRepository.save(holidayEntity)).thenReturn(holidayEntity);
        when(deleteUtility.softDeleteById(
                eq(id), eq(holidayEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(holidayEntity);

        // Act
        Long returnValue = holidayService.deleteHolidayById(id);

        // Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        // Verify
        verify(holidayRepository).findByIdAndIsActive(id, isActive);
        verify(deleteUtility).softDeleteById(
                eq(id), eq(holidayEntityOptional),
                anyString(), anyString(), eq(infoContributor));
        verify(holidayRepository).save(holidayEntity);
    }

    @Test
    public void testDeleteHolidayById_whenIdNotExist_thenReturnId() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        Optional<HolidayEntity> holidayEntityOptional = Optional.empty();

        when(holidayRepository.findByIdAndIsActive(nonExistentId, isActive)).thenReturn(Optional.empty());
        when(deleteUtility.softDeleteById(
                eq(nonExistentId), eq(holidayEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(null);

        // Act
        Long returnValue = holidayService.deleteHolidayById(nonExistentId);

        // Assert
        Assertions.assertThat(returnValue).isEqualTo(nonExistentId);

        // Verify
        verify(holidayRepository).findByIdAndIsActive(nonExistentId, isActive);
        verify(deleteUtility).softDeleteById(
                eq(nonExistentId), eq(holidayEntityOptional),
                anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testDeleteHolidayByDescription_whenDescriptionExists_thenReturnId()
            throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        HolidayEntity holidayEntity = HolidayUtil.createHolidayEntity(id);
        String description = holidayEntity.getDescription();
        Optional<HolidayEntity> holidayEntityOptional = Optional.of(holidayEntity);

        when(holidayRepository.findByDescriptionAndIsActive(description, isActive))
                .thenReturn(holidayEntityOptional);
        when(holidayRepository.save(holidayEntity)).thenReturn(holidayEntity);
        when(deleteUtility.softDeleteByField(
                eq(description), eq(holidayEntityOptional), eq(holidayRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenReturn(holidayEntity);

        // Act
        Long returnValue = holidayService.deleteHolidayByDescription(description);

        // Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        // Verify
        verify(holidayRepository).findByDescriptionAndIsActive(description, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(description), eq(holidayEntityOptional), eq(holidayRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
        verify(holidayRepository).save(holidayEntity);
    }

    @Test
    public void testDeleteHolidayByDescription_whenDescriptionNotExist_thenThrowException()
            throws DataNotFoundException {
        //Arrange
        final boolean isActive = true;
        String description = "TestDescription";

        Optional<HolidayEntity> holidayEntityOptional = Optional.empty();

        when(holidayRepository.findByDescriptionAndIsActive(description, isActive))
                .thenReturn(holidayEntityOptional);
        when(deleteUtility.softDeleteByField(
                eq(description), eq(holidayEntityOptional), eq(holidayRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenThrow(DataNotFoundException.class);

        // Act && Assert
        Assertions.assertThatThrownBy(() -> holidayService.deleteHolidayByDescription(description))
                .isInstanceOf(DataNotFoundException.class);

        // Verify
        verify(holidayRepository).findByDescriptionAndIsActive(description, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(description), eq(holidayEntityOptional), eq(holidayRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testIsDateHoliday_whenDateIsHoliday_thenReturnTrue() {
        //Arrange
        LocalDate date = LocalDate.of(2023, 8, 15);
        boolean expectedValue = true;

        when(holidayRepository.isDateBetweenHolidays(date)).thenReturn(expectedValue);

        //Act
        Boolean returnValue = holidayService.isDateHoliday(date);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedValue);

        //Verify
        verify(holidayRepository).isDateBetweenHolidays(date);
    }

}
