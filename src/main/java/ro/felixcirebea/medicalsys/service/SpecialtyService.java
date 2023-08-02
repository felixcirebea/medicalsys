package ro.felixcirebea.medicalsys.service;

import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.SpecialtyConverter;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyConverter specialtyConverter;
    private final Contributor infoContributor;

    public SpecialtyService(SpecialtyRepository specialtyRepository, SpecialtyConverter specialtyConverter, Contributor infoContributor) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyConverter = specialtyConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertSpecialty(SpecialtyDto dto) {
        if (dto.getId() != null) {
            return updateSpecialty(dto);
        }
        return specialtyRepository.save(specialtyConverter.fromDtoToEntity(dto)).getId();
    }

    private Long updateSpecialty(SpecialtyDto dto) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Data not found"));
        specialtyEntity.setName(dto.getName());
        return specialtyRepository.save(specialtyEntity).getId();
    }

    public String getSpecialtyById(String id) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Data not found"));
        return specialtyEntity.getName();
    }

    public List<SpecialtyDto> getAllSpecialties() {
        return StreamSupport.stream(specialtyRepository.findAll().spliterator(), false)
                .map(specialtyConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteSpecialtyById(String id) {
        Optional<SpecialtyEntity> specialtyEntityOptional = specialtyRepository.findById(Long.valueOf(id));
        if (specialtyEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            return Long.valueOf(id);
        }
        specialtyRepository.deleteById(Long.valueOf(id));
        return Long.valueOf(id);
    }

}
