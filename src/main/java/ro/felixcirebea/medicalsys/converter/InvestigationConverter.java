package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.Dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;


@Component
public class InvestigationConverter {

    public InvestigationEntity fromDtoToEntity(InvestigationDto investigationDto, SpecialtyEntity specialtyEntity) {
        InvestigationEntity investigationEntity = new InvestigationEntity();

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);

        if (investigationDto.getBasePrice() == null) {
            investigationEntity.setBasePrice(50.0);
        } else {
            investigationEntity.setBasePrice(investigationDto.getBasePrice());
        }

        return investigationEntity;
    }

}
