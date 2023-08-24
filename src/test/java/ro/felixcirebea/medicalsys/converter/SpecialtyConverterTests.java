package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;

public class SpecialtyConverterTests {

    private SpecialtyConverter converter;

    private SpecialtyDto inputDto;

    private SpecialtyEntity inputEntity;

    @BeforeEach
    public void setUp() {
        Long id = 1L;

        converter = new SpecialtyConverter();

        inputDto = SpecialtyUtil.createSpecialtyDto();
        inputEntity = SpecialtyUtil.createSpecialtyEntity(id);
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        SpecialtyEntity returnValue = converter.fromDtoToEntity(inputDto);

        //Assert
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputDto.getName());
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        SpecialtyDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputEntity.getName());
    }

}
