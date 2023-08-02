package ro.felixcirebea.medicalsys.service;

import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.DoctorConverter;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
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

    public Long upsertDoctor(DoctorDto doctorDto) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(doctorDto.getSpecialty())
                .orElseThrow(() -> new RuntimeException("Data not found"));
        if (doctorDto.getId() != null) {
            return updateDoctor(doctorDto, specialtyEntity);
        }
        return doctorRepository.save(doctorConverter.fromDtoToEntity(doctorDto, specialtyEntity)).getId();
    }

    private Long updateDoctor(DoctorDto doctorDto, SpecialtyEntity specialtyEntity) {
        DoctorEntity doctorEntity = doctorRepository.findById(doctorDto.getId())
                .orElseThrow(() -> new RuntimeException("Data not found"));
        doctorEntity.setName(doctorDto.getName());
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(doctorDto.getPriceRate());
        return doctorRepository.save(doctorEntity).getId();
    }

    public DoctorDto getDoctorById(String doctorId) {
        DoctorEntity doctorEntity = doctorRepository.findById(Long.valueOf(doctorId))
                .orElseThrow(() -> new RuntimeException("Data not found"));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public DoctorDto getDoctorByName(String doctorName) {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new RuntimeException("Data not found"));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public List<DoctorDto> getAllDoctors() {
        return StreamSupport.stream(doctorRepository.findAll().spliterator(), false)
                .map(doctorConverter::fromEntityToDto).toList();
    }

    public Long deleteDoctorById(String doctorId) {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findById(Long.valueOf(doctorId));
        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            return Long.valueOf(doctorId);
        }
        doctorRepository.deleteById(Long.valueOf(doctorId));
        return Long.valueOf(doctorId);
    }

    public Long deleteDoctorByName(String doctorName) {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new RuntimeException("Data not found"));
        infoContributor.incrementFailedDeleteOperations(); //don't reach this far - refactor

        doctorRepository.deleteById(doctorEntity.getId());
        return doctorEntity.getId();
    }
}
