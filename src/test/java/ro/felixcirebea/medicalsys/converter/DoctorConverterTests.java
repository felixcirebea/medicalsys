package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;

public class DoctorConverterTests {

    public static final Long ID = 1L;

    private DoctorConverter converter;

    private DoctorDto inputDto;

    private DoctorEntity inputEntity;

    private SpecialtyEntity specialtyEntity;

    @BeforeEach
    public void setUp() {
        converter = new DoctorConverter();

        inputDto = DoctorUtil.createDoctorDto();
        inputEntity = DoctorUtil.createDoctorEntity(ID);
        specialtyEntity = SpecialtyUtil.createSpecialtyEntity(ID);
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        DoctorEntity returnValue = converter.fromDtoToEntity(inputDto, specialtyEntity);

        //Assert
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputDto.getName());
        Assertions.assertThat(returnValue.getSpecialty()).isEqualTo(specialtyEntity);
        Assertions.assertThat(returnValue.getPriceRate()).isEqualTo(inputDto.getPriceRate());
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        DoctorDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getName()).isEqualTo(inputEntity.getName());
        Assertions.assertThat(returnValue.getSpecialty()).isEqualTo(specialtyEntity.getName());
        Assertions.assertThat(returnValue.getPriceRate()).isEqualTo(inputEntity.getPriceRate());

    }

}
