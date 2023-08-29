package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.util.InvestigationUtil;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;

public class InvestigationConverterTests {

    public static final Long ID = 1L;
    public static final Double DEFAULT_BASE_PRICE = 50D;

    private InvestigationConverter converter;

    private InvestigationDto inputDto;

    private InvestigationEntity inputEntity;

    private SpecialtyEntity specialtyEntity;

    @BeforeEach
    public void setUp() {

        converter = new InvestigationConverter();

        inputDto = InvestigationUtil.createInvestigationDto();
        inputEntity = InvestigationUtil.createInvestigationEntity(ID);
        specialtyEntity = SpecialtyUtil.createSpecialtyEntity(ID);
    }

    @Test
    public void testFromDtoToEntity_whenBasePriceNotNull_thenReturnValueHasInputBasePrice() {
        //Act
        InvestigationEntity returnValue = converter.fromDtoToEntity(inputDto, specialtyEntity);

        //Assert
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputDto.getName());
        Assertions.assertThat(returnValue.getSpecialty()).isEqualTo(specialtyEntity);
        Assertions.assertThat(returnValue.getDuration()).isEqualTo(inputDto.getDuration());
        Assertions.assertThat(returnValue.getBasePrice()).isEqualTo(inputDto.getBasePrice());
    }

    @Test
    public void testFromDtoToEntity_whenBasePriceNull_thenReturnValueHasDefaultBasePrice() {
        //Arrange
        inputDto.setBasePrice(null);

        //Act
        InvestigationEntity returnValue = converter.fromDtoToEntity(inputDto, specialtyEntity);

        //Assert
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputDto.getName());
        Assertions.assertThat(returnValue.getSpecialty()).isEqualTo(specialtyEntity);
        Assertions.assertThat(returnValue.getDuration()).isEqualTo(inputDto.getDuration());
        Assertions.assertThat(returnValue.getBasePrice()).isEqualTo(DEFAULT_BASE_PRICE);
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        InvestigationDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputEntity.getName());
        Assertions.assertThat(returnValue.getSpecialty()).isEqualTo(specialtyEntity.getName());
        Assertions.assertThat(returnValue.getDuration()).isEqualTo(inputEntity.getDuration());
        Assertions.assertThat(returnValue.getBasePrice()).isEqualTo(inputEntity.getBasePrice());
    }

}
