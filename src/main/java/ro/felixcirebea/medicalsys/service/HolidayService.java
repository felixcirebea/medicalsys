package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.HolidayConverter;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.HolidayRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DeleteUtility;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class HolidayService {

    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of holiday: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Holiday: %s deleted";
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

    public Long upsertHoliday(HolidayDto holidayDto)
            throws DataNotFoundException {
        if (holidayDto.getId() != null) {
            return updateHoliday(holidayDto);
        }

        log.info(String.format(LOG_INSERT_MSG, holidayDto.getDescription()));
        return holidayRepository.save(
                holidayConverter.fromDtoToEntity(holidayDto)).getId();
    }

    private Long updateHoliday(HolidayDto holidayDto)
            throws DataNotFoundException {
        HolidayEntity holidayEntity =
                holidayRepository.findByIdAndIsActive(holidayDto.getId(), true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        holidayEntity.setStartDate(holidayDto.getStartDate());
        holidayEntity.setEndDate(holidayDto.getEndDate());
        holidayEntity.setDescription(holidayDto.getDescription());
        log.info(String.format(LOG_UPDATE_MSG, holidayDto.getDescription(), holidayDto));
        return holidayRepository.save(holidayEntity).getId();
    }

    public HolidayDto getHolidayById(Long holidayId)
            throws DataNotFoundException {
        HolidayEntity holidayEntity =
                holidayRepository.findByIdAndIsActive(holidayId, true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public HolidayDto getHolidayByDescription(String holidayDescription)
            throws DataNotFoundException {
        HolidayEntity holidayEntity =
                holidayRepository.findByDescriptionAndIsActive(holidayDescription, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, holidayDescription)));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public List<HolidayDto> getAllHolidays() {
        return holidayRepository.findAllByIsActive(true).stream()
                .map(holidayConverter::fromEntityToDto)
                .toList();
    }

    public Boolean isDateHoliday(LocalDate inputDate) {
        return holidayRepository.isDateBetweenHolidays(inputDate);
    }

    public Long deleteHolidayById(Long holidayId) {
        Optional<HolidayEntity> holidayEntityOptional =
                holidayRepository.findByIdAndIsActive(holidayId, true);

        HolidayEntity holidayEntity = DeleteUtility.softDeleteById(
                holidayId, holidayEntityOptional,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, infoContributor);

        if (holidayEntity == null) {
            return holidayId;
        }
        return holidayRepository.save(holidayEntity).getId();
    }

    public Long deleteHolidayByDescription(String description)
            throws DataNotFoundException {
        Optional<HolidayEntity> holidayEntityOptional =
                holidayRepository.findByDescriptionAndIsActive(description, true);

        HolidayEntity holidayEntity = DeleteUtility.softDeleteByField(
                description, holidayEntityOptional, holidayRepository,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, NOT_FOUND_MSG, infoContributor);

        return holidayRepository.save(holidayEntity).getId();
    }
}
