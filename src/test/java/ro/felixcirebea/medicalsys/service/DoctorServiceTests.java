package ro.felixcirebea.medicalsys.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.felixcirebea.medicalsys.converter.DoctorConverter;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTests {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private DoctorConverter doctorConverter;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DeleteUtility deleteUtility;

    @Mock
    private Contributor infoContributor;

    @Mock
    private WorkingHoursService workingHoursService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    public void testUpsertDoctor_whenDtoIsValid_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(doctorDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(doctorConverter.fromDtoToEntity(doctorDto, specialtyEntity))
                .thenReturn(doctorEntity);
        when(doctorRepository.save(doctorEntity)).thenReturn(doctorEntity);

        //Act
        Long returnValue = doctorService.upsertDoctor(doctorDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(doctorDto.getSpecialty(), isActive);
        verify(doctorConverter).fromDtoToEntity(doctorDto, specialtyEntity);
        verify(doctorRepository).save(doctorEntity);
    }

    @Test
    public void testUpsertDoctor_whenDtoIdNotNull_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        final double updatedPriceRate = 100D;
        final String updatedName = "UpdatedName";
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        doctorDto.setId(expectedId);
        doctorDto.setName(updatedName);
        doctorDto.setPriceRate(updatedPriceRate);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(expectedId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(doctorDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(doctorRepository.findByIdAndIsActive(expectedId, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(doctorRepository.save(doctorEntity)).thenReturn(doctorEntity);

        //Act
        Long returnValue = doctorService.upsertDoctor(doctorDto);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(expectedId);
        Assertions.assertThat(doctorEntity.getName()).isEqualTo(updatedName);
        Assertions.assertThat(doctorEntity.getPriceRate()).isEqualTo(updatedPriceRate);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(doctorDto.getSpecialty(), true);
        verify(doctorRepository).findByIdAndIsActive(doctorDto.getId(), isActive);
        verify(doctorRepository).save(doctorEntity);
    }

    @Test
    public void testUpsertDoctor_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        doctorDto.setId(nonExistentId);
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);

        when(specialtyRepository.findByNameAndIsActive(doctorDto.getSpecialty(), isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(doctorRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.upsertDoctor(doctorDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(doctorDto.getSpecialty(), true);
        verify(doctorRepository).findByIdAndIsActive(doctorDto.getId(), isActive);
    }

    @Test
    public void testUpsertDoctor_whenSpecialtyNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        final String nonExistentSpecialty = "FakeSpecialty";
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        doctorDto.setId(nonExistentId);
        doctorDto.setSpecialty(nonExistentSpecialty);

        when(specialtyRepository.findByNameAndIsActive(doctorDto.getSpecialty(), isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.upsertDoctor(doctorDto))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(doctorDto.getSpecialty(), true);
    }

    @Test
    public void testGetDoctorById_whenIdExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        doctorDto.setId(expectedId);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(expectedId);

        when(doctorRepository.findByIdAndIsActive(expectedId, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(doctorConverter.fromEntityToDto(doctorEntity))
                .thenReturn(doctorDto);

        //Act
        DoctorDto returnValue = doctorService.getDoctorById(expectedId);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(expectedId);
        Assertions.assertThat(returnValue.getName()).isEqualTo(doctorEntity.getName());

        //Verify
        verify(doctorRepository).findByIdAndIsActive(expectedId, isActive);
        verify(doctorConverter).fromEntityToDto(doctorEntity);
    }

    @Test
    public void testGetDoctorById_whenIdNotExist_thenThrowException() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;

        when(doctorRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.getDoctorById(nonExistentId))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByIdAndIsActive(nonExistentId, isActive);
    }

    @Test
    public void testGetDoctorByName_whenNameExists_thenReturnDto() throws DataNotFoundException {
        //Arrange
        final Long expectedId = 1L;
        final boolean isActive = true;
        final String inputName = "TestDoctor";
        DoctorDto doctorDto = DoctorUtil.createDoctorDto();
        doctorDto.setId(expectedId);
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(expectedId);

        when(doctorRepository.findByNameAndIsActive(inputName, isActive))
                .thenReturn(Optional.of(doctorEntity));
        when(doctorConverter.fromEntityToDto(doctorEntity))
                .thenReturn(doctorDto);

        //Act
        DoctorDto returnValue = doctorService.getDoctorByName(inputName);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(expectedId);
        Assertions.assertThat(returnValue.getName()).isEqualTo(doctorEntity.getName());

        //Verify
        verify(doctorRepository).findByNameAndIsActive(inputName, isActive);
        verify(doctorConverter).fromEntityToDto(doctorEntity);
    }

    @Test
    public void testGetDoctorByName_whenNameNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String nonExistentName = "FakeDoctor";

        when(doctorRepository.findByNameAndIsActive(nonExistentName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.getDoctorByName(nonExistentName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(nonExistentName, isActive);
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyExists_thenReturnListOfDtos() throws DataNotFoundException {
        //Arrange
        final String specialtyName = "TestSpecialty";
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);
        DoctorEntity doctorEntity1 = DoctorUtil.createDoctorEntity(1L);
        DoctorEntity doctorEntity2 = DoctorUtil.createDoctorEntity(2L);
        specialtyEntity.setDoctors(List.of(doctorEntity1, doctorEntity2));
        DoctorDto doctorDto1 = DoctorDto.builder().id(1L).build();
        DoctorDto doctorDto2 = DoctorDto.builder().id(2L).build();

        when(specialtyRepository.findByNameAndIsActive(specialtyName, isActive))
                .thenReturn(Optional.of(specialtyEntity));
        when(doctorConverter.fromEntityToDto(doctorEntity1))
                .thenReturn(doctorDto1);
        when(doctorConverter.fromEntityToDto(doctorEntity2))
                .thenReturn(doctorDto2);

        //Act
        List<DoctorDto> returnValue = doctorService.getDoctorsBySpecialty(specialtyName);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(specialtyName, isActive);
        verify(doctorConverter).fromEntityToDto(doctorEntity1);
        verify(doctorConverter).fromEntityToDto(doctorEntity2);
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyExists_thenReturnEmptyList() throws DataNotFoundException {
        //Arrange
        final String specialtyName = "TestSpecialty";
        final boolean isActive = true;
        SpecialtyEntity specialtyEntity = SpecialtyUtil.createSpecialtyEntity(1L);
        specialtyEntity.setDoctors(Collections.emptyList());

        when(specialtyRepository.findByNameAndIsActive(specialtyName, isActive))
                .thenReturn(Optional.of(specialtyEntity));

        //Act
        List<DoctorDto> returnValue = doctorService.getDoctorsBySpecialty(specialtyName);

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(specialtyName, isActive);
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyNotExist_thenThrowException() {
        //Arrange
        final boolean isActive = true;
        final String nonExistentName = "TestSpecialty";

        when(specialtyRepository.findByNameAndIsActive(nonExistentName, isActive))
                .thenReturn(Optional.empty());

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.getDoctorsBySpecialty(nonExistentName))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(specialtyRepository).findByNameAndIsActive(nonExistentName, isActive);
    }

    @Test
    public void testGetAllDoctors_whenDoctorsExist_thenReturnListOfDtos() {
        //Arrange
        final boolean isActive = true;
        DoctorEntity doctorEntity1 = DoctorUtil.createDoctorEntity(1L);
        DoctorEntity doctorEntity2 = DoctorUtil.createDoctorEntity(2L);
        DoctorDto doctorDto1 = DoctorDto.builder().id(1L).build();
        DoctorDto doctorDto2 = DoctorDto.builder().id(2L).build();

        when(doctorRepository.findAllByIsActive(isActive))
                .thenReturn(List.of(doctorEntity1, doctorEntity2));
        when(doctorConverter.fromEntityToDto(doctorEntity1))
                .thenReturn(doctorDto1);
        when(doctorConverter.fromEntityToDto(doctorEntity2))
                .thenReturn(doctorDto2);

        //Act
        List<DoctorDto> returnValue = doctorService.getAllDoctors();

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isFalse();
        Assertions.assertThat(returnValue.size()).isEqualTo(2);

        //Verify
        verify(doctorRepository).findAllByIsActive(isActive);
        verify(doctorConverter).fromEntityToDto(doctorEntity1);
        verify(doctorConverter).fromEntityToDto(doctorEntity2);
    }

    @Test
    public void testGetAllDoctors_whenDoctorsNotExist_thenReturnEmptyList() {
        //Arrange
        final boolean isActive = true;

        when(doctorRepository.findAllByIsActive(isActive))
                .thenReturn(Collections.emptyList());

        //Act
        List<DoctorDto> returnValue = doctorService.getAllDoctors();

        //Assert
        Assertions.assertThat(returnValue.isEmpty()).isTrue();

        //Verify
        verify(doctorRepository).findAllByIsActive(isActive);
    }

    @Test
    public void testDeleteDoctorById_whenIdExists_thenReturnId() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        Optional<DoctorEntity> doctorEntityOptional = Optional.of(doctorEntity);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        doctorEntity.setVacation(List.of(vacationEntity1, vacationEntity2));

        when(doctorRepository.findByIdAndIsActive(id, isActive))
                .thenReturn(doctorEntityOptional);
        when(deleteUtility.softDeleteById(
                eq(id), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(doctorEntity);
        when(doctorRepository.save(doctorEntity))
                .thenReturn(doctorEntity);
        when(workingHoursService.deleteAllWorkingHoursForDoctor(doctorEntity))
                .thenReturn(null);
        when(appointmentService.cancelAllAppointmentForDoctor(doctorEntity))
                .thenReturn(null);

        //Act
        Long returnValue = doctorService.deleteDoctorById(id);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);
        Assertions.assertThat(doctorEntity.getVacation().get(0).getStatus())
                .isEqualTo(VacationStatus.CANCELED);
        Assertions.assertThat(doctorEntity.getVacation().get(1).getStatus())
                .isEqualTo(VacationStatus.CANCELED);

        //Verify
        verify(doctorRepository).findByIdAndIsActive(id, isActive);
        verify(deleteUtility).softDeleteById(
                eq(id), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor));
        verify(doctorRepository).save(doctorEntity);
        verify(workingHoursService).deleteAllWorkingHoursForDoctor(doctorEntity);
        verify(appointmentService).cancelAllAppointmentForDoctor(doctorEntity);
    }

    @Test
    public void testDeleteDoctorById_whenIdExistsAndVacationDone_thenReturnId() {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        Optional<DoctorEntity> doctorEntityOptional = Optional.of(doctorEntity);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        vacationEntity1.setStatus(VacationStatus.DONE);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        doctorEntity.setVacation(List.of(vacationEntity1, vacationEntity2));

        when(doctorRepository.findByIdAndIsActive(id, isActive))
                .thenReturn(doctorEntityOptional);
        when(deleteUtility.softDeleteById(
                eq(id), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(doctorEntity);
        when(doctorRepository.save(doctorEntity))
                .thenReturn(doctorEntity);
        when(workingHoursService.deleteAllWorkingHoursForDoctor(doctorEntity))
                .thenReturn(null);
        when(appointmentService.cancelAllAppointmentForDoctor(doctorEntity))
                .thenReturn(null);

        //Act
        Long returnValue = doctorService.deleteDoctorById(id);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);
        Assertions.assertThat(doctorEntity.getVacation().get(0).getStatus())
                .isEqualTo(VacationStatus.DONE);
        Assertions.assertThat(doctorEntity.getVacation().get(1).getStatus())
                .isEqualTo(VacationStatus.CANCELED);

        //Verify
        verify(doctorRepository).findByIdAndIsActive(id, isActive);
        verify(deleteUtility).softDeleteById(
                eq(id), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor));
        verify(doctorRepository).save(doctorEntity);
        verify(workingHoursService).deleteAllWorkingHoursForDoctor(doctorEntity);
        verify(appointmentService).cancelAllAppointmentForDoctor(doctorEntity);
    }

    @Test
    public void testDeleteDoctorById_whenIdNotExist_thenReturnId() {
        //Arrange
        final Long nonExistentId = 999L;
        final boolean isActive = true;
        Optional<DoctorEntity> doctorEntityOptional = Optional.empty();

        when(doctorRepository.findByIdAndIsActive(nonExistentId, isActive))
                .thenReturn(doctorEntityOptional);
        when(deleteUtility.softDeleteById(
                eq(nonExistentId), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor)))
                .thenReturn(null);

        //Act
        Long returnValue = doctorService.deleteDoctorById(nonExistentId);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(nonExistentId);

        //Verify
        verify(doctorRepository).findByIdAndIsActive(nonExistentId, isActive);
        verify(deleteUtility).softDeleteById(
                eq(nonExistentId), eq(doctorEntityOptional),
                anyString(), anyString(), eq(infoContributor));
    }

    @Test
    public void testDeleteDoctorByName_whenNameExists_thenReturnId() throws DataNotFoundException {
        //Arrange
        final Long id = 1L;
        final boolean isActive = true;
        DoctorEntity doctorEntity = DoctorUtil.createDoctorEntity(id);
        String name = doctorEntity.getName();
        Optional<DoctorEntity> doctorEntityOptional = Optional.of(doctorEntity);
        VacationEntity vacationEntity1 = VacationUtil.createVacationEntity(1L);
        VacationEntity vacationEntity2 = VacationUtil.createVacationEntity(2L);
        doctorEntity.setVacation(List.of(vacationEntity1, vacationEntity2));

        when(doctorRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(doctorEntityOptional);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(doctorEntityOptional), eq(doctorRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenReturn(doctorEntity);
        when(doctorRepository.save(doctorEntity))
                .thenReturn(doctorEntity);
        when(workingHoursService.deleteAllWorkingHoursForDoctor(doctorEntity))
                .thenReturn(null);
        when(appointmentService.cancelAllAppointmentForDoctor(doctorEntity))
                .thenReturn(null);

        //Act
        Long returnValue = doctorService.deleteDoctorByName(name);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(id);
        Assertions.assertThat(doctorEntity.getVacation().get(0).getStatus())
                .isEqualTo(VacationStatus.CANCELED);
        Assertions.assertThat(doctorEntity.getVacation().get(1).getStatus())
                .isEqualTo(VacationStatus.CANCELED);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(doctorEntityOptional), eq(doctorRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
        verify(doctorRepository).save(doctorEntity);
        verify(workingHoursService).deleteAllWorkingHoursForDoctor(doctorEntity);
        verify(appointmentService).cancelAllAppointmentForDoctor(doctorEntity);
    }

    @Test
    public void testDeleteDoctorByName_whenNameNotExist_thenReturnId() throws DataNotFoundException {
        //Arrange
        final boolean isActive = true;
        final String name = "FakeDoctor";
        Optional<DoctorEntity> doctorEntityOptional = Optional.empty();

        when(doctorRepository.findByNameAndIsActive(name, isActive))
                .thenReturn(doctorEntityOptional);
        when(deleteUtility.softDeleteByField(
                eq(name), eq(doctorEntityOptional), eq(doctorRepository),
                anyString(), anyString(), anyString(), eq(infoContributor)))
                .thenThrow(DataNotFoundException.class);

        //Act && assert
        Assertions.assertThatThrownBy(() -> doctorService.deleteDoctorByName(name))
                .isInstanceOf(DataNotFoundException.class);

        //Verify
        verify(doctorRepository).findByNameAndIsActive(name, isActive);
        verify(deleteUtility).softDeleteByField(
                eq(name), eq(doctorEntityOptional), eq(doctorRepository),
                anyString(), anyString(), anyString(), eq(infoContributor));
    }
}
