package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;
import ro.felixcirebea.medicalsys.util.HolidayUtil;

public class HolidayConverterTests {

    private HolidayConverter converter;

    private HolidayDto inputDto;

    private HolidayEntity inputEntity;

    @BeforeEach
    public void setUp() {
        Long id = 1L;

        converter = new HolidayConverter();

        inputDto = HolidayUtil.createHolidayDto();
        inputEntity = HolidayUtil.createHolidayEntity(id);
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        HolidayEntity returnValue = converter.fromDtoToEntity(inputDto);

        //Assert
        Assertions.assertThat(returnValue.getStartDate()).isEqualTo(inputDto.getStartDate());
        Assertions.assertThat(returnValue.getEndDate()).isEqualTo(inputDto.getEndDate());
        Assertions.assertThat(returnValue.getDescription()).isEqualTo(inputDto.getDescription());
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        HolidayDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getStartDate()).isEqualTo(inputEntity.getStartDate());
        Assertions.assertThat(returnValue.getEndDate()).isEqualTo(inputEntity.getEndDate());
        Assertions.assertThat(returnValue.getDescription()).isEqualTo(inputEntity.getDescription());
    }

}
