package ro.felixcirebea.medicalsys.service;

import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.Dto.InvestigationDto;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;

@Service
public class InvestigationService {

    private final InvestigationRepository investigationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final InvestigationConverter investigationConverter;

    public InvestigationService(InvestigationRepository investigationRepository, SpecialtyRepository specialtyRepository, InvestigationConverter investigationConverter) {
        this.investigationRepository = investigationRepository;
        this.specialtyRepository = specialtyRepository;
        this.investigationConverter = investigationConverter;
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

}
