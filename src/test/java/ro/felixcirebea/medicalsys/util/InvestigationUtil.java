package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

public class InvestigationUtil {

    public static final String INVESTIGATION_NAME = "TestInvestigation";
    public static final SpecialtyEntity SPECIALTY = SpecialtyUtil.createSpecialtyEntity(1L);
    public static final String SPECIALTY_NAME = "TestSpecialty";
    public static final double BASE_PRICE = 150D;
    public static final int DURATION = 30;

    public static InvestigationDto createInvestigationDto() {
        return InvestigationDto.builder()
                .name(INVESTIGATION_NAME)
                .specialty(SPECIALTY_NAME)
                .basePrice(BASE_PRICE)
                .duration(DURATION)
                .build();
    }

    public static InvestigationEntity createInvestigationEntity(Long id) {
        InvestigationEntity investigationEntity = new InvestigationEntity();
        investigationEntity.setId(id);
        investigationEntity.setName(INVESTIGATION_NAME);
        investigationEntity.setSpecialty(SPECIALTY);
        investigationEntity.setBasePrice(BASE_PRICE);
        investigationEntity.setDuration(DURATION);

        return investigationEntity;
    }

}
