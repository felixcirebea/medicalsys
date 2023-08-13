package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.HolidayConverter;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.HolidayRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class HolidayService {

    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of holiday: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Holiday id: %s deleted";
    private final HolidayRepository holidayRepository;
    private final HolidayConverter holidayConverter;
    private final Contributor infoContributor;

    public HolidayService(HolidayRepository holidayRepository,
                          HolidayConverter holidayConverter,
                          Contributor infoContributor) {
        this.holidayRepository = holidayRepository;
        this.holidayConverter = holidayConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertHoliday(HolidayDto holidayDto) throws DataNotFoundException {
        if (holidayDto.getId() != null) {
            return updateHoliday(holidayDto);
        }

        log.info(String.format(LOG_INSERT_MSG, holidayDto.getDescription()));
        return holidayRepository.save(holidayConverter.fromDtoToEntity(holidayDto)).getId();
    }

    private Long updateHoliday(HolidayDto holidayDto) throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findById(holidayDto.getId())
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        holidayEntity.setStartDate(holidayDto.getStartDate());
        holidayEntity.setEndDate(holidayDto.getEndDate());
        holidayEntity.setDescription(holidayDto.getDescription());
        log.info(String.format(LOG_UPDATE_MSG, holidayDto.getDescription(), holidayDto));
        return holidayRepository.save(holidayEntity).getId();
    }

    public HolidayDto getHolidayById(Long holidayId) throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public HolidayDto getHolidayByDescription(String holidayDescription)
            throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findByDescription(holidayDescription)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, holidayDescription)));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public List<HolidayDto> getAllHolidays() {
        return StreamSupport.stream(holidayRepository.findAll().spliterator(), false)
                .map(holidayConverter::fromEntityToDto)
                .toList();
    }

    public Boolean isDateHoliday(LocalDate inputDate) {
        return holidayRepository.isDateBetweenHolidays(inputDate);
    }

    public Long deleteHolidayById(Long holidayId) {
        boolean deleteCondition = holidayRepository.existsById(holidayId);
        if (!deleteCondition) {
            log.warn(String.format(LOG_FAIL_DELETE_MSG, holidayId));
            infoContributor.incrementFailedDeleteOperations();
            return holidayId;
        }

        holidayRepository.deleteById(holidayId);
        log.info(String.format(LOG_SUCCESS_DELETE_MSG, holidayId));
        return holidayId;
    }

    public Long deleteHolidayByDescription(String description) throws DataNotFoundException {
        Optional<HolidayEntity> holidayEntityOptional = holidayRepository.findByDescription(description);

        if (holidayEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_DELETE_MSG, description));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, description));
        }

        Long holidayId = holidayEntityOptional.get().getId();
        holidayRepository.deleteById(holidayId);
        return holidayId;
    }
}
