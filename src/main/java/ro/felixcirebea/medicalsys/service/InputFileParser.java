package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.entity.*;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.InputFileException;
import ro.felixcirebea.medicalsys.repository.*;
import ro.felixcirebea.medicalsys.util.Validator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class InputFileParser {

    private final SpecialtyRepository specialtyRepository;
    private final InvestigationRepository investigationRepository;
    private final DoctorRepository doctorRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final HolidayRepository holidayRepository;
    private final VacationRepository vacationRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("classpath:/input-files/specialties.csv")
    private Resource specialtyResource;

    @Value("classpath:/input-files/investigations.csv")
    private Resource investigationResource;

    @Value("classpath:/input-files/doctors.csv")
    private Resource doctorResource;

    @Value("classpath:/input-files/working-hours.csv")
    private Resource workingHoursResource;

    @Value("classpath:/input-files/holidays.csv")
    private Resource holidayResource;

    @Value("classpath:/input-files/vacations.csv")
    private Resource vacationResource;

    @Value("classpath:/input-files/appointments.csv")
    private Resource appointmentResource;

    public InputFileParser(SpecialtyRepository specialtyRepository, InvestigationRepository investigationRepository,
                           DoctorRepository doctorRepository, WorkingHoursRepository workingHoursRepository,
                           HolidayRepository holidayRepository, VacationRepository vacationRepository,
                           AppointmentRepository appointmentRepository) {
        this.specialtyRepository = specialtyRepository;
        this.investigationRepository = investigationRepository;
        this.doctorRepository = doctorRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.holidayRepository = holidayRepository;
        this.vacationRepository = vacationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void run() throws DataMismatchException {
        try {
            populateTable(getPath(specialtyResource), SpecialtyEntity.class);
            populateTable(getPath(investigationResource), InvestigationEntity.class);
            populateTable(getPath(doctorResource), DoctorEntity.class);
            populateTable(getPath(workingHoursResource), WorkingHoursEntity.class);
            populateTable(getPath(holidayResource), HolidayEntity.class);
            populateTable(getPath(vacationResource), VacationEntity.class);
            populateTable(getPath(appointmentResource), AppointmentEntity.class);
            log.info("DB successfully populated");
        } catch (InputFileException exception) {
            log.error(exception.getMessage());
            System.exit(1);
        }
    }

    private <T> void populateTable(String filePath, Class<T> clazz) throws DataMismatchException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                switch (clazz.getSimpleName()) {
                    case "SpecialtyEntity" -> specialtyRepository.saveAll(generateSpecialtyCollection(line));
                    case "InvestigationEntity" -> investigationRepository.save(generateInvestigationEntity(line));
                    case "DoctorEntity" -> doctorRepository.save(generateDoctorEntity(line));
                    case "WorkingHoursEntity" -> workingHoursRepository.save(generateWorkingHoursEntity(line));
                    case "HolidayEntity" -> holidayRepository.save(generateHolidayEntity(line));
                    case "VacationEntity" -> vacationRepository.save(generateVacationEntity(line));
                    case "AppointmentEntity" -> appointmentRepository.save(generateAppointmentEntity(line));
                    default -> throw new InputFileException("No suitable class found");
                }
            }
        } catch (IOException e) {
            throw new InputFileException(e.getMessage());
        }
    }

    private AppointmentEntity generateAppointmentEntity(String line) throws DataMismatchException {
        String[] splitLine = line.split(",");

        DoctorEntity doctorEntity = doctorRepository.findByName(splitLine[1])
                .orElseThrow(() -> new InputFileException(
                        String.format("Internal error - %s not present in DB", splitLine[1])));

        InvestigationEntity investigationEntity = investigationRepository.findByName(splitLine[2])
                .orElseThrow(() -> new InputFileException(
                        String.format("Internal error - %s not present in DB", splitLine[2])));

        LocalDate dateValue = Validator.dateValidator(splitLine[3]);
        Boolean isHoliday = holidayRepository.isDateBetweenHolidays(dateValue);
        if (isHoliday) {
            throw new InputFileException(String.format("Internal error - %s is holiday", splitLine[3]));
        }

        Boolean isVacation = vacationRepository.isDateBetweenVacation(doctorEntity, dateValue);
        if (isVacation) {
            throw new InputFileException(String.format(
                    "Internal error - for %s doctor %s is in vacation", splitLine[3], doctorEntity.getName()));
        }

        LocalTime startHour = Validator.timeValidator(splitLine[4]);
        LocalTime endHour = startHour.plusMinutes(investigationEntity.getDuration());
        Boolean isOverlapping = appointmentRepository.existsByDoctorDateAndTimeRange(
                doctorEntity, dateValue, startHour, endHour);
        if (isOverlapping) {
            throw new InputFileException(String.format(
                    "Internal error - appointment for %s, in %s at %s is overlapping",
                    doctorEntity.getName(), dateValue, startHour));
        }

        Double basePrice = investigationEntity.getBasePrice();
        Double doctorRate = doctorEntity.getPriceRate();
        Double price = ((doctorRate / 100) * basePrice) + basePrice;

        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setClientName(splitLine[0]);
        appointmentEntity.setDoctor(doctorEntity);
        appointmentEntity.setInvestigation(investigationEntity);
        appointmentEntity.setDate(dateValue);
        appointmentEntity.setStartTime(startHour);
        appointmentEntity.setEndTime(endHour);
        appointmentEntity.setPrice(price);
        appointmentEntity.setStatus(AppointmentStatus.NEW);

        return appointmentEntity;
    }

    private VacationEntity generateVacationEntity(String line) {
        String[] splitLine = line.split(",");

        DoctorEntity doctorEntity = doctorRepository.findByName(splitLine[0]).orElseThrow(() ->
                new InputFileException(String.format("Internal error - %s not present in DB", splitLine[0])));

        VacationEntity vacationEntity = new VacationEntity();
        vacationEntity.setDoctor(doctorEntity);
        vacationEntity.setStartDate(LocalDate.parse(splitLine[1]));
        vacationEntity.setEndDate(LocalDate.parse(splitLine[2]));
        if (vacationEntity.getStartDate().isAfter(vacationEntity.getEndDate())) {
            throw new InputFileException("Internal error - start date cannot be after end date");
        }
        vacationEntity.setType(VacationType.valueOf(splitLine[3]));

        return vacationEntity;
    }

    private HolidayEntity generateHolidayEntity(String line) {
        String[] splitLine = line.split(",");

        HolidayEntity holidayEntity = new HolidayEntity();
        holidayEntity.setStartDate(LocalDate.parse(splitLine[0]));
        holidayEntity.setEndDate(LocalDate.parse(splitLine[1]));
        if (holidayEntity.getStartDate().isAfter(holidayEntity.getEndDate())) {
            throw new InputFileException("Internal error - start date cannot be after end date");
        }
        holidayEntity.setDescription(splitLine[2]);

        return holidayEntity;
    }

    private WorkingHoursEntity generateWorkingHoursEntity(String line) {
        String[] splitLine = line.split(",");

        DoctorEntity doctorEntity = doctorRepository.findByName(splitLine[0]).orElseThrow(() ->
                new InputFileException(String.format("Internal error - %s not present in DB", splitLine[0])));

        WorkingHoursEntity workingHoursEntity = new WorkingHoursEntity();
        workingHoursEntity.setDoctor(doctorEntity);
        workingHoursEntity.setDayOfWeek(DayOfWeek.of(Integer.parseInt(splitLine[1])));
        workingHoursEntity.setStartHour(LocalTime.parse(splitLine[2].toUpperCase()));
        workingHoursEntity.setEndHour(LocalTime.parse(splitLine[3]));

        return workingHoursEntity;
    }

    private DoctorEntity generateDoctorEntity(String line) {
        String[] splitLine = line.split(",");

        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(splitLine[1]).orElseThrow(() ->
                new InputFileException(String.format("Internal error - %s not present in DB", splitLine[1])));

        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.setName(splitLine[0]);
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(Double.valueOf(splitLine[2]));

        return doctorEntity;
    }

    private InvestigationEntity generateInvestigationEntity(String line) {
        String[] splitLine = line.split(",");

        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(splitLine[1]).orElseThrow(() ->
                new InputFileException(String.format("Internal error - %s not present in DB", splitLine[1])));

        InvestigationEntity investigationEntity = new InvestigationEntity();
        investigationEntity.setName(splitLine[0]);
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setBasePrice(Double.valueOf(splitLine[2]));
        investigationEntity.setDuration(Integer.valueOf(splitLine[3]));

        return investigationEntity;
    }

    private List<SpecialtyEntity> generateSpecialtyCollection(String line) {
        String[] splitLine = line.split(",");
        List<SpecialtyEntity> returnList = new ArrayList<>();
        Arrays.stream(splitLine).forEach(string -> {
            SpecialtyEntity entity = new SpecialtyEntity();
            entity.setName(string);
            returnList.add(entity);
        });
        return returnList;
    }

    private String getPath(Resource resource) {
        try {
            return Paths.get(resource.getURI()).toString();
        } catch (IOException e) {
            throw new InputFileException(e.getMessage());
        }
    }
}
