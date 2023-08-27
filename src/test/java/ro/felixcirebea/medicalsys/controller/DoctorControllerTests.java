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
        Long expectedId = 1L;
        when(doctorService.upsertDoctor(doctorDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/doctors/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertDoctor_whenIdNotNullDoctorExists_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        doctorDto.setId(expectedId);
        when(doctorService.upsertDoctor(doctorDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/doctors/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertDoctor_whenIdNotNullDoctorNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        doctorDto.setId(nonExistentId);
        when(doctorService.upsertDoctor(doctorDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/doctors/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorById_whenDoctorExists_thenReturnOk() throws Exception {
        Long id = 1L;
        when(doctorService.getDoctorById(id)).thenReturn(doctorDto);

        ResultActions result = mockMvc.perform(get("/doctors/" + id));

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
        Long nonExistentId = 999L;
        when(doctorService.getDoctorById(nonExistentId))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/doctors/" + nonExistentId));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get("/doctors/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorByName_whenDoctorExists_thenReturnOk() throws Exception {
        String name = "TestDoctor";
        when(doctorService.getDoctorByName(name))
                .thenReturn(doctorDto);

        ResultActions result = mockMvc.perform(get("/doctors/by-name")
                .param("name", name));

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
        String name = "FakeDoctor";
        when(doctorService.getDoctorByName(name))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/doctors/by-name")
                .param("name", name));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDoctorsBySpecialty_whenSpecialtyExists_thenReturnOk() throws Exception {
        String specialty = "TestSpecialty";
        when(doctorService.getDoctorsBySpecialty(specialty))
                .thenReturn(List.of(doctorDto));

        ResultActions result = mockMvc.perform(get("/doctors/by-specialty")
                .param("specialty", specialty));

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
        String specialty = "TesFakeSpecialty";
        when(doctorService.getDoctorsBySpecialty(specialty))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/doctors/by-specialty")
                .param("specialty", specialty));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllDoctors_whenDoctorsExist_thenReturnOk() throws Exception {
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctorDto));

        ResultActions result = mockMvc.perform(get("/doctors/all"));

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
        Long id = 1L;
        when(doctorService.deleteDoctorById(id)).thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/doctors/"+id));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteDoctorById_whenDoctorNotExist_thenReturnOk() throws Exception {
        Long nonExistentId = 999L;
        when(doctorService.deleteDoctorById(nonExistentId)).thenReturn(nonExistentId);

        ResultActions result = mockMvc.perform(delete("/doctors/"+nonExistentId));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(nonExistentId)));
    }

    @Test
    public void testDeleteDoctorByName_whenDoctorExists_thenReturnOk() throws Exception {
        Long id = 1L;
        String name = "TestDoctor";
        when(doctorService.deleteDoctorByName(name)).thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/doctors/by-name")
                .param("name", name));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteDoctorByName_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String name = "FakeDoctor";
        when(doctorService.deleteDoctorByName(name))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete("/doctors/by-name")
                .param("name", name));

        result.andExpect(status().isBadRequest());
    }

}
