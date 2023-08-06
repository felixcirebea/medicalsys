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

    private final HolidayRepository holidayRepository;
    private final HolidayConverter holidayConverter;
    private final Contributor infoContributor;

    public HolidayService(HolidayRepository holidayRepository, HolidayConverter holidayConverter,
                          Contributor infoContributor) {
        this.holidayRepository = holidayRepository;
        this.holidayConverter = holidayConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertHoliday(HolidayDto holidayDto) throws DataNotFoundException {
        if (holidayDto.getId() != null) {
            return updateHoliday(holidayDto);
        }
        log.info(String.format("%s holiday was saved", holidayDto.getDescription()));
        return holidayRepository.save(holidayConverter.fromDtoToEntity(holidayDto)).getId();
    }

    private Long updateHoliday(HolidayDto holidayDto) throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findById(holidayDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));

        holidayEntity.setStartDate(holidayDto.getStartDate());
        holidayEntity.setEndDate(holidayDto.getEndDate());
        holidayEntity.setDescription(holidayDto.getDescription());
        log.info(String.format("Holiday with id %s was updated", holidayDto.getId()));

        return holidayRepository.save(holidayEntity).getId();
    }

    public HolidayDto getHolidayById(Long holidayId) throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public HolidayDto getHolidayByDescription(String holidayDescription) throws DataNotFoundException {
        HolidayEntity holidayEntity = holidayRepository.findByDescription(holidayDescription).orElseThrow(() ->
                new DataNotFoundException(String.format("Holiday with description %s not found", holidayDescription)));
        return holidayConverter.fromEntityToDto(holidayEntity);
    }

    public List<HolidayDto> getAllHolidays() {
        return StreamSupport.stream(holidayRepository.findAll().spliterator(), false)
                .map(holidayConverter::fromEntityToDto).toList();
    }

    public Boolean isDateHoliday(LocalDate inputDate) {
        return holidayRepository.isDateBetweenHolidays(inputDate);
    }

    public Long deleteHolidayById(Long holidayId) {
        Optional<HolidayEntity> holidayEntityOptional = holidayRepository.findById(holidayId);
        if (holidayEntityOptional.isEmpty()) {
            log.warn(String.format("Can't delete holiday with id %s because it doesn't exist", holidayId));
            infoContributor.incrementFailedDeleteOperations();
            return holidayId;
        }
        holidayRepository.deleteById(holidayId);
        log.info(String.format("Holiday with id %s deleted", holidayId));
        return holidayId;
    }

    public Long deleteHolidayByDescription(String description) throws DataNotFoundException {
        Optional<HolidayEntity> holidayEntityOptional = holidayRepository.findByDescription(description);
        if (holidayEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format("Can't delete holiday with description %s because it doesn't exist", description));
            throw new DataNotFoundException(String.format("Holiday with description %s not found", description));
        }
        holidayRepository.deleteById(holidayEntityOptional.get().getId());
        return holidayEntityOptional.get().getId();
    }
}
