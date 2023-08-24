package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.helper.DeleteUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class InvestigationService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of investigation: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Investigation: %s deleted";
    private final InvestigationRepository investigationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final InvestigationConverter investigationConverter;
    private final Contributor infoContributor;
    private final DoctorRepository doctorRepository;
    private final DeleteUtility deleteUtility;

    public InvestigationService(InvestigationRepository investigationRepository,
                                SpecialtyRepository specialtyRepository,
                                InvestigationConverter investigationConverter,
                                Contributor infoContributor,
                                DoctorRepository doctorRepository,
                                DeleteUtility deleteUtility) {
        this.investigationRepository = investigationRepository;
        this.specialtyRepository = specialtyRepository;
        this.investigationConverter = investigationConverter;
        this.infoContributor = infoContributor;
        this.doctorRepository = doctorRepository;
        this.deleteUtility = deleteUtility;
    }

    public Long upsertInvestigation(InvestigationDto investigationDto)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByNameAndIsActive(investigationDto.getSpecialty(), true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, investigationDto.getSpecialty())));
        if (investigationDto.getId() != null) {
            return updateInvestigation(investigationDto, specialtyEntity);
        }

        log.info(String.format(LOG_INSERT_MSG, investigationDto.getName()));
        return investigationRepository
                .save(investigationConverter
                        .fromDtoToEntity(investigationDto, specialtyEntity))
                .getId();
    }

    private Long updateInvestigation(InvestigationDto investigationDto,
                                     SpecialtyEntity specialtyEntity)
            throws DataNotFoundException {
        InvestigationEntity investigationEntity =
                investigationRepository.findByIdAndIsActive(investigationDto.getId(), true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        String nameToLog = investigationEntity.getName();

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setDuration(investigationDto.getDuration());
        investigationEntity.setBasePrice(investigationDto.getBasePrice());

        log.info(String.format(LOG_UPDATE_MSG, nameToLog, investigationDto));
        return investigationRepository.save(investigationEntity).getId();
    }

    public InvestigationDto getInvestigationById(Long investigationId)
            throws DataNotFoundException {
        InvestigationEntity investigationEntity =
                investigationRepository.findByIdAndIsActive(investigationId, true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public InvestigationDto getInvestigationByName(String investigationName)
            throws DataNotFoundException {
        InvestigationEntity investigationEntity =
                investigationRepository.findByNameAndIsActive(investigationName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, investigationName)));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public List<InvestigationDto> getInvestigationBySpecialty(String specialtyName)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByNameAndIsActive(specialtyName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getInvestigations().stream()
                .filter(inv -> inv.getIsActive().equals(true))
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public List<InvestigationDto> getInvestigationByDuration(Integer duration) {
        return investigationRepository.findAllByDurationAndIsActive(duration, true)
                .stream()
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public List<InvestigationDto> getAllInvestigations() {
        return investigationRepository.findAllByIsActive(true).stream()
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteInvestigationById(Long investigationId) {
        Optional<InvestigationEntity> investigationEntityOptional =
                investigationRepository.findByIdAndIsActive(investigationId, true);

        InvestigationEntity doctorEntity = deleteUtility.softDeleteById(
                investigationId, investigationEntityOptional,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, infoContributor);

        if (doctorEntity == null) {
            return investigationId;
        }
        return investigationRepository.save(doctorEntity).getId();
    }


    public Long deleteInvestigationByName(String investigationName)
            throws DataNotFoundException {
        Optional<InvestigationEntity> investigationEntityOptional =
                investigationRepository.findByNameAndIsActive(investigationName, true);

        InvestigationEntity investigationEntity = deleteUtility.softDeleteByField(
                investigationName, investigationEntityOptional, investigationRepository,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, NOT_FOUND_MSG, infoContributor);

        return investigationRepository.save(investigationEntity).getId();
    }

    public Map<String, Map<String, Double>> getInvestigationWithPricing(
            String doctorName, String investigationName)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, doctorName)));
        Double doctorPriceRate = doctorEntity.getPriceRate();

        Map<String, Map<String, Double>> returnCollection = new HashMap<>();
        Map<String, Double> innerCollection = new HashMap<>();

        if (investigationName == null || StringUtils.isBlank(investigationName)) {
            doctorEntity.getSpecialty().getInvestigations().stream()
                    .filter(inv -> inv.getIsActive().equals(true))
                    .forEach(inv -> {
                Double basePrice = inv.getBasePrice();
                Double price = basePrice + (doctorPriceRate / 100) * basePrice;
                innerCollection.put(inv.getName(), price);
            });

            returnCollection.put(doctorName, innerCollection);
            return returnCollection;
        }

        InvestigationEntity investigationEntity =
                investigationRepository.findByNameAndIsActive(investigationName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, investigationName)));
        Double basePrice = investigationEntity.getBasePrice();
        Double price = basePrice + ((doctorPriceRate / 100) * basePrice);

        innerCollection.put(investigationName, price);
        returnCollection.put(doctorName, innerCollection);
        return returnCollection;
    }


}
