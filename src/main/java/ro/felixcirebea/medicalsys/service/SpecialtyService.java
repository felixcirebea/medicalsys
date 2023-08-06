package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.SpecialtyConverter;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyConverter specialtyConverter;
    private final Contributor infoContributor;

    public SpecialtyService(SpecialtyRepository specialtyRepository,
                            SpecialtyConverter specialtyConverter,
                            Contributor infoContributor) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyConverter = specialtyConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertSpecialty(SpecialtyDto specialtyDto) throws DataNotFoundException {
        if (specialtyDto.getId() != null) {
            return updateSpecialty(specialtyDto);
        }
        log.info(String.format("Specialty with name %s was saved", specialtyDto.getName()));
        return specialtyRepository.save(specialtyConverter.fromDtoToEntity(specialtyDto)).getId();
    }

    private Long updateSpecialty(SpecialtyDto specialtyDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(specialtyDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        specialtyEntity.setName(specialtyDto.getName());
        log.info("%s with id %s was updated as follows:" + specialtyDto);
        return specialtyRepository.save(specialtyEntity).getId();
    }

    public String getSpecialtyById(Long specialtyId) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        return specialtyEntity.getName();
    }

    public String getSpecialtyByName(String specialtyName) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Specialty with name %s not found", specialtyName)));
        return specialtyEntity.getName();
    }

    public List<SpecialtyDto> getAllSpecialties() {
        return StreamSupport.stream(specialtyRepository.findAll().spliterator(), false)
                .map(specialtyConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteSpecialtyById(Long specialtyId) {
        Optional<SpecialtyEntity> specialtyEntityOptional = specialtyRepository.findById(specialtyId);
        if (specialtyEntityOptional.isEmpty()) {
            log.warn(String.format("Can't delete specialty with id %s because it doesn't exist", specialtyId));
            infoContributor.incrementFailedDeleteOperations();
            return specialtyId;
        }
        specialtyRepository.deleteById(specialtyId);
        log.info(String.format("Specialty with id %s deleted", specialtyId));
        return specialtyId;
    }

    public Long deleteSpecialtyByName(String specialtyName) throws DataNotFoundException {
        Optional<SpecialtyEntity> specialtyEntityOptional = specialtyRepository.findByName(specialtyName);

        if (specialtyEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            throw new DataNotFoundException(String.format("Specialty with name %s not found", specialtyName));
        }

        specialtyRepository.deleteById(specialtyEntityOptional.get().getId());
        return specialtyEntityOptional.get().getId();
    }

}
