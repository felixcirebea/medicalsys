package ro.felixcirebea.medicalsys.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.service.InvestigationService;
import ro.felixcirebea.medicalsys.util.InvestigationUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvestigationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class InvestigationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestigationService investigationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Contributor contributor;

    private InvestigationDto investigationDto;

    @BeforeEach
    public void setUp() {
        investigationDto = InvestigationUtil.createInvestigationDto();
    }

    @Test
    public void testUpsertInvestigation_whenInvestigationExists_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        when(investigationService.upsertInvestigation(investigationDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/investigations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertInvestigation_whenIdNotNotNullAndInvestigationExists_thenReturnOk() throws Exception {
        Long expectedId = 1L;
        investigationDto.setId(expectedId);
        when(investigationService.upsertInvestigation(investigationDto))
                .thenReturn(expectedId);

        ResultActions result = mockMvc.perform(post("/investigations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedId)));
    }

    @Test
    public void testUpsertInvestigation_whenIdNotNotNullAndInvestigationNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 999L;
        investigationDto.setId(nonExistentId);
        when(investigationService.upsertInvestigation(investigationDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post("/investigations/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationById_whenInvestigationExists_thenReturnOk() throws Exception {
        Long id = 1L;
        investigationDto.setId(id);
        when(investigationService.getInvestigationById(id))
                .thenReturn(investigationDto);

        ResultActions result = mockMvc.perform(get("/investigations/" + id));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.name", CoreMatchers.is(investigationDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.specialty", CoreMatchers.is(investigationDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.basePrice", CoreMatchers.is(investigationDto.getBasePrice())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.duration", CoreMatchers.is(investigationDto.getDuration())));
    }

    @Test
    public void testGetInvestigationById_whenInvestigationNotExist_thenReturnBadRequest() throws Exception {
        Long nonExistentId = 1L;
        when(investigationService.getInvestigationById(nonExistentId))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/investigations/" + nonExistentId));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get("/investigations/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationByName_whenInvestigationExists_thenReturnOk() throws Exception {
        String inputName = investigationDto.getName();
        when(investigationService.getInvestigationByName(inputName))
                .thenReturn(investigationDto);

        ResultActions result = mockMvc.perform(get("/investigations/get")
                .param("name", inputName));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.name", CoreMatchers.is(inputName)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.specialty", CoreMatchers.is(investigationDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.basePrice", CoreMatchers.is(investigationDto.getBasePrice())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.duration", CoreMatchers.is(investigationDto.getDuration())));
    }

    @Test
    public void testGetInvestigationByName_whenInvestigationNotExist_thenReturnBadRequest() throws Exception {
        String nonExistentName = "FakeInvestigation";
        when(investigationService.getInvestigationByName(nonExistentName))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/investigations/get")
                .param("name", nonExistentName));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyExists_thenReturnOk() throws Exception {
        String inputSpecialty = "TestSpecialty";
        when(investigationService.getInvestigationBySpecialty(inputSpecialty))
                .thenReturn(List.of(investigationDto));

        ResultActions result = mockMvc.perform(get("/investigations/by-specialty")
                .param("specialty", inputSpecialty));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].name", CoreMatchers.is(investigationDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].specialty", CoreMatchers.is(investigationDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].basePrice", CoreMatchers.is(investigationDto.getBasePrice())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].duration", CoreMatchers.is(investigationDto.getDuration())));
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyNotExist_thenReturnBadRequest() throws Exception {
        String nonExistentSpecialty = "FakeSpecialty";
        when(investigationService.getInvestigationBySpecialty(nonExistentSpecialty))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/investigations/by-specialty")
                .param("specialty", nonExistentSpecialty));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationByDuration_whenDurationExists_thenReturnOk() throws Exception {
        Integer inputDuration = 30;
        when(investigationService.getInvestigationByDuration(inputDuration))
                .thenReturn(List.of(investigationDto));

        ResultActions result = mockMvc.perform(get("/investigations/by-duration")
                .param("duration", String.valueOf(inputDuration)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].name", CoreMatchers.is(investigationDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].specialty", CoreMatchers.is(investigationDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].basePrice", CoreMatchers.is(investigationDto.getBasePrice())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].duration", CoreMatchers.is(investigationDto.getDuration())));
    }

    @Test
    public void testGetInvestigationByDuration_whenDurationNotExist_thenReturnOkt() throws Exception {
        Integer nonExistentDuration = 1000;
        when(investigationService.getInvestigationByDuration(nonExistentDuration))
                .thenReturn(Collections.emptyList());

        ResultActions result = mockMvc.perform(get("/investigations/by-duration")
                .param("duration", String.valueOf(nonExistentDuration)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(0)));
    }

    @Test
    public void testDeleteInvestigationById_whenIdExists_thenReturnOk() throws Exception {
        Long id = 1L;
        when(investigationService.deleteInvestigationById(id))
                .thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/investigations/" + id));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteInvestigationById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(delete("/investigations/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteInvestigationByName_whenNameExists_thenReturnOk() throws Exception {
        Long id = 1L;
        String name = "TestInvestigation";
        when(investigationService.deleteInvestigationByName(name))
                .thenReturn(id);

        ResultActions result = mockMvc.perform(delete("/investigations/by-name")
                .param("investigation", name));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(id)));
    }

    @Test
    public void testDeleteInvestigationByName_whenNameNotExist_thenReturnBadRequest() throws Exception {
        String nonExistentName = "FakeInvestigation";
        when(investigationService.deleteInvestigationByName(nonExistentName))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete("/investigations/by-name")
                .param("investigation", nonExistentName));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllInvestigations_whenInvestigationsExist_thenReturnOk() throws Exception {
        InvestigationDto investigationDto1 = InvestigationUtil.createInvestigationDto();
        investigationDto1.setId(1L);
        InvestigationDto investigationDto2 = InvestigationUtil.createInvestigationDto();
        investigationDto2.setId(2L);

        when(investigationService.getAllInvestigations())
                .thenReturn(List.of(investigationDto1, investigationDto2));

        ResultActions result = mockMvc.perform(get("/investigations/all"));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(2)));
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNotNullAndExist_thenReturnOk() throws Exception {
        TypeReference<Map<String, Map<String, Double>>> typeReference =
                new TypeReference<>() {
                };

        String doctor = "TestDoctor";
        String investigation = investigationDto.getName();
        Double price = 250D;

        when(investigationService.getInvestigationWithPricing(doctor, investigation))
                .thenReturn(Map.of(doctor, Map.of(investigation, price)));

        MvcResult mvcResult = mockMvc.perform(get("/investigations/pricing")
                        .param("doctor", doctor)
                        .param("investigation", investigation))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Map<String, Double>> result =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);

        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(doctor).isEmpty()).isFalse();
        Assertions.assertThat(result.get(doctor).size()).isEqualTo(1);
        Assertions.assertThat(result.get(doctor).containsKey(investigation)).isTrue();
        Assertions.assertThat(result.get(doctor).get(investigation)).isEqualTo(price);
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNull_thenReturnOk() throws Exception {
        TypeReference<Map<String, Map<String, Double>>> typeReference =
                new TypeReference<>() {
                };

        String doctor = "TestDoctor";
        String investigation1 = "TestInvestigation1";
        String investigation2 = "TestInvestigation2";
        Double price = 250D;

        when(investigationService.getInvestigationWithPricing(doctor, null))
                .thenReturn(Map.of(doctor, Map.of(investigation1, price, investigation2, price)));

        MvcResult mvcResult = mockMvc.perform(get("/investigations/pricing")
                        .param("doctor", doctor))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Map<String, Double>> result =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);

        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(doctor).isEmpty()).isFalse();
        Assertions.assertThat(result.get(doctor).size()).isEqualTo(2);
        Assertions.assertThat(result.get(doctor).containsKey(investigation1)).isTrue();
        Assertions.assertThat(result.get(doctor).containsKey(investigation2)).isTrue();
        Assertions.assertThat(result.get(doctor).get(investigation1)).isEqualTo(price);
        Assertions.assertThat(result.get(doctor).get(investigation2)).isEqualTo(price);
    }

    @Test
    public void testGetInvestigationWithPricing_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        String doctor = "FakeDoctor";
        String investigation = "TestInvestigation";

        when(investigationService.getInvestigationWithPricing(doctor, investigation))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/investigations/pricing")
                        .param("doctor", doctor)
                        .param("investigation", investigation));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNotNullAndNotExist_thenReturnBadRequest()
            throws Exception {
        String doctor = "TestDoctor";
        String investigation = "FakeInvestigation";

        when(investigationService.getInvestigationWithPricing(doctor, investigation))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get("/investigations/pricing")
                .param("doctor", doctor)
                .param("investigation", investigation));

        result.andExpect(status().isBadRequest());
    }

}
