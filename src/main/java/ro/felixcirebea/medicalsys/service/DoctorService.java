package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.DoctorConverter;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorConverter doctorConverter;
    private final Contributor infoContributor;

    public DoctorService(DoctorRepository doctorRepository,
                         SpecialtyRepository specialtyRepository,
                         DoctorConverter doctorConverter, Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorConverter = doctorConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertDoctor(DoctorDto doctorDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(doctorDto.getSpecialty())
                .orElseThrow(() -> new DataNotFoundException(String.format(
                        "Specialty %s not found", doctorDto.getSpecialty())));
        if (doctorDto.getId() != null) {
            return updateDoctor(doctorDto, specialtyEntity);
        }
        log.info(String.format("Doctor with name %s was saved", doctorDto.getName()));
        return doctorRepository.save(doctorConverter.fromDtoToEntity(doctorDto, specialtyEntity)).getId();
    }

    private Long updateDoctor(DoctorDto doctorDto, SpecialtyEntity specialtyEntity) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findById(doctorDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        doctorEntity.setName(doctorDto.getName());
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(doctorDto.getPriceRate());
        log.info(String.format("Doctor with id %s was updated", doctorDto.getId()));
        return doctorRepository.save(doctorEntity).getId();
    }

    public DoctorDto getDoctorById(Long doctorId) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public DoctorDto getDoctorByName(String doctorName) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Doctor with name %s not found", doctorName)));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public List<DoctorDto> getAllDoctors() {
        return StreamSupport.stream(doctorRepository.findAll().spliterator(), false)
                .map(doctorConverter::fromEntityToDto).toList();
    }

    public Long deleteDoctorById(Long doctorId) {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findById(doctorId);
        if (doctorEntityOptional.isEmpty()) {
            log.warn(String.format("Can't delete doctor with id %s because it doesn't exist", doctorId));
            infoContributor.incrementFailedDeleteOperations();
            return doctorId;
        }
        doctorRepository.deleteById(doctorId);
        log.info(String.format("Doctor with id %s deleted", doctorId));
        return doctorId;
    }

    public Long deleteDoctorByName(String doctorName) throws DataNotFoundException {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findByName(doctorName);

        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format("Can't delete doctor with name %s. Name doesn't exist", doctorName));
            throw new DataNotFoundException(String.format("Doctor with name %s not found", doctorName));
        }

        doctorRepository.deleteById(doctorEntityOptional.get().getId());
        return doctorEntityOptional.get().getId();
    }
}
