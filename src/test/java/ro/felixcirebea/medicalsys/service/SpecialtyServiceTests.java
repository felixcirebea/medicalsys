package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.SpecialtyConverter;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DeleteUtility;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpecialtyServiceTests {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyConverter specialtyConverter;

    @Mock
    private DeleteUtility deleteUtility;

    @Mock
    private Contributor infoContributor;

    @InjectMocks
    private SpecialtyService specialtyService;

    @Test
    public void testUpsertSpecialty_whenDtoIsValid_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        SpecialtyDto specialtyDto = SpecialtyUtil.createSpecialtyDto();
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(expectedId);


        when(specialtyConverter.fromDtoToEntity(specialtyDto)).thenReturn(specialtyEntity);
        when(specialtyRepository.save(specialtyEntity)).thenReturn(specialtyEntity);

        //Act
        Long returnValue = specialtyService.upsertSpecialty(specialtyDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);

        //Verify
        verify(specialtyConverter).fromDtoToEntity(specialtyDto);
        verify(specialtyRepository).save(specialtyEntity);
    }

    @Test
    public void testUpsertSpecialty_whenDtoIdNotNull_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        SpecialtyDto specialtyDto = SpecialtyUtil.createSpecialtyDto();
        specialtyDto.setId(expectedId);
        specialtyDto.setName("UpdatedName");
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(expectedId);

        when(specialtyRepository.findByIdAndIsActive(expectedId, isActive)).thenReturn(Optional.of(specialtyEntity));
        when(specialtyRepository.save(specialtyEntity)).thenReturn(specialtyEntity);

        //Act
        Long returnValue = specialtyService.upsertSpecialty(specialtyDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);
        Assertions.assertThat(specialtyDto.getName()).isEqualTo(specialtyEntity.getName());

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(expectedId, isActive);
        verify(specialtyRepository).save(specialtyEntity);
    }

    @Test
    public void testUpsertSpecialty_whenDtoIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        SpecialtyDto specialtyDto = SpecialtyUtil.createSpecialtyDto();
        specialtyDto.setId(nonExistentId);

        when(specialtyRepository.findByIdAndIsActive(nonExistentId, isActive)).thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> specialtyService.upsertSpecialty(specialtyDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetSpecialtyById_whenIdExists_thenReturnString() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        SpecialtyDto specialtyDto = SpecialtyUtil.createSpecialtyDto();
        specialtyDto.setId(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(expectedId);

        when(specialtyRepository.findByIdAndIsActive(expectedId, isActive)).thenReturn(Optional.of(specialtyEntity));

        //Act
        String returnValue = specialtyService.getSpecialtyById(expectedId);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(specialtyEntity.getName());

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(expectedId, isActive);
    }

    @Test
    public void testGetSpecialtyById_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;

        when(specialtyRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> specialtyService.getSpecialtyById(nonExistentId))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetSpecialtyByName_whenNameExists_thenReturnString() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final String expectedName = "TestSpecialty";
        final boolean isActive = true;
        SpecialtyDto specialtyDto = SpecialtyUtil.createSpecialtyDto();
        specialtyDto.setId(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(expectedId);

        when(specialtyRepository.findByNameAndIsActive(expectedName, isActive))
                .thenReturn(Optional.of(specialtyEntity));

        //Act
        String returnValue = specialtyService.getSpecialtyByName(expectedName);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(specialtyEntity.getName());

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(expectedName, isActive);
    }

    @Test
    public void testGetSpecialtyByName_whenNameNotExist_thenThrowException() {
        //Arrange
        final String nonExistentName = "FakeSpecialty";
        final boolean isActive = true;

        when(specialtyRepository.findByNameAndIsActive(nonExistentName, isActive))
                .thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> specialtyService.getSpecialtyByName(nonExistentName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(nonExistentName, isActive);
    }

    @Test
    public void testGetAllSpecialties_whenSpecialtiesExist_thenReturnListOfSpecialties() {
        //Arrange
        final boolean isActive = true;

        SpecialtyDto specialtyDto1 = SpecialtyDto.builder().id(1L).build();
        SpecialtyDto specialtyDto2 = SpecialtyDto.builder().id(2L).build();

        SpecialtyEntity specialtyEntity1 = new SpecialtyEntity();
        specialtyEntity1.setId(1L);
        SpecialtyEntity specialtyEntity2 = new SpecialtyEntity();
        specialtyEntity2.setId(2L);

        when(specialtyRepository.findAllByIsActive(isActive))
                .thenReturn(List.of(specialtyEntity1, specialtyEntity2));
        when(specialtyConverter.fromEntityToDto(specialtyEntity1)).thenReturn(specialtyDto1);
        when(specialtyConverter.fromEntityToDto(specialtyEntity2)).thenReturn(specialtyDto2);

        //Act
        List<SpecialtyDto> returnValue = specialtyService.getAllSpecialties();

        //Assert
        Assertions.assertThat(returnValue).isNotEmpty();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(specialtyRepository).findAllByIsActive(isActive);
        verify(specialtyConverter).fromEntityToDto(specialtyEntity1);
        verify(specialtyConverter).fromEntityToDto(specialtyEntity2);
    }

    @Test
    public void testGetAllSpecialties_whenSpecialtiesNotExist_thenReturnEmptyList() {
        //Arrange
        final boolean isActive = true;

        when(specialtyRepository.findAllByIsActive(isActive)).thenReturn(Collections.emptyList());

        //Act
        List<SpecialtyDto> returnValue = specialtyService.getAllSpecialties();

        //Assert
        Assertions.assertThat(returnValue).isEmpty();

        //Verify
        verify(specialtyRepository).findAllByIsActive(isActive);
        verifyNoInteractions(specialtyConverter);
    }

    @Test
    public void testDeleteSpecialtyById_whenIdExists_thenReturnId() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(id);
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.of(specialtyEntity);

        when(specialtyRepository.findByIdAndIsActive(id, isActive)).thenReturn(specialtyEntityOptional);
        when(deleteUtility.softDeleteById(
                eq(id), eq(specialtyEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(specialtyEntity);
        when(specialtyRepository.save(specialtyEntity)).thenReturn(specialtyEntity);

        //Act
        Long returnValue = specialtyService.deleteSpecialtyById(id);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(id, isActive);
        verify(specialtyRepository).save(specialtyEntity);
        verify(deleteUtility).softDeleteById(
                eq(id), eq(specialtyEntityOptional),
                anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testDeleteSpecialtyById_whenIdNotExist_thenReturnId() {
        //Arrange
        final Long nonExistentId = 1L;
        final boolean isActive = true;
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.empty();

        when(specialtyRepository.findByIdAndIsActive(nonExistentId, isActive)).thenReturn(Optional.empty());
        when(deleteUtility.softDeleteById(
                eq(nonExistentId), eq(specialtyEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(null);

        //Act
        Long returnValue = specialtyService.deleteSpecialtyById(nonExistentId);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(nonExistentId);

        //Verify
        verify(specialtyRepository).findByIdAndIsActive(nonExistentId, isActive);
        verify(deleteUtility).softDeleteById(
                eq(nonExistentId), eq(specialtyEntityOptional),
                anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testDeleteSpecialtyByName_whenNameExists_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(id);
        String name = specialtyEntity.getName();
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.of(specialtyEntity);

        when(specialtyRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(specialtyEntityOptional);
        when(specialtyRepository.save(specialtyEntity)).thenReturn(specialtyEntity);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(specialtyEntityOptional), eq(specialtyRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenReturn(specialtyEntity);

        //Act
        Long returnValue = specialtyService.deleteSpecialtyByName(name);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(specialtyEntityOptional), eq(specialtyRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
        verify(specialtyRepository).save(specialtyEntity);
    }

    @Test
    public void testDeleteSpecialtyByName_whenNameNotExist_thenThrowException() throws DataNotFoundException {
        //Arrange
        final String name = "FakeSpecialty";
        final boolean isActive = true;
        Optional<SpecialtyEntity> specialtyEntityOptional = Optional.empty();

        when(specialtyRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(specialtyEntityOptional);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(specialtyEntityOptional), eq(specialtyRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenThrow(DataNotFoundException.class);

        //Act && Assert
        Assertions.assertThatThrownBy(() -> specialtyService.deleteSpecialtyByName(name))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(specialtyEntityOptional), eq(specialtyRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
    }

}
