package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.AppointmentEntity;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;

@Component
public class AppointmentConverter {

    public AppointmentEntity fromDtoToEntity(AppointmentDto appointmentDto,
                                             DoctorEntity doctorEntity,
                                             InvestigationEntity investigationEntity) {
        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setClientName(appointmentDto.getClientName());
        appointmentEntity.setDoctor(doctorEntity);
        appointmentEntity.setInvestigation(investigationEntity);
        appointmentEntity.setDate(appointmentDto.getDate());
        appointmentEntity.setStartTime(appointmentDto.getStartHour());
        appointmentEntity.setEndTime(
                appointmentDto.getStartHour().plusMinutes(
                        investigationEntity.getDuration()));

        Double price = ((doctorEntity.getPriceRate() / 100) * investigationEntity.getBasePrice()) +
                investigationEntity.getBasePrice();
        appointmentEntity.setPrice(price);
        return appointmentEntity;
    }

    public AppointmentDto fromEntityToDto(AppointmentEntity appointmentEntity) {
        return AppointmentDto.builder()
                .id(appointmentEntity.getId())
                .clientName(appointmentEntity.getClientName())
                .doctor(appointmentEntity.getDoctor().getName())
                .investigation(appointmentEntity.getInvestigation().getName())
                .date(appointmentEntity.getDate())
                .startHour(appointmentEntity.getStartTime())
                .price(appointmentEntity.getPrice())
                .build();
    }
}
