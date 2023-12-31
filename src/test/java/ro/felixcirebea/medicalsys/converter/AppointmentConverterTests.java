package ro.felixcirebea.medicalsys.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.AppointmentEntity;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;
import ro.felixcirebea.medicalsys.util.AppointmentUtil;
import ro.felixcirebea.medicalsys.util.DoctorUtil;
import ro.felixcirebea.medicalsys.util.InvestigationUtil;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentConverterTests {

    public static final Long ID = 1L;
    public static final LocalDate DATE = LocalDate.of(2023, 1, 1);
    public static final LocalTime START_TIME = LocalTime.of(8, 0);
    public static final LocalTime END_TIME = LocalTime.of(8, 30);

    private AppointmentConverter appointmentConverter;

    private AppointmentEntity inputEntity;

    private AppointmentDto inputDto;

    private DoctorEntity doctorEntity;

    private InvestigationEntity investigationEntity;

    @BeforeEach
    public void setUp() {
        doctorEntity = DoctorUtil.createDoctorEntity(ID);
        investigationEntity = InvestigationUtil.createInvestigationEntity(ID);

        inputEntity =
                AppointmentUtil.createAppointmentEntity(ID, DATE, START_TIME, END_TIME);

        inputDto = AppointmentUtil.createAppointmentDto(DATE, START_TIME);

        appointmentConverter = new AppointmentConverter();
    }

    @Test
    public void testFromDtoToEntity() {
        //Act
        AppointmentEntity returnValue =
                appointmentConverter.fromDtoToEntity(inputDto, doctorEntity, investigationEntity);

        //Assert
        Assertions.assertThat(returnValue.getClientName()).isEqualTo(inputDto.getClientName());
        Assertions.assertThat(returnValue.getDoctor().getName()).isEqualTo(inputDto.getDoctor());
        Assertions.assertThat(returnValue.getInvestigation().getName()).isEqualTo(inputDto.getInvestigation());
        Assertions.assertThat(returnValue.getDate()).isEqualTo(inputDto.getDate());
        Assertions.assertThat(returnValue.getStartTime()).isEqualTo(inputDto.getStartHour());
        Assertions.assertThat(returnValue.getEndTime())
                .isEqualTo(inputDto.getStartHour().plusMinutes(investigationEntity.getDuration()));
        Assertions.assertThat(returnValue.getPrice())
                .isEqualTo((doctorEntity.getPriceRate() / 100 * investigationEntity.getBasePrice())
                        + investigationEntity.getBasePrice());
        Assertions.assertThat(returnValue.getStatus()).isEqualTo(AppointmentStatus.NEW);
    }

    @Test
    public void testFromEntityToDto() {
        //Act
        AppointmentDto returnValue = appointmentConverter.fromEntityToDto(inputEntity);

        //Assert
        Assertions.assertThat(returnValue.getId()).isEqualTo(inputEntity.getId());
        Assertions.assertThat(returnValue.getClientName()).isEqualTo(inputEntity.getClientName());
        Assertions.assertThat(returnValue.getDoctor()).isEqualTo(doctorEntity.getName());
        Assertions.assertThat(returnValue.getInvestigation()).isEqualTo(investigationEntity.getName());
        Assertions.assertThat(returnValue.getDate()).isEqualTo(inputEntity.getDate());
        Assertions.assertThat(returnValue.getStartHour()).isEqualTo(inputEntity.getStartTime());
        Assertions.assertThat(returnValue.getPrice()).isEqualTo(inputEntity.getPrice());
    }

}
