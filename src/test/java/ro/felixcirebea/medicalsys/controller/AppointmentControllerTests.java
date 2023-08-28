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
        String doctor = "TestDoctor";
        String investigation = "TestInvestigation";
        LocalDate desiredDate = LocalDate.of(2023, 1, 15);

        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(9, 0);
        LocalTime time3 = LocalTime.of(9, 30);
        LocalTime time4 = LocalTime.of(10, 0);

        List<LocalTime> resultList = List.of(time1, time2, time3, time4);

        when(appointmentService.getAvailableHours(doctor, investigation, desiredDate))
                .thenReturn(resultList);

        ResultActions result = mockMvc.perform(get("/appointments/available-hours")
                .param("doctor", doctor)
                .param("investigation", investigation)
                .param("date", String.valueOf(desiredDate)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(4)));
    }

    @Test
    public void testGetAvailableHours_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String investigation = "TestInvestigation";
        LocalDate desiredDate = LocalDate.of(2023, 1, 15);

        when(appointmentService.getAvailableHours(doctor, investigation, desiredDate))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/appointments/available-hours")
                .param("doctor", doctor)
                .param("investigation", investigation)
                .param("date", String.valueOf(desiredDate)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetAvailableHours_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate currentDate = LocalDate.of(2023, 1, 30);
        String doctor = "TestDoctor";
        String investigation = "TestInvestigation";
        LocalDate desiredDate = currentDate.minusDays(15);

        when(appointmentService.getAvailableHours(doctor, investigation, desiredDate))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(get("/appointments/available-hours")
                .param("doctor", doctor)
                .param("investigation", investigation)
                .param("date", String.valueOf(desiredDate)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testBookAppointment_whenDtoValid_thenReturnOk() throws Exception {
        Long id = 1L;
        when(appointmentService.bookAppointment(appointmentDto))
                .thenReturn(id);

        ResultActions result = mockMvc.perform(post("/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testBookAppointment_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        appointmentDto.setDoctor("FakeDoctor");
        when(appointmentService.bookAppointment(appointmentDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testBookAppointment_whenDateIsBeforeCurrentDate_thenReturnBadRequest() throws Exception {
        LocalDate currentDate = LocalDate.of(2023, 1, 30);
        appointmentDto.setDate(currentDate.minusDays(15));

        when(appointmentService.bookAppointment(appointmentDto))
                .thenThrow(ConcurrencyException.class);

        ResultActions result = mockMvc.perform(post("/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    public void testGetAppointmentById_whenIdExists_thenReturnOk() throws Exception {
        Long id = 1L;
        when(appointmentService.getAppointmentById(id))
                .thenReturn(appointmentDto);

        ResultActions result = mockMvc.perform(get("/appointments/" + id));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.clientName", CoreMatchers.is("TestClient")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date", CoreMatchers.is("2023-01-15")));
    }

    @Test
    public void testGetAppointmentById_whenIdNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        when(appointmentService.getAppointmentById(nonExistentId))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/appointments/" + nonExistentId));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetAppointmentById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get("/appointments/" + id));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testDeleteAppointmentByIdAndClientName_whenAppointmentExists_thenReturnOk() throws Exception {
        Long id = 1L;
        String clientName = "TestClient";
        when(appointmentService.cancelAppointmentByIdAndName(id, clientName))
                .thenReturn("Appointment successfully canceled");

        ResultActions result = mockMvc.perform(post("/appointments/cancel-book")
                .param("id", String.valueOf(id))
                .param("clientName", clientName));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Appointment successfully canceled"));
    }

    @Test
    public void testDeleteAppointmentByIdAndClientName_whenAppointmentNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        String clientName = "TestClient";
        when(appointmentService.cancelAppointmentByIdAndName(nonExistentId, clientName))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/appointments/cancel-book")
                .param("id", String.valueOf(nonExistentId))
                .param("clientName", clientName));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
