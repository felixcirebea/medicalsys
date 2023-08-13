package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.converter.InvestigationConverter;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class InvestigationService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of investigation: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Investigation id: %s deleted";
    private final InvestigationRepository investigationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final InvestigationConverter investigationConverter;
    private final Contributor infoContributor;
    private final DoctorRepository doctorRepository;

    public InvestigationService(InvestigationRepository investigationRepository,
                                SpecialtyRepository specialtyRepository,
                                InvestigationConverter investigationConverter,
                                Contributor infoContributor,
                                DoctorRepository doctorRepository) {
        this.investigationRepository = investigationRepository;
        this.specialtyRepository = specialtyRepository;
        this.investigationConverter = investigationConverter;
        this.infoContributor = infoContributor;
        this.doctorRepository = doctorRepository;
    }

    public Long upsertInvestigation(InvestigationDto investigationDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(investigationDto.getSpecialty())
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, investigationDto.getSpecialty())));
        if (investigationDto.getId() != null) {
            return updateInvestigation(investigationDto, specialtyEntity);
        }

        log.info(String.format(LOG_INSERT_MSG, investigationDto.getName()));
        return investigationRepository.save(
                investigationConverter.fromDtoToEntity(investigationDto, specialtyEntity)).getId();
    }

    private Long updateInvestigation(InvestigationDto investigationDto, SpecialtyEntity specialtyEntity)
            throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findById(investigationDto.getId())
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        investigationEntity.setName(investigationDto.getName());
        investigationEntity.setSpecialty(specialtyEntity);
        investigationEntity.setDuration(investigationDto.getDuration());
        if (investigationDto.getBasePrice() != null) {
            investigationEntity.setBasePrice(investigationDto.getBasePrice());
        }

        log.info(String.format(LOG_UPDATE_MSG, investigationDto.getName(), investigationDto));
        return investigationRepository.save(investigationEntity).getId();
    }

    public InvestigationDto getInvestigationById(Long investigationId) throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public InvestigationDto getInvestigationByName(String investigationName) throws DataNotFoundException {
        InvestigationEntity investigationEntity = investigationRepository.findByName(investigationName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, investigationName)));
        return investigationConverter.fromEntityToDto(investigationEntity);
    }

    public List<InvestigationDto> getInvestigationBySpecialty(String specialtyName) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getInvestigations().stream()
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public List<InvestigationDto> getInvestigationByDuration(Integer duration) {
        return investigationRepository.findAllByDuration(duration).stream()
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public List<InvestigationDto> getAllInvestigations() {
        return StreamSupport.stream(investigationRepository.findAll().spliterator(), false)
                .map(investigationConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteInvestigationById(Long investigationId) {
        boolean deleteCondition = investigationRepository.existsById(investigationId);
        if (!deleteCondition) {
            log.warn(String.format(LOG_FAIL_DELETE_MSG, investigationId));
            infoContributor.incrementFailedDeleteOperations();
            return investigationId;
        }
        investigationRepository.deleteById(investigationId);
        log.info(String.format(LOG_SUCCESS_DELETE_MSG, investigationId));
        return investigationId;
    }


    public Long deleteInvestigationByName(String investigationName) throws DataNotFoundException {
        Optional<InvestigationEntity> investigationEntityOptional = investigationRepository
                .findByName(investigationName);

        if (investigationEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_DELETE_MSG, investigationName));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, investigationName));
        }

        Long doctorId = investigationEntityOptional.get().getId();
        investigationRepository.deleteById(doctorId);
        return doctorId;
    }

    public Map<String, Map<String, Double>> getInvestigationWithPricing(String doctorName, String investigationName)
            throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName)));
        Double doctorPriceRate = doctorEntity.getPriceRate();

        Map<String, Map<String, Double>> returnCollection = new HashMap<>();
        Map<String, Double> innerCollection = new HashMap<>();

        if (investigationName == null || StringUtils.isBlank(investigationName)) {
            doctorEntity.getSpecialty().getInvestigations().forEach(investigation -> {
                Double basePrice = investigation.getBasePrice();
                Double price = basePrice + (doctorPriceRate / 100) * basePrice;
                innerCollection.put(investigation.getName(), price);
            });
            returnCollection.put(doctorName, innerCollection);
            return returnCollection;
        }

        InvestigationEntity investigationEntity = investigationRepository.findByName(investigationName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, investigationName)));
        Double basePrice = investigationEntity.getBasePrice();
        Double price = basePrice + ((doctorPriceRate / 100) * basePrice);

        innerCollection.put(investigationName, price);
        returnCollection.put(doctorName, innerCollection);
        return returnCollection;
    }


}
