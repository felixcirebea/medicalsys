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
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.HolidayService;
import ro.felixcirebea.medicalsys.util.HolidayUtil;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HolidayController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class HolidayControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HolidayService holidayService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private HolidayDto holidayDto;

    @BeforeEach
    public void setUp() {
        holidayDto = HolidayUtil.createHolidayDto();
    }

    @Test
    public void testUpsertHoliday_whenHolidayNotExist_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        when(holidayService.upsertHoliday(holidayDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/holidays/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertHoliday_whenIdNotNullHolidayExists_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        holidayDto.setId(expectedId);
        when(holidayService.upsertHoliday(holidayDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/holidays/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertHoliday_whenIdNotNullHolidayNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        holidayDto.setId(nonExistentId);
        when(holidayService.upsertHoliday(holidayDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/holidays/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayById_whenHolidayExists_thenReturnOk() throws Exception {
        Long id = 1L;
        when(holidayService.getHolidayById(id)).thenReturn(holidayDto);

        ResultActions result = mockMvc.perform(get("/holidays/" + id));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.startDate", CoreMatchers.is(String.valueOf(holidayDto.getStartDate()))))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.endDate", CoreMatchers.is(String.valueOf(holidayDto.getEndDate()))))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.description", CoreMatchers.is(holidayDto.getDescription())));
    }

    @Test
    public void testGetHolidayById_whenHolidayNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        when(holidayService.getHolidayById(nonExistentId))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/holidays/" + nonExistentId));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get("/holidays/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayByDescription_whenHolidayExists_thenReturnOk() throws Exception {
        String description = "TestHoliday";
        when(holidayService.getHolidayByDescription(description))
                .thenReturn(holidayDto);

        ResultActions result = mockMvc.perform(get("/holidays/by-description")
                .param("description", description));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.startDate", CoreMatchers.is(String.valueOf(holidayDto.getStartDate()))))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.endDate", CoreMatchers.is(String.valueOf(holidayDto.getEndDate()))))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.description", CoreMatchers.is(holidayDto.getDescription())));
    }

    @Test
    public void testGetHolidayByDescription_whenHolidayNotExist_thenReturnBadRequest() throws Exception {
        String description = "FakeHoliday";
        when(holidayService.getHolidayByDescription(description))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/holidays/by-description")
                .param("description", description));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllHolidays_whenHolidaysExist_thenReturnOk() throws Exception {
        when(holidayService.getAllHolidays()).thenReturn(List.of(holidayDto));

        ResultActions result = mockMvc.perform(get("/holidays/all"));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].description", CoreMatchers.is(holidayDto.getDescription())));
    }

    @Test
    public void testIsHoliday_whenDateValid_thenReturnOk() throws Exception {
        String date = "2023-08-15";

        when(holidayService.isDateHoliday(LocalDate.parse(date)))
                .thenReturn(true);

        ResultActions result = mockMvc.perform(get("/holidays/is-holiday")
                .param("date", date));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
    }

    @Test
    public void testIsHoliday_whenDateNotValid_thenReturnOk() throws Exception {
        String date = "2023/08/15";

        ResultActions result = mockMvc.perform(get("/holidays/is-holiday")
                .param("date", date));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteHolidayById_whenHolidayExists_thenReturnOk() throws Exception {
        Long id = 1L;
        when(holidayService.deleteHolidayById(id)).thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/holidays/"+id));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteHolidayById_whenHolidayNotExist_thenReturnOk() throws Exception {
        Long nonExistentId = 999L;
        when(holidayService.deleteHolidayById(nonExistentId)).thenReturn(nonExistentId);

        ResultActions result = mockMvc.perform(delete("/holidays/"+nonExistentId));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(nonExistentId)));
    }

    @Test
    public void testHolidayByDescription_whenHolidayExists_thenReturnOk() throws Exception {
        Long id = 1L;
        String description = "TestHoliday";
        when(holidayService.deleteHolidayByDescription(description)).thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/holidays/by-description")
                .param("description", description));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testHolidayByDescription_whenHolidayNotExist_thenReturnBadRequest() throws Exception {
        String description = "FakeDoctor";
        when(holidayService.deleteHolidayByDescription(description))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete("/holidays/by-description")
                .param("description", description));

        result.andExpect(status().isBadRequest());
    }
}
