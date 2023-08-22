package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class InvestigationServiceTests {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private InvestigationConverter investigationConverter;

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private DeleteUtility deleteUtility;

    @Mock
    private Contributor infoContributor;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private InvestigationService investigationService;

    @Test
    public void testUpsertInvestigation_whenDtoIsValid_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(investigationDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(investigationConverter.fromDtoToEntity(investigationDto, specialtyEntity))
                .thenReturn(investigationEntity);
        when(investigationRepository.save(investigationEntity))
                .thenReturn(investigationEntity);

        //Act
        Long returnValue = investigationService.upsertInvestigation(investigationDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(investigationDto.getSpecialty(), isActive);
        verify(investigationConverter).fromDtoToEntity(investigationDto, specialtyEntity);
        verify(investigationRepository).save(investigationEntity);
    }

    @Test
    public void testUpsertInvestigation_whenIdNotNull_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        double updatedBasePrice = 500D;
        int updatedDuration = 150;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();
        investigationDto.setId(expectedId);
        investigationDto.setBasePrice(updatedBasePrice);
        investigationDto.setDuration(updatedDuration);

        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(investigationDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(investigationRepository.findByIdAndIsActive(investigationDto.getId(), isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(investigationRepository.save(investigationEntity))
                .thenReturn(investigationEntity);

        //Act
        Long returnValue = investigationService.upsertInvestigation(investigationDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);
        Assertions.assertThat(investigationEntity.getBasePrice()).isEqualTo(updatedBasePrice);
        Assertions.assertThat(investigationEntity.getDuration()).isEqualTo(updatedDuration);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(investigationDto.getSpecialty(), isActive);
        verify(investigationRepository).findByIdAndIsActive(expectedId, isActive);
        verify(investigationRepository).save(investigationEntity);
    }

    @Test
    public void testUpsertInvestigation_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();
        investigationDto.setId(nonExistentId);

        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(investigationDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(investigationRepository.findByIdAndIsActive(investigationDto.getId(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> investigationService.upsertInvestigation(investigationDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(investigationDto.getSpecialty(), isActive);
        verify(investigationRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testUpsertInvestigation_whenSpecialtyNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();

        when(specialtyRepository.findByNameAndIsActive(investigationDto.getSpecialty(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> investigationService.upsertInvestigation(investigationDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(investigationDto.getSpecialty(), isActive);
    }

    @Test
    public void testGetInvestigationById_whenIdExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();
        investigationDto.setId(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        when(investigationRepository.findByIdAndIsActive(id, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(investigationConverter.fromEntityToDto(investigationEntity))
                .thenReturn(investigationDto);

        //Act
        InvestigationDto returnValue = investigationService.getInvestigationById(id);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(investigationEntity.getId());
        Assertions.assertThat(returnValue.getName()).isEqualTo(investigationEntity.getName());

        //Verify
        verify(investigationRepository).findByIdAndIsActive(id, isActive);
        verify(investigationConverter).fromEntityToDto(investigationEntity);
    }

    @Test
    public void testGetInvestigationById_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;

        when(investigationRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> investigationService.getInvestigationById(nonExistentId))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(investigationRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetInvestigationByName_whenNameExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String name = "TestInvestigation";
        final boolean isActive = true;
        InvestigationDto investigationDto = InvestigationUtil.createInvestigationDto();
        investigationDto.setId(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        when(investigationRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(Optional.of(investigationEntity));
        when(investigationConverter.fromEntityToDto(investigationEntity))
                .thenReturn(investigationDto);

        //Act
        InvestigationDto returnValue = investigationService.getInvestigationByName(name);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(investigationEntity.getId());
        Assertions.assertThat(returnValue.getName()).isEqualTo(name);

        //Verify
        verify(investigationRepository).findByNameAndIsActive(name, isActive);
        verify(investigationConverter).fromEntityToDto(investigationEntity);
    }

    @Test
    public void testGetInvestigationByName_whenNameNotExist_thenThrowException() {
        //Arrange
        final String nonExistentName = "FakeInvestigation";
        final boolean isActive = true;

        when(investigationRepository.findByNameAndIsActive(nonExistentName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> investigationService.getInvestigationByName(nonExistentName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(investigationRepository).findByNameAndIsActive(nonExistentName, isActive);
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyExists_thenReturnListOfDtos() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String name = "TestSpecialty";
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(id);
        InvestigationEntity investigationEntity1 = InvestigationUtil.createInvestigationEntity(1L);
        InvestigationEntity investigationEntity2 = InvestigationUtil.createInvestigationEntity(2L);
        specialtyEntity.setInvestigations(List.of(investigationEntity1, investigationEntity2));
        InvestigationDto investigationDto1 = InvestigationDto.builder().id(1L).build();
        InvestigationDto investigationDto2 = InvestigationDto.builder().id(2L).build();

        when(specialtyRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(investigationConverter.fromEntityToDto(investigationEntity1))
                .thenReturn(investigationDto1);
        when(investigationConverter.fromEntityToDto(investigationEntity2))
                .thenReturn(investigationDto2);

        //Act
        List<InvestigationDto> returnValue = investigationService.getInvestigationBySpecialty(name);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(name, isActive);
        verify(investigationConverter).fromEntityToDto(investigationEntity1);
        verify(investigationConverter).fromEntityToDto(investigationEntity2);
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyExists_thenReturnEmptyList() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String name = "TestSpecialty";
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(id);
        InvestigationEntity investigationEntity1 = InvestigationUtil.createInvestigationEntity(1L);
        investigationEntity1.setIsActive(false);
        InvestigationEntity investigationEntity2 = InvestigationUtil.createInvestigationEntity(2L);
        investigationEntity2.setIsActive(false);
        specialtyEntity.setInvestigations(List.of(investigationEntity1, investigationEntity2));

        when(specialtyRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(Optional.of(specialtyEntity));

        //Act
        List<InvestigationDto> returnValue = investigationService.getInvestigationBySpecialty(name);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(name, isActive);
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyNotExist_thenThrowException() {
        //Arrange
        final String nonExistentName = "FakeInvestigation";
        final boolean isActive = true;

        when(specialtyRepository.findByNameAndIsActive(nonExistentName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> investigationService.getInvestigationBySpecialty(nonExistentName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(nonExistentName, isActive);
    }

    @Test
    public void testGetInvestigationByDuration_whenInvestigationsExist_thenReturnListOfDtos() {
        final Integer duration = 30;
        final boolean isActive = true;
        InvestigationEntity investigationEntity1 = InvestigationUtil.createInvestigationEntity(1L);
        InvestigationEntity investigationEntity2 = InvestigationUtil.createInvestigationEntity(2L);

        InvestigationDto investigationDto1 = InvestigationUtil.createInvestigationDto();
        investigationDto1.setId(1L);
        InvestigationDto investigationDto2 = InvestigationUtil.createInvestigationDto();
        investigationDto2.setId(2L);

        when(investigationRepository.findAllByDurationAndIsActive(duration, isActive))
                .thenReturn(List.of(investigationEntity1, investigationEntity2));
        when(investigationConverter.fromEntityToDto(investigationEntity1))
                .thenReturn(investigationDto1);
        when(investigationConverter.fromEntityToDto(investigationEntity2))
                .thenReturn(investigationDto2);

        //Act
        List<InvestigationDto> returnValue = investigationService.getInvestigationByDuration(duration);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(investigationRepository).findAllByDurationAndIsActive(duration, isActive);
        verify(investigationConverter).fromEntityToDto(investigationEntity1);
        verify(investigationConverter).fromEntityToDto(investigationEntity2);
    }

    @Test
    public void testGetInvestigationByDuration_whenInvestigationsNotExist_thenReturnEmptyList() {
        final Integer duration = 30;
        final boolean isActive = true;

        when(investigationRepository.findAllByDurationAndIsActive(duration, isActive))
                .thenReturn(Collections.emptyList());

        //Act
        List<InvestigationDto> returnValue = investigationService.getInvestigationByDuration(duration);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(investigationRepository).findAllByDurationAndIsActive(duration, isActive);
    }

    @Test
    public void testGetAllInvestigations_whenInvestigationsExist_thenReturnListOfDtos() {
        final boolean isActive = true;
        InvestigationEntity investigationEntity1 = InvestigationUtil.createInvestigationEntity(1L);
        InvestigationEntity investigationEntity2 = InvestigationUtil.createInvestigationEntity(2L);

        InvestigationDto investigationDto1 = InvestigationUtil.createInvestigationDto();
        investigationDto1.setId(1L);
        InvestigationDto investigationDto2 = InvestigationUtil.createInvestigationDto();
        investigationDto2.setId(2L);

        when(investigationRepository.findAllByIsActive(isActive))
                .thenReturn(List.of(investigationEntity1, investigationEntity2));
        when(investigationConverter.fromEntityToDto(investigationEntity1))
                .thenReturn(investigationDto1);
        when(investigationConverter.fromEntityToDto(investigationEntity2))
                .thenReturn(investigationDto2);

        //Act
        List<InvestigationDto> returnValue = investigationService.getAllInvestigations();

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(investigationRepository).findAllByIsActive(isActive);
        verify(investigationConverter).fromEntityToDto(investigationEntity1);
        verify(investigationConverter).fromEntityToDto(investigationEntity2);
    }

    @Test
    public void testGetAllInvestigations_whenInvestigationsNotExist_thenReturnEmptyList() {
        final boolean isActive = true;

        when(investigationRepository.findAllByIsActive(isActive))
                .thenReturn(Collections.emptyList());

        //Act
        List<InvestigationDto> returnValue = investigationService.getAllInvestigations();

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(investigationRepository).findAllByIsActive(isActive);
    }

    @Test
    public void testDeleteInvestigationById_whenIdExists_thenReturnId() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);
        Optional<InvestigationEntity> investigationEntityOptional = Optional.of(investigationEntity);

        when(investigationRepository.findByIdAndIsActive(id, isActive))
                .thenReturn(investigationEntityOptional);
        when(deleteUtility.softDeleteById(
                eq(id), eq(investigationEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(investigationEntity);
        when(investigationRepository.save(investigationEntity))
                .thenReturn(investigationEntity);

        //Act
        Long returnValue = investigationService.deleteInvestigationById(id);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        //Verify
        verify(investigationRepository).findByIdAndIsActive(id, isActive);
        verify(deleteUtility).softDeleteById(
                eq(id), eq(investigationEntityOptional),
                anyString(), anyString(), eq(infoContributor));
        verify(investigationRepository).save(investigationEntity);
    }

    @Test
    public void testDeleteInvestigationById_whenIdNotExist_thenReturnId() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        Optional<InvestigationEntity> investigationEntityOptional = Optional.empty();

        when(investigationRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(Optional.empty());
        when(deleteUtility.softDeleteById(
                eq(nonExistentId), eq(investigationEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(null);

        // Act
        Long returnValue = investigationService.deleteInvestigationById(nonExistentId);

        // Assert
        Assertions.assertThat(returnValue).isEqualTo(nonExistentId);

        // Verify
        verify(investigationRepository).findByIdAndIsActive(nonExistentId, isActive);
        verify(deleteUtility).softDeleteById(
                eq(nonExistentId), eq(investigationEntityOptional),
                anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testDeleteInvestigationByName_whenNameExists_thenReturnId()
            throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        final String name = "TestInvestigation";
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);
        Optional<InvestigationEntity> investigationEntityOptional = Optional.of(investigationEntity);

        when(investigationRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(investigationEntityOptional);
        when(investigationRepository.save(investigationEntity))
                .thenReturn(investigationEntity);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(investigationEntityOptional), eq(investigationRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenReturn(investigationEntity);

        // Act
        Long returnValue = investigationService.deleteInvestigationByName(name);

        // Assert
        Assertions.assertThat(returnValue).isEqualTo(id);

        // Verify
        verify(investigationRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(investigationEntityOptional), eq(investigationRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
        verify(investigationRepository).save(investigationEntity);
    }

    @Test
    public void testDeleteInvestigationByName_whenNameNotExist_thenThrowException()
            throws DataNotFoundException {
        //Arrange
        final boolean isActive = true;
        String name = "FakeInvestigation";
        Optional<InvestigationEntity> investigationEntityOptional = Optional.empty();

        when(investigationRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(investigationEntityOptional);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(investigationEntityOptional), eq(investigationRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenThrow(DataNotFoundException.class);

        // Act && Assert
        Assertions.assertThatThrownBy(() -> investigationService.deleteInvestigationByName(name))
                .isInstanceOf(DataNotFoundException.class);

        // Verify
        verify(investigationRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(investigationEntityOptional), eq(investigationRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testGetInvestigationWithPricing_whenDocAndInvValid_thenReturnMapWithOneEntry() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String doctorName = "TestDoctor";
        final String investigationName = "TestInvestigation";
        final boolean isActive = true;
        final Double expectedPrice = 225D;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        InvestigationEntity investigationEntity = InvestigationUtil.createInvestigationEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.of(investigationEntity));

        //Act
        Map<String, Map<String, Double>> returnValue =
                investigationService.getInvestigationWithPricing(doctorName, investigationName);

        //Assert
        Assertions.assertThat(returnValue.get(doctorName).get(investigationName)).isEqualTo(expectedPrice);
        Assertions.assertThat(returnValue.containsKey(doctorName)).isTrue();
        Assertions.assertThat(returnValue.get(doctorName).containsKey(investigationName)).isTrue();

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvBlank_thenReturnMapWithTwoEntries() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final String doctorName = "TestDoctor";
        final String investigationName = "";
        final boolean isActive = true;
        final Double expectedPrice1 = 225D;
        final Double expectedPrice2 = 300D;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        InvestigationEntity investigationEntity1 = InvestigationUtil.createInvestigationEntity(1L);
        investigationEntity1.setName("TestInvestigation1");
        investigationEntity1.setBasePrice(150D);

        InvestigationEntity investigationEntity2 = InvestigationUtil.createInvestigationEntity(2L);
        investigationEntity2.setName("TestInvestigation2");
        investigationEntity2.setBasePrice(200D);

        InvestigationEntity investigationEntity3 = InvestigationUtil.createInvestigationEntity(3L);
        investigationEntity3.setName("TestInvestigation3");
        investigationEntity3.setBasePrice(250D);
        investigationEntity3.setIsActive(false);

        specialtyEntity.setInvestigations(List.of(investigationEntity1, investigationEntity2, investigationEntity3));
        doctorEntity.setSpecialty(specialtyEntity);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));

        //Act
        Map<String, Map<String, Double>> returnValue =
                investigationService.getInvestigationWithPricing(doctorName, investigationName);

        //Assert
        Assertions.assertThat(returnValue.get(doctorName).get(investigationEntity1.getName()))
                .isEqualTo(expectedPrice1);
        Assertions.assertThat(returnValue.get(doctorName).get(investigationEntity2.getName()))
                .isEqualTo(expectedPrice2);

        Assertions.assertThat(returnValue.containsKey(doctorName)).isTrue();
        Assertions.assertThat(returnValue.get(doctorName).containsKey(investigationEntity1.getName()))
                .isTrue();
        Assertions.assertThat(returnValue.get(doctorName).containsKey(investigationEntity2.getName()))
                .isTrue();
        Assertions.assertThat(returnValue.get(doctorName).containsKey(investigationEntity3.getName()))
                .isFalse();

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvNotValid_thenThrowException() {
        //Arrange
        final Long id = 1L;
        final String doctorName = "TestDoctor";
        final String investigationName = "FakeInvestigation";
        final boolean isActive = true;

        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(investigationRepository.findByNameAndIsActive(investigationName, isActive))
                .thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> investigationService.getInvestigationWithPricing(doctorName, investigationName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
        verify(investigationRepository).findByNameAndIsActive(investigationName, isActive);
    }

    @Test
    public void testGetInvestigationWithPricing_whenDocNotValid_thenThrowException() {
        //Arrange
        final String doctorName = "FakeDoctor";
        final String investigationName = "TestInvestigation";
        final boolean isActive = true;

        when(doctorRepository.findByNameAndIsActive(doctorName, isActive))
                .thenReturn(Optional.empty());

        //Act && Assert
        Assertions.assertThatThrownBy(() -> investigationService.getInvestigationWithPricing(doctorName, investigationName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(doctorName, isActive);
    }
}

