package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;


@Component
public class InvestigationConverter {

    private static final double DEFAULT_BASE_PRICE = 50D;

    public InvestigationEntity fromDtoToEntity(InvestigationDto investigationDto, SpecialtyEntity specialtyEntity) {
        InvestigationEntity investigationEntity = new InvestigationEntity();

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setDuration(investigationDto.getDuration());

        if (investigationDto.getBasePrice() == null) {
            investigationEntity.setBasePrice(DEFAULT_BASE_PRICE);
        } else {
            investigationEntity.setBasePrice(investigationDto.getBasePrice());
        }

        return investigationEntity;
    }

    public InvestigationDto fromEntityToDto(InvestigationEntity investigationEntity) {
        return InvestigationDto.builder()
                .id(investigationEntity.getId())
                .name(investigationEntity.getName())
                .specialty(investigationEntity.getSpecialty().getName())
                .basePrice(investigationEntity.getBasePrice())
                .duration(investigationEntity.getDuration())
                .build();
    }
}
