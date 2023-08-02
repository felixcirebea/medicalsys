package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

@Component
public class SpecialtyConverter {

    public SpecialtyEntity fromDtoToEntity(SpecialtyDto dto) {
        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setName(dto.getName());
        return entity;
    }

    public SpecialtyDto fromEntityToDto(SpecialtyEntity entity) {
        return SpecialtyDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

}
