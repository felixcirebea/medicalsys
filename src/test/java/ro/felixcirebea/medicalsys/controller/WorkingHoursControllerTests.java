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
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.WorkingHoursService;
import ro.felixcirebea.medicalsys.util.WorkingHoursUtil;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = WorkingHoursController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class WorkingHoursControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkingHoursService workingHoursService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private WorkingHoursDto workingHoursDto;

    @BeforeEach
    public void setUp() {
        workingHoursDto = WorkingHoursUtil.createWorkingHoursDto(1);
    }

    @Test
    public void testUpsertWorkingHours_whenDtoValid_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        when(workingHoursService.upsertWorkingHours(workingHoursDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/working-hours/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workingHoursDto)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertWorkingHours_whenWorkingHoursNotExist_thenReturnBadRequest() throws Exception {
        when(workingHoursService.upsertWorkingHours(workingHoursDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/working-hours/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workingHoursDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testUpsertWorkingHours_whenDayOfWeekNotValid_thenReturnBadRequest() throws Exception {
        workingHoursDto.setDayOfWeek(15);
        when(workingHoursService.upsertWorkingHours(workingHoursDto))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(post("/working-hours/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workingHoursDto)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorAndDayNotNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";
        Integer dayOfWeek = 1;

        when(workingHoursService.getWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenReturn(List.of(workingHoursDto));

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDayNull_thenReturnOk() throws Exception {
        String doctor = "TestDoctor";

        when(workingHoursService.getWorkingHoursByDoctorAndDay(doctor, null))
                .thenReturn(List.of(workingHoursDto));

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day")
                .param("doctor", doctor));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorNull_thenReturnOk() throws Exception {
        Integer dayOfWeek = 1;

        when(workingHoursService.getWorkingHoursByDoctorAndDay(null, dayOfWeek))
                .thenReturn(List.of(workingHoursDto));

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day")
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorAndDayNull_thenReturnOk() throws Exception {
        when(workingHoursService.getWorkingHoursByDoctorAndDay(null, null))
                .thenReturn(List.of(workingHoursDto));

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day"));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        Integer dayOfWeek = 1;

        when(workingHoursService.getWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testGetWorkingHoursByDoctorAndDay_whenDayNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        Integer dayOfWeek = 15;

        when(workingHoursService.getWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(get("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDoctorAndDayNotNull_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        String doctor = "TestDoctor";
        Integer dayOfWeek = 1;

        when(workingHoursService.deleteWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(delete("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDayNull_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        String doctor = "TestDoctor";

        when(workingHoursService.deleteWorkingHoursByDoctorAndDay(doctor, null))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(delete("/working-hours/by-doctor-and-day")
                .param("doctor", doctor));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        Integer dayOfWeek = 1;

        when(workingHoursService.deleteWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testDeleteWorkingHoursByDoctorAndDay_whenDayNotValid_thenReturnBadRequest() throws Exception {
        String doctor = "TestDoctor";
        Integer dayOfWeek = 15;

        when(workingHoursService.deleteWorkingHoursByDoctorAndDay(doctor, dayOfWeek))
                .thenThrow(DataMismatchException.class);

        ResultActions result = mockMvc.perform(delete("/working-hours/by-doctor-and-day")
                .param("doctor", doctor)
                .param("day", String.valueOf(dayOfWeek)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
