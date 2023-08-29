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

    public static final String BASE_PATH = "/holidays";
    public static final Long ID = 1L;
    public static final Long NON_EXISTENT_ID = 999L;
    public static final String HOLIDAY = "TestHoliday";
    public static final String FAKE_HOLIDAY = "FakeHoliday";
    public static final String DATE = "2023-08-15";
    public static final String WRONG_DATE = "2023/08/15";
    public static final String DESCRIPTION = "TestHoliday";
    public static final String FAKE_DESCRIPTION = "FakeDoctor";

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
        when(holidayService.upsertHoliday(holidayDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertHoliday_whenIdNotNullHolidayExists_thenReturnOk() throws Exception {
        holidayDto.setId(ID);
        when(holidayService.upsertHoliday(holidayDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertHoliday_whenIdNotNullHolidayNotExist_thenReturnBadRequest() throws Exception {
        holidayDto.setId(NON_EXISTENT_ID);
        when(holidayService.upsertHoliday(holidayDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holidayDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayById_whenHolidayExists_thenReturnOk() throws Exception {
        when(holidayService.getHolidayById(ID)).thenReturn(holidayDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + ID));

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
        when(holidayService.getHolidayById(NON_EXISTENT_ID))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetHolidayByDescription_whenHolidayExists_thenReturnOk() throws Exception {
        when(holidayService.getHolidayByDescription(HOLIDAY))
                .thenReturn(holidayDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-description")
                .param("description", HOLIDAY));

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
        when(holidayService.getHolidayByDescription(FAKE_HOLIDAY))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-description")
                .param("description", FAKE_HOLIDAY));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllHolidays_whenHolidaysExist_thenReturnOk() throws Exception {
        when(holidayService.getAllHolidays()).thenReturn(List.of(holidayDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/all"));

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
        when(holidayService.isDateHoliday(LocalDate.parse(DATE)))
                .thenReturn(true);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/is-holiday")
                .param("date", DATE));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(true)));
    }

    @Test
    public void testIsHoliday_whenDateNotValid_thenReturnOk() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_PATH + "/is-holiday")
                .param("date", WRONG_DATE));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteHolidayById_whenHolidayExists_thenReturnOk() throws Exception {
        when(holidayService.deleteHolidayById(ID)).thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" + ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testDeleteHolidayById_whenHolidayNotExist_thenReturnOk() throws Exception {
        when(holidayService.deleteHolidayById(NON_EXISTENT_ID)).thenReturn(NON_EXISTENT_ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(NON_EXISTENT_ID)));
    }

    @Test
    public void testHolidayByDescription_whenHolidayExists_thenReturnOk() throws Exception {
        when(holidayService.deleteHolidayByDescription(DESCRIPTION)).thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-description")
                .param("description", DESCRIPTION));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testHolidayByDescription_whenHolidayNotExist_thenReturnBadRequest() throws Exception {
        when(holidayService.deleteHolidayByDescription(FAKE_DESCRIPTION))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-description")
                .param("description", FAKE_DESCRIPTION));

        result.andExpect(status().isBadRequest());
    }
}
