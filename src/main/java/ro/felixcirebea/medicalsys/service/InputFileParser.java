package ro.felixcirebea.medicalsys.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InputFileParser {

    private final SpecialtyRepository specialtyRepository;
    private final InvestigationRepository investigationRepository;
    private final DoctorRepository doctorRepository;

    @Value("classpath:/input-files/specialties.csv")
    private Resource specialtyResource;

    @Value("classpath:/input-files/investigations.csv")
    private Resource investigationResource;

    @Value("classpath:/input-files/doctors.csv")
    private Resource doctorResource;

    public InputFileParser(SpecialtyRepository specialtyRepository, InvestigationRepository investigationRepository, DoctorRepository doctorRepository) {
        this.specialtyRepository = specialtyRepository;
        this.investigationRepository = investigationRepository;
        this.doctorRepository = doctorRepository;
    }

    public void run() {
        populateTable(getPath(specialtyResource), SpecialtyEntity.class);
        populateTable(getPath(investigationResource), InvestigationEntity.class);
        populateTable(getPath(doctorResource), DoctorEntity.class);
    }

    private <T> void populateTable(String filePath, Class<T> clazz) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                switch (clazz.getSimpleName()) {
                    case "SpecialtyEntity" -> specialtyRepository.saveAll(generateSpecialtyCollection(line));
                    case "InvestigationEntity" -> investigationRepository.save(generateInvestigationEntity(line));
                    case "DoctorEntity" -> doctorRepository.save(generateDoctorEntity(line));
                    default -> throw new RuntimeException("No suitable class found");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DoctorEntity generateDoctorEntity(String line) {
        String[] splitLine = line.split(",");

        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(splitLine[1])
                .orElseThrow(() -> new RuntimeException("Internal error"));

        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.setName(splitLine[0]);
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(Double.valueOf(splitLine[2]));

        return doctorEntity;
    }

    private InvestigationEntity generateInvestigationEntity(String line) {
        String[] splitLine = line.split(",");

        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(splitLine[1])
                .orElseThrow(() -> new RuntimeException("Internal error"));

        InvestigationEntity investigationEntity = new InvestigationEntity();
        investigationEntity.setName(splitLine[0]);
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setBasePrice(Double.valueOf(splitLine[2]));

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
            throw new RuntimeException("Wrong input file path");
        }
    }
}
