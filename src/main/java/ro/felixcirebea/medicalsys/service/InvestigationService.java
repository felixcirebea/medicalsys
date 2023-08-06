package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class InvestigationService {

    private final InvestigationRepository investigationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final InvestigationConverter investigationConverter;
    private final Contributor infoContributor;

    public InvestigationService(InvestigationRepository investigationRepository,
                                SpecialtyRepository specialtyRepository,
                                InvestigationConverter investigationConverter,
                                Contributor infoContributor) {
        this.investigationRepository = investigationRepository;
        this.specialtyRepository = specialtyRepository;
        this.investigationConverter = investigationConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertInvestigation(InvestigationDto investigationDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(investigationDto.getSpecialty())
                .orElseThrow(() -> new DataNotFoundException(String.format(
                        "Specialty %s not found", investigationDto.getSpecialty())));
        if (investigationDto.getId() != null) {
            return updateInvestigation(investigationDto, specialtyEntity);
        }
        log.info(String.format("Investigation with name %s was saved", investigationDto.getName()));
        return investigationRepository.save(investigationConverter.fromDtoToEntity(investigationDto, specialtyEntity))
                .getId();
    }

    private Long updateInvestigation(InvestigationDto investigationDto, SpecialtyEntity specialtyEntity)
            throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findById(investigationDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setDuration(investigationDto.getDuration());
        if (investigationDto.getBasePrice() != null) {
            investigationEntity.setBasePrice(investigationDto.getBasePrice());
        }

        log.info(String.format("Investigation with id %s was updated", investigationDto.getId()));
        return investigationRepository.save(investigationEntity).getId();
    }

    public InvestigationDto getInvestigationById(Long investigationId) throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public InvestigationDto getInvestigationByName(String investigationName) throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findByName(investigationName)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Investigation with name %s not found", investigationName)));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public List<InvestigationDto> getInvestigationBySpecialty(String specialtyName) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new DataNotFoundException(String.format("Specialty %s not found", specialtyName)));
        return specialtyEntity.getInvestigations().stream().map(investigationConverter::fromEntityToDto).toList();
    }

    public List<InvestigationDto> getInvestigationByDuration(Integer duration) {
        return investigationRepository.findAllByDuration(duration).stream()
                .map(investigationConverter::fromEntityToDto).toList();
    }

    public List<InvestigationDto> getAllInvestigations() {
        return StreamSupport.stream(investigationRepository.findAll().spliterator(), false)
                .map(investigationConverter::fromEntityToDto).toList();
    }

    public Long deleteInvestigationById(Long investigationId) {
        Optional<InvestigationEntity> investigationEntityOptional = investigationRepository
                .findById(investigationId);
        if (investigationEntityOptional.isEmpty()) {
            log.warn(String.format("Can't delete investigation with id %s because it doesn't exist", investigationId));
            infoContributor.incrementFailedDeleteOperations();
            return investigationId;
        }
        investigationRepository.deleteById(investigationId);
        log.info(String.format("Investigation with id %s deleted", investigationId));
        return investigationId;
    }


    public Long deleteInvestigationByName(String investigationName) throws DataNotFoundException {
        Optional<InvestigationEntity> investigationEntityOptional = investigationRepository
                .findByName(investigationName);

        if (investigationEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            throw new DataNotFoundException(String.format("Investigation with name %s not found", investigationName));
        }

        investigationRepository.deleteById(investigationEntityOptional.get().getId());
        return investigationEntityOptional.get().getId();
    }


}
