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
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.AppointmentService;
import ro.felixcirebea.medicalsys.util.AppointmentUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class AppointmentControllerTests {

    public static final String BASE_PATH = "/appointments";
    public static final String DOCTOR = "TestDoctor";
    public static final String INVESTIGATION = "TestInvestigation";
    public static final LocalDate DESIRED_DATE = LocalDate.of(2023, 1, 15);
    public static final String FAKE_DOCTOR = "FakeDoctor";
    public static final LocalDate CURRENT_DATE = LocalDate.of(2023, 1, 30);
    public static final Long ID = 1L;
    public static final Long NON_EXISTENT_ID = 999L;
    public static final String CLIENT_NAME = "TestClient";
    public static final String SUCCESS_CANCEL_APPOINTMENT_MSG = "Appointment successfully canceled";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private AppointmentDto appointmentDto;


    @BeforeEach
    public void setUp() {
        LocalDate date = LocalDate.of(2023, 1, 15);
        LocalTime time = LocalTime.of(8, 30);
        appointmentDto = AppointmentUtil.createAppointmentDto(date, time);
    }

    @Test
    public void testGetAvailableHours_whenDoctorInvestigationAndDateValid_thenReturnOk() throws Exception {
        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(9, 0);
        LocalTime time3 = LocalTime.of(9, 30);
        LocalTime time4 = LocalTime.of(10, 0);

        List<LocalTime> resultList = List.of(time1, time2, time3, time4);

        when(appointmentService.getAvailableHours(DOCTOR, INVESTIGATION, DESIRED_DATE))
                .thenReturn(resultList);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/available-hours")
                .param("doctor", DOCTOR)
                .param("investigation", INVESTIGATION)
                .param("date", String.valueOf(DESIRED_DATE)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(4)));
    }

    @Test
    public void testGetAvailableHours_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        when(appointmentService.getAvailableHours(FAKE_DOCTOR, INVESTIGATION, DESIRED_DATE))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/available-hours")
                .param("doctor", FAKE_DOCTOR)
                .param("investigation", INVESTIGATION)
                .param("date", String.valueOf(DESIRED_DATE)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetAvailableHours_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate desiredDate = CURRENT_DATE.minusDays(15);

        when(appointmentService.getAvailableHours(DOCTOR, INVESTIGATION, desiredDate))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/available-hours")
                .param("doctor", DOCTOR)
                .param("investigation", INVESTIGATION)
                .param("date", String.valueOf(desiredDate)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testBookAppointment_whenDtoValid_thenReturnOk() throws Exception {
        when(appointmentService.bookAppointment(appointmentDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testBookAppointment_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        appointmentDto.setDoctor(FAKE_DOCTOR);
        when(appointmentService.bookAppointment(appointmentDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testBookAppointment_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        appointmentDto.setDate(CURRENT_DATE.minusDays(15));

        when(appointmentService.bookAppointment(appointmentDto))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    public void testGetAppointmentById_whenIdExists_thenReturnOk() throws Exception {
        when(appointmentService.getAppointmentById(ID))
                .thenReturn(appointmentDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + ID));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.clientName", CoreMatchers.is(CLIENT_NAME)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.date", CoreMatchers.is(String.valueOf(DESIRED_DATE))));
    }

    @Test
    public void testGetAppointmentById_whenIdNotExist_thenReturnBadRequest() throws Exception {
        when(appointmentService.getAppointmentById(NON_EXISTENT_ID))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetAppointmentById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + id));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testDeleteAppointmentByIdAndClientName_whenAppointmentExists_thenReturnOk() throws Exception {
        when(appointmentService.cancelAppointmentByIdAndName(ID, CLIENT_NAME))
                .thenReturn(SUCCESS_CANCEL_APPOINTMENT_MSG);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/cancel-book")
                .param("id", String.valueOf(ID))
                .param("clientName", CLIENT_NAME));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(SUCCESS_CANCEL_APPOINTMENT_MSG));
    }

    @Test
    public void testDeleteAppointmentByIdAndClientName_whenAppointmentNotExist_thenReturnBadRequest() throws Exception {
        when(appointmentService.cancelAppointmentByIdAndName(NON_EXISTENT_ID, CLIENT_NAME))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/cancel-book")
                .param("id", String.valueOf(NON_EXISTENT_ID))
                .param("clientName", CLIENT_NAME));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
