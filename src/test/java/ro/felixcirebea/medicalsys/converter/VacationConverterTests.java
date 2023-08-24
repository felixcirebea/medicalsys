package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.VacationUtil;

public class VacationConverterTests {

    private VacationConverter converter;

    private VacationDto inputDto;

    private VacationEntity inputEntity;

    private DoctorEntity doctorEntity;

    @BeforeEach()
    public void setUp() {
        Long id = 1L;

        converter = new VacationConverter();

        inputDto = VacationUtil.createVacationDto();
        inputEntity = VacationUtil.createVacationEntity(id);
        doctorEntity = DoctorUtil.createDoctorEntity(id);
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        VacationEntity returnValue = converter.fromDtoToEntity(inputDto, doctorEntity);

        //Assert
        Assertions.assertThat(returnValue.getDoctor()).isEqualTo(doctorEntity);
        Assertions.assertThat(returnValue.getStartDate()).isEqualTo(inputDto.getStartDate());
        Assertions.assertThat(returnValue.getEndDate()).isEqualTo(inputDto.getEndDate());
        Assertions.assertThat(returnValue.getType()).isEqualTo(inputDto.getType());
        Assertions.assertThat(returnValue.getStatus()).isEqualTo(VacationStatus.PLANNED);
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        VacationDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getDoctor()).isEqualTo(doctorEntity.getName());
        Assertions.assertThat(returnValue.getStartDate()).isEqualTo(inputEntity.getStartDate());
        Assertions.assertThat(returnValue.getEndDate()).isEqualTo(inputEntity.getEndDate());
        Assertions.assertThat(returnValue.getType()).isEqualTo(inputEntity.getType());
    }
}
