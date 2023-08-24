package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.WorkingHoursUtil;

import java.time.DayOfWeek;

public class WorkingHoursConverterTests {

    private WorkingHoursConverter converter;

    private WorkingHoursDto inputDto;

    private WorkingHoursEntity inputEntity;

    private DoctorEntity doctorEntity;

    @BeforeEach()
    public void setUp() {
        Long id = 1L;
        Integer dayOfWeek = 1;

        converter = new WorkingHoursConverter();

        inputDto = WorkingHoursUtil.createWorkingHoursDto(dayOfWeek);
        inputEntity = WorkingHoursUtil.createWorkingHoursEntity(id, dayOfWeek);
        doctorEntity = DoctorUtil.createDoctorEntity(id);
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        WorkingHoursEntity returnValue = converter.fromDtoToEntity(inputDto, doctorEntity);

        //Assert
        Assertions.assertThat(returnValue.getDoctor()).isEqualTo(doctorEntity);
        Assertions.assertThat(returnValue.getDayOfWeek()).isEqualTo(DayOfWeek.of(inputDto.getDayOfWeek()));
        Assertions.assertThat(returnValue.getStartHour()).isEqualTo(inputDto.getStartHour());
        Assertions.assertThat(returnValue.getEndHour()).isEqualTo(inputDto.getEndHour());
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        WorkingHoursDto returnValue = converter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getDoctor()).isEqualTo(doctorEntity.getName());
        Assertions.assertThat(returnValue.getDayOfWeek()).isEqualTo(inputEntity.getDayOfWeek().getValue());
        Assertions.assertThat(returnValue.getStartHour()).isEqualTo(inputEntity.getStartHour());
        Assertions.assertThat(returnValue.getEndHour()).isEqualTo(inputEntity.getEndHour());
    }
}
