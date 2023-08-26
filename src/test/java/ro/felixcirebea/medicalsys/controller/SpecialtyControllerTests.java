package ro.felixcirebea.medicalsys.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.SpecialtyService;
import ro.felixcirebea.medicalsys.util.SpecialtyUtil;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpecialtyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class SpecialtyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpecialtyService specialtyService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private SpecialtyDto specialtyDto1;

    private SpecialtyDto specialtyDto2;

    @BeforeEach()
    public void setUp() {
        specialtyDto1 = SpecialtyUtil.createSpecialtyDto();
        specialtyDto2 = SpecialtyUtil.createSpecialtyDto();
    }

    @Test
    public void testUpsertSpecialty_whenSpecialtyNotPresent_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        when(specialtyService.upsertSpecialty(specialtyDto1)).thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/specialties/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialtyDto1)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUpsertSpecialty_whenIdNotNullAndSpecialtyExists_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        specialtyDto1.setId(expectedId);
        when(specialtyService.upsertSpecialty(specialtyDto1)).thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/specialties/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialtyDto1)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUpsertSpecialty_whenIdNotNullAndSpecialtyNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        specialtyDto1.setId(nonExistentId);
        when(specialtyService.upsertSpecialty(specialtyDto1)).thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/specialties/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialtyDto1)));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetSpecialtyById_whenIdExists_thenReturnOk() throws Exception {
        Long id = 1L;

        when(specialtyService.getSpecialtyById(id)).thenReturn(specialtyDto1.getName());

        ResultActions result = mockMvc.perform(get("/specialties/" + id)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(specialtyDto1.getName())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetSpecialtyById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get("/specialties/" + id)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void testGetSpecialtyById_whenIdNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;

        when(specialtyService.getSpecialtyById(nonExistentId))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/specialties/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetSpecialtyByName_whenSpecialtyExists_thenReturnOk() throws Exception {
        String inputName = "TestSpecialty";
        String expectedName = specialtyDto1.getName();

        when(specialtyService.getSpecialtyByName(inputName)).thenReturn(expectedName);

        ResultActions result = mockMvc.perform(get("/specialties/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expectedName)
                .param("name", inputName));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(expectedName)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetSpecialtyByName_whenSpecialtyNotExist_thenReturnBadRequest() throws Exception {
        String inputName = "FakeSpecialty";

        when(specialtyService.getSpecialtyByName(inputName))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/specialties/get")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", inputName));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetAllSpecialties_whenSpecialtiesExist_thenReturnOk() throws Exception {
        specialtyDto1.setId(1L);
        specialtyDto2.setId(2L);
        String expectedName = "TestSpecialty";

        when(specialtyService.getAllSpecialties()).thenReturn(List.of(specialtyDto1, specialtyDto2));

        ResultActions result = mockMvc.perform(get("/specialties/all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", CoreMatchers.is(expectedName)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name", CoreMatchers.is(expectedName)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetAllSpecialties_whenSpecialtiesNotExist_thenReturnOk() throws Exception {
        when(specialtyService.getAllSpecialties()).thenReturn(Collections.emptyList());

        ResultActions result = mockMvc.perform(get("/specialties/all")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(0)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDeleteById_whenIdExists_thenReturnOk() throws Exception {
        Long id = 1L;

        when(specialtyService.deleteSpecialtyById(id)).thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/specialties/" + id)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDeleteById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(delete("/specialties/" + id)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDeleteSpecialtyByName_whenNameExists_thenReturnOk() throws Exception {
        Long id = 1L;
        String inputName = "TestSpecialty";

        when(specialtyService.deleteSpecialtyByName(inputName))
                .thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/specialties/by-name")
                .param("name", inputName)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteSpecialtyByName_whenNameNotExist_thenReturnBadRequest() throws Exception {
        String inputName = "FakeSpecialty";

        when(specialtyService.deleteSpecialtyByName(inputName))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete("/specialties/by-name")
                .param("name", inputName)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Disabled
    //for more fine-grained assertions and also more complex scenario
    public void testGetAllSpecialties_withMapper_whenSpecialtiesExist_thenReturnOk() throws Exception {
        specialtyDto1.setId(1L);
        specialtyDto2.setId(2L);
        String expectedName = "TestSpecialty";

        when(specialtyService.getAllSpecialties()).thenReturn(List.of(specialtyDto1, specialtyDto2));

        MvcResult mvcResult = mockMvc.perform(get("/specialties/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        TypeReference<List<SpecialtyDto>> typeReference = new TypeReference<List<SpecialtyDto>>() {};

        List<SpecialtyDto> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);

        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get(0).getName()).isEqualTo(expectedName);
        Assertions.assertThat(result.get(1).getName()).isEqualTo(expectedName);
    }



}
