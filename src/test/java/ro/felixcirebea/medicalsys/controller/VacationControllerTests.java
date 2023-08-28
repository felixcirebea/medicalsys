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
        Long expectedId = 1L;
        when(vacationService.insertVacation(vacationDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/vacations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testInsertVacation_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        vacationDto.setDoctor("FakeDoctor");
        when(vacationService.insertVacation(vacationDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/vacations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInsertVacation_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate currentDate = LocalDate.of(2023, 1, 5);
        vacationDto.setStartDate(currentDate.minusDays(3));
        when(vacationService.insertVacation(vacationDto))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post("/vacations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vacationDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDoctorAndVacationExist_thenReturnOk() throws Exception {
        Long id = 1L;
        String doctor = "TestDoctor";
        String startDate = "2023-01-01";

        when(vacationService.cancelVacation(doctor, startDate))
                .thenReturn(id);

        ResultActions result = mockMvc.perform(post("/vacations/update-status")
                .param("doctor", doctor)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testCancelVacation_whenVacationNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String startDate = "2023-01-15";

        when(vacationService.cancelVacation(doctor, startDate))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/vacations/update-status")
                .param("doctor", doctor)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDateNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String startDate = "2023/01/01";

        when(vacationService.cancelVacation(doctor, startDate))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(post("/vacations/update-status")
                .param("doctor", doctor)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testCancelVacation_whenDateBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate currentDate = LocalDate.of(2023, 5, 5);
        LocalDate startDateValue = currentDate.minusDays(4);
        String doctor = "TestDoctor";
        String startDate = String.valueOf(startDateValue);

        when(vacationService.cancelVacation(doctor, startDate))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post("/vacations/update-status")
                .param("doctor", doctor)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenDoctorStartAndEndDateNotNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String startDate = "2023-01-01";
        String endDate = "2023-02-01";

        when(vacationService.getVacationByDoctorAndDates(doctor, startDate, endDate))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor)
                .param("start-date", startDate)
                .param("end-date", endDate));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartDateNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String endDate = "2023-02-01";

        when(vacationService.getVacationByDoctorAndDates(doctor, null, endDate))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor)
                .param("end-date", endDate));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenEndDateNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String startDate = "2023-01-01";

        when(vacationService.getVacationByDoctorAndDates(doctor, startDate, null))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor)
                .param("start-date", startDate));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartAndEndDateNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";

        when(vacationService.getVacationByDoctorAndDates(doctor, null, null))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenDoctorNotExists_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String startDate = "2023-01-01";
        String endDate = "2023-02-01";

        when(vacationService.getVacationByDoctorAndDates(doctor, startDate, endDate))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor)
                .param("start-date", startDate)
                .param("end-date", endDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndDates_whenStartDateNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String startDate = "2023/01/01";
        String endDate = "2023-02-01";

        when(vacationService.getVacationByDoctorAndDates(doctor, startDate, endDate))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-dates")
                .param("doctor", doctor)
                .param("start-date", startDate)
                .param("end-date", endDate));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNotNullAndTypeValid_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String type = "VACATION";
        VacationType vacationType = VacationType.valueOf(type);

        when(vacationService.getVacationByDoctorAndType(doctor, vacationType))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-type")
                .param("doctor", doctor)
                .param("type", type));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNull_thenReturnOk() throws Exception {
        String type = "VACATION";
        VacationType vacationType = VacationType.valueOf(type);

        when(vacationService.getVacationByDoctorAndType(null, vacationType))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-type")
                .param("type", type));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndType_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String type = "VACATION";
        VacationType vacationType = VacationType.valueOf(type);

        when(vacationService.getVacationByDoctorAndType(doctor, vacationType))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-type")
                .param("doctor", doctor)
                .param("type", type));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndType_whenTypeNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String type = "vacation";

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-type")
                .param("doctor", doctor)
                .param("type", type));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testIsVacation_whenDoctorAndDateValid_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String date = "2023-01-01";
        LocalDate dateValue = LocalDate.parse(date);

        when(vacationService.isDateVacation(doctor, dateValue))
                .thenReturn(true);

        ResultActions result = mockMvc.perform(get("/vacations/is-vacation")
                .param("doctor", doctor)
                .param("date", date));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
    }

    @Test
    public void testIsVacation_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String date = "2023-01-01";
        LocalDate dateValue = LocalDate.parse(date);

        when(vacationService.isDateVacation(doctor, dateValue))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/vacations/is-vacation")
                .param("doctor", doctor)
                .param("date", date));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testIsVacation_whenDateNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String date = "2023/01/01";

        ResultActions result = mockMvc.perform(get("/vacations/is-vacation")
                .param("doctor", doctor)
                .param("date", date));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenDoctorExistsAndStatusValid_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        String status = "PLANNED";
        VacationStatus vacationStatus = VacationStatus.valueOf(status);

        when(vacationService.getVacationByStatus(doctor, vacationStatus))
                .thenReturn(List.of(vacationDto));

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-status")
                .param("doctor", doctor)
                .param("status", status));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String status = "PLANNED";
        VacationStatus vacationStatus = VacationStatus.valueOf(status);

        when(vacationService.getVacationByStatus(doctor, vacationStatus))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-status")
                .param("doctor", doctor)
                .param("status", status));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetVacationByDoctorAndStatus_whenStatusNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        String status = "planned";

        ResultActions result = mockMvc.perform(get("/vacations/by-doctor-and-status")
                .param("doctor", doctor)
                .param("status", status));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
