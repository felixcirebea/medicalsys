package ro.felixcirebea.medicalsys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.VacationService;
import ro.felixcirebea.medicalsys.util.VacationUtil;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = VacationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class VacationControllerTests {

    public static final String BASE_PATH = "/vacations";
    public static final Long ID = 1L;
    public static final String FAKE_DOCTOR = "FakeDoctor";
    public static final LocalDate CURRENT_DATE = LocalDate.of(2023, 1, 5);
    public static final String DOCTOR = "TestDoctor";
    public static final String START_DATE = "2023-01-01";
    public static final String FAKE_START_DATE = "2023-01-15";
    public static final String INVALID_START_DATE = "2023/01/01";
    public static final String END_DATE = "2023-02-01";
    public static final String TYPE = "VACATION";
    public static final String INVALID_TYPE = "vacation";
    public static final String STATUS = "PLANNED";
    public static final String INVALID_STATUS = "planned";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VacationService vacationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private VacationDto vacationDto;

    @BeforeEach
    public void setUp() {
        vacationDto = VacationUtil.createVacationDto();
    }

    @Test
    public void testInsertVacation_whenDoctorExists_thenReturnOk() throws Exception {
        when(vacationService.insertVacation(vacationDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testInsertVacation_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        vacationDto.setDoctor(FAKE_DOCTOR);
        when(vacationService.insertVacation(vacationDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInsertVacation_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        vacationDto.setStartDate(CURRENT_DATE.minusDays(3));
        when(vacationService.insertVacation(vacationDto))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDoctorAndVacationExist_thenReturnOk() throws Exception {
        when(vacationService.cancelVacation(DOCTOR, START_DATE))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/update-status")
                .param("doctor", DOCTOR)
                .param("start-date", START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testCancelVacation_whenVacationNotExist_thenReturnBadRequest() throws Exception {
        when(vacationService.cancelVacation(DOCTOR, FAKE_START_DATE))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/update-status")
                .param("doctor", DOCTOR)
                .param("start-date", FAKE_START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDateNotValid_thenReturnBadRequest() throws Exception {
        when(vacationService.cancelVacation(DOCTOR, INVALID_START_DATE))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/update-status")
                .param("doctor", DOCTOR)
                .param("start-date", INVALID_START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDateBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate startDateValue = CURRENT_DATE.minusDays(4);
        String startDate = String.valueOf(startDateValue);

        when(vacationService.cancelVacation(DOCTOR, startDate))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/update-status")
                .param("doctor", DOCTOR)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenDoctorStartAndEndDateNotNull_thenReturnOk() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(DOCTOR, START_DATE, END_DATE))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", DOCTOR)
                .param("start-date", START_DATE)
                .param("end-date", END_DATE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartDateNull_thenReturnOk() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(DOCTOR, null, END_DATE))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", DOCTOR)
                .param("end-date", END_DATE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenEndDateNull_thenReturnOk() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(DOCTOR, START_DATE, null))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", DOCTOR)
                .param("start-date", START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartAndEndDateNull_thenReturnOk() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(DOCTOR, null, null))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", DOCTOR));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenDoctorNotExists_thenReturnBadRequest() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(FAKE_DOCTOR, START_DATE, END_DATE))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", FAKE_DOCTOR)
                .param("start-date", START_DATE)
                .param("end-date", END_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartDateNotValid_thenReturnBadRequest() throws Exception {
        when(vacationService.getVacationByDoctorAndDates(DOCTOR, INVALID_START_DATE, END_DATE))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-dates")
                .param("doctor", DOCTOR)
                .param("start-date", INVALID_START_DATE)
                .param("end-date", END_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNotNullAndTypeValid_thenReturnOk() throws Exception {
        VacationType vacationType = VacationType.valueOf(TYPE);

        when(vacationService.getVacationByDoctorAndType(DOCTOR, vacationType))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-type")
                .param("doctor", DOCTOR)
                .param("type", TYPE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNull_thenReturnOk() throws Exception {
        VacationType vacationType = VacationType.valueOf(TYPE);

        when(vacationService.getVacationByDoctorAndType(null, vacationType))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-type")
                .param("type", TYPE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        VacationType vacationType = VacationType.valueOf(TYPE);

        when(vacationService.getVacationByDoctorAndType(FAKE_DOCTOR, vacationType))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-type")
                .param("doctor", FAKE_DOCTOR)
                .param("type", TYPE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndType_whenTypeNotValid_thenReturnBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-type")
                .param("doctor", DOCTOR)
                .param("type", INVALID_TYPE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testIsVacation_whenDoctorAndDateValid_thenReturnOk() throws Exception {
        LocalDate dateValue = LocalDate.parse(START_DATE);

        when(vacationService.isDateVacation(DOCTOR, dateValue))
                .thenReturn(true);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/is-vacation")
                .param("doctor", DOCTOR)
                .param("date", START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
    }

    @Test
    public void testIsVacation_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        LocalDate dateValue = LocalDate.parse(START_DATE);

        when(vacationService.isDateVacation(FAKE_DOCTOR, dateValue))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/is-vacation")
                .param("doctor", FAKE_DOCTOR)
                .param("date", START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testIsVacation_whenDateNotValid_thenReturnBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_PATH + "/is-vacation")
                .param("doctor", DOCTOR)
                .param("date", INVALID_START_DATE));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenDoctorExistsAndStatusValid_thenReturnOk() throws Exception {
        VacationStatus vacationStatus = VacationStatus.valueOf(STATUS);

        when(vacationService.getVacationByStatus(DOCTOR, vacationStatus))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-status")
                .param("doctor", DOCTOR)
                .param("status", STATUS));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        VacationStatus vacationStatus = VacationStatus.valueOf(STATUS);

        when(vacationService.getVacationByStatus(FAKE_DOCTOR, vacationStatus))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-status")
                .param("doctor", FAKE_DOCTOR)
                .param("status", STATUS));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenStatusNotValid_thenReturnBadRequest() throws Exception {

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-doctor-and-status")
                .param("doctor", DOCTOR)
                .param("status", INVALID_STATUS));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
