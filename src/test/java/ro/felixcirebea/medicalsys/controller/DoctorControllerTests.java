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
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.DoctorService;
import ro.felixcirebea.medicalsys.util.DoctorUtil;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class DoctorControllerTests {

    public static final String BASE_PATH = "/doctors";
    public static final Long ID = 1L;
    public static final Long NON_EXISTENT_ID = 999L;
    public static final String DOCTOR = "TestDoctor";
    public static final String FAKE_DOCTOR = "FakeDoctor";
    public static final String SPECIALTY = "TestSpecialty";
    public static final String FAKE_SPECIALTY = "TesFakeSpecialty";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private DoctorDto doctorDto;

    @BeforeEach
    public void setUp() {
        doctorDto = DoctorUtil.createDoctorDto();
    }

    @Test
    public void testUpsertDoctor_whenDoctorNotExists_thenReturnOk() throws Exception {
        when(doctorService.upsertDoctor(doctorDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertDoctor_whenIdNotNullDoctorExists_thenReturnOk() throws Exception {
        doctorDto.setId(ID);
        when(doctorService.upsertDoctor(doctorDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertDoctor_whenIdNotNullDoctorNotExist_thenReturnBadRequest() throws Exception {
        doctorDto.setId(NON_EXISTENT_ID);
        when(doctorService.upsertDoctor(doctorDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorById_whenDoctorExists_thenReturnOk() throws Exception {
        when(doctorService.getDoctorById(ID)).thenReturn(doctorDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.name", CoreMatchers.is(doctorDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.specialty", CoreMatchers.is(doctorDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.priceRate", CoreMatchers.is(doctorDto.getPriceRate())));
    }

    @Test
    public void testGetDoctorById_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        when(doctorService.getDoctorById(NON_EXISTENT_ID))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorByName_whenDoctorExists_thenReturnOk() throws Exception {
        when(doctorService.getDoctorByName(DOCTOR))
                .thenReturn(doctorDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-name")
                .param("name", DOCTOR));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.name", CoreMatchers.is(doctorDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.specialty", CoreMatchers.is(doctorDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.priceRate", CoreMatchers.is(doctorDto.getPriceRate())));
    }

    @Test
    public void testGetDoctorByName_whenDoctorNoeExist_thenReturnBadRequest() throws Exception {
        when(doctorService.getDoctorByName(FAKE_DOCTOR))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-name")
                .param("name", FAKE_DOCTOR));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyExists_thenReturnOk() throws Exception {
        when(doctorService.getDoctorsBySpecialty(SPECIALTY))
                .thenReturn(List.of(doctorDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-specialty")
                .param("specialty", SPECIALTY));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].name", CoreMatchers.is(doctorDto.getName())));
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyNotExist_thenReturnBadRequest() throws Exception {
        when(doctorService.getDoctorsBySpecialty(FAKE_SPECIALTY))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-specialty")
                .param("specialty", FAKE_SPECIALTY));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllDoctors_whenDoctorsExist_thenReturnOk() throws Exception {
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctorDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/all"));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].name", CoreMatchers.is(doctorDto.getName())));
    }

    @Test
    public void testDeleteDoctorById_whenDoctorExists_thenReturnOk() throws Exception {
        when(doctorService.deleteDoctorById(ID)).thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" +ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testDeleteDoctorById_whenDoctorNotExist_thenReturnOk() throws Exception {
        when(doctorService.deleteDoctorById(NON_EXISTENT_ID)).thenReturn(NON_EXISTENT_ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(NON_EXISTENT_ID)));
    }

    @Test
    public void testDeleteDoctorByName_whenDoctorExists_thenReturnOk() throws Exception {
        when(doctorService.deleteDoctorByName(DOCTOR)).thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-name")
                .param("name", DOCTOR));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testDeleteDoctorByName_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        when(doctorService.deleteDoctorByName(FAKE_DOCTOR))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-name")
                .param("name", FAKE_DOCTOR));

        result.andExpect(status().isBadRequest());
    }

}
