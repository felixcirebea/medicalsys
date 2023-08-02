package ro.felixcirebea.medicalsys.service;

import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class InvestigationService {

    private final InvestigationRepository investigationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final InvestigationConverter investigationConverter;
    private final Contributor infoContributor;

    public InvestigationService(InvestigationRepository investigationRepository, SpecialtyRepository specialtyRepository, InvestigationConverter investigationConverter, Contributor infoContributor) {
        this.investigationRepository = investigationRepository;
        this.specialtyRepository = specialtyRepository;
        this.investigationConverter = investigationConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertInvestigation(InvestigationDto investigationDto) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(investigationDto.getSpecialty())
                .orElseThrow(() -> new RuntimeException("Data not found!"));
        if (investigationDto.getId() != null) {
            return updateInvestigation(investigationDto, specialtyEntity);
        }
        return investigationRepository.save(
                investigationConverter.fromDtoToEntity(investigationDto, specialtyEntity)
        ).getId();
    }

    private Long updateInvestigation(InvestigationDto investigationDto, SpecialtyEntity specialtyEntity) {
        InvestigationEntity investigationEntity = investigationRepository.findById(investigationDto.getId())
                .orElseThrow(() -> new RuntimeException("Data not found!"));

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);

        if (investigationDto.getBasePrice() != null) {
            investigationEntity.setBasePrice(investigationDto.getBasePrice());
        }

        return investigationRepository.save(investigationEntity).getId();
    }

    public InvestigationDto getInvestigationById(String investigationId) {
        InvestigationEntity investigationEntity = investigationRepository.findById(Long.valueOf(investigationId))
                .orElseThrow(() -> new RuntimeException("Data not found"));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public InvestigationDto getInvestigationByName(String investigationName) {
        InvestigationEntity investigationEntity = investigationRepository.findByName(investigationName)
                .orElseThrow(() -> new RuntimeException("Data not found"));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public List<InvestigationDto> getInvestigationBySpecialty(String specialtyName) {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new RuntimeException("No such specialty"));
        return specialtyEntity.getInvestigations().stream().map(investigationConverter::fromEntityToDto).toList();
    }

    public List<InvestigationDto> getAllInvestigations() {
        return StreamSupport.stream(investigationRepository.findAll().spliterator(), false)
                .map(investigationConverter::fromEntityToDto).toList();
    }

    public Long deleteInvestigationById(String investigationId) {
        Optional<InvestigationEntity> investigationEntityOptional = investigationRepository
                .findById(Long.valueOf(investigationId));
        if (investigationEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            return Long.valueOf(investigationId);
        }
        investigationRepository.deleteById(Long.valueOf(investigationId));
        return Long.valueOf(investigationId);
    }


    public Long deleteInvestigationByName(String investigationName) {
        InvestigationEntity investigationEntity = investigationRepository
                .findByName(investigationName).orElseThrow(() -> new RuntimeException("Data not found"));
        infoContributor.incrementFailedDeleteOperations(); //don't know if it goes through here

        investigationRepository.deleteById(investigationEntity.getId());
        return investigationEntity.getId();
    }
}
