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

    public static final String BASE_PATH = "/investigations";
    public static final Long ID = 1L;
    public static final Long NON_EXISTENT_ID = 999L;
    public static final String INVESTIGATION = "TestInvestigation";
    public static final String FAKE_INVESTIGATION = "FakeInvestigation";
    public static final String SPECIALTY = "TestSpecialty";
    public static final String FAKE_SPECIALTY = "FakeSpecialty";
    public static final Integer DURATION = 30;
    public static final Integer NON_EXISTENT_DURATION = 1000;
    public static final String DOCTOR = "TestDoctor";
    public static final Double PRICE = 250D;
    public static final String FAKE_DOCTOR = "FakeDoctor";

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
        when(investigationService.upsertInvestigation(investigationDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertInvestigation_whenIdNotNotNullAndInvestigationExists_thenReturnOk() throws Exception {
        investigationDto.setId(ID);
        when(investigationService.upsertInvestigation(investigationDto))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testUpsertInvestigation_whenIdNotNotNullAndInvestigationNotExist_thenReturnBadRequest() throws Exception {
        investigationDto.setId(NON_EXISTENT_ID);
        when(investigationService.upsertInvestigation(investigationDto))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(post(BASE_PATH + "/insert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(investigationDto)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationById_whenInvestigationExists_thenReturnOk() throws Exception {
        investigationDto.setId(ID);
        when(investigationService.getInvestigationById(ID))
                .thenReturn(investigationDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + ID));

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
        when(investigationService.getInvestigationById(NON_EXISTENT_ID))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + NON_EXISTENT_ID));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationByName_whenInvestigationExists_thenReturnOk() throws Exception {
        when(investigationService.getInvestigationByName(INVESTIGATION))
                .thenReturn(investigationDto);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/get")
                .param("name", INVESTIGATION));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.name", CoreMatchers.is(INVESTIGATION)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.specialty", CoreMatchers.is(investigationDto.getSpecialty())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.basePrice", CoreMatchers.is(investigationDto.getBasePrice())))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.duration", CoreMatchers.is(investigationDto.getDuration())));
    }

    @Test
    public void testGetInvestigationByName_whenInvestigationNotExist_thenReturnBadRequest() throws Exception {
        when(investigationService.getInvestigationByName(FAKE_INVESTIGATION))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/get")
                .param("name", FAKE_INVESTIGATION));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationBySpecialty_whenSpecialtyExists_thenReturnOk() throws Exception {
        when(investigationService.getInvestigationBySpecialty(SPECIALTY))
                .thenReturn(List.of(investigationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-specialty")
                .param("specialty", SPECIALTY));

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
        when(investigationService.getInvestigationBySpecialty(FAKE_SPECIALTY))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-specialty")
                .param("specialty", FAKE_SPECIALTY));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationByDuration_whenDurationExists_thenReturnOk() throws Exception {
        when(investigationService.getInvestigationByDuration(DURATION))
                .thenReturn(List.of(investigationDto));

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-duration")
                .param("duration", String.valueOf(DURATION)));

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
        when(investigationService.getInvestigationByDuration(NON_EXISTENT_DURATION))
                .thenReturn(Collections.emptyList());

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/by-duration")
                .param("duration", String.valueOf(NON_EXISTENT_DURATION)));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$.size()", CoreMatchers.is(0)));
    }

    @Test
    public void testDeleteInvestigationById_whenIdExists_thenReturnOk() throws Exception {
        when(investigationService.deleteInvestigationById(ID))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" + ID));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testDeleteInvestigationById_whenIdNotLong_thenReturnBadRequest() throws Exception {
        String id = "test";

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/" + id));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteInvestigationByName_whenNameExists_thenReturnOk() throws Exception {
        when(investigationService.deleteInvestigationByName(INVESTIGATION))
                .thenReturn(ID);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-name")
                .param("investigation", INVESTIGATION));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(ID)));
    }

    @Test
    public void testDeleteInvestigationByName_whenNameNotExist_thenReturnBadRequest() throws Exception {
        when(investigationService.deleteInvestigationByName(FAKE_INVESTIGATION))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(delete(BASE_PATH + "/by-name")
                .param("investigation", FAKE_INVESTIGATION));

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

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/all"));

        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.isA(List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(2)));
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNotNullAndExist_thenReturnOk() throws Exception {
        TypeReference<Map<String, Map<String, Double>>> typeReference =
                new TypeReference<>() {
                };

        when(investigationService.getInvestigationWithPricing(DOCTOR, INVESTIGATION))
                .thenReturn(Map.of(DOCTOR, Map.of(INVESTIGATION, PRICE)));

        MvcResult mvcResult = mockMvc.perform(get(BASE_PATH + "/pricing")
                        .param("doctor", DOCTOR)
                        .param("investigation", INVESTIGATION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Map<String, Double>> result =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);

        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(DOCTOR).isEmpty()).isFalse();
        Assertions.assertThat(result.get(DOCTOR).size()).isEqualTo(1);
        Assertions.assertThat(result.get(DOCTOR).containsKey(INVESTIGATION)).isTrue();
        Assertions.assertThat(result.get(DOCTOR).get(INVESTIGATION)).isEqualTo(PRICE);
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNull_thenReturnOk() throws Exception {
        TypeReference<Map<String, Map<String, Double>>> typeReference =
                new TypeReference<>() {
                };

        String investigation1 = "TestInvestigation1";
        String investigation2 = "TestInvestigation2";

        when(investigationService.getInvestigationWithPricing(DOCTOR, null))
                .thenReturn(Map.of(DOCTOR, Map.of(investigation1, PRICE, investigation2, PRICE)));

        MvcResult mvcResult = mockMvc.perform(get(BASE_PATH + "/pricing")
                        .param("doctor", DOCTOR))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Map<String, Double>> result =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);

        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(DOCTOR).isEmpty()).isFalse();
        Assertions.assertThat(result.get(DOCTOR).size()).isEqualTo(2);
        Assertions.assertThat(result.get(DOCTOR).containsKey(investigation1)).isTrue();
        Assertions.assertThat(result.get(DOCTOR).containsKey(investigation2)).isTrue();
        Assertions.assertThat(result.get(DOCTOR).get(investigation1)).isEqualTo(PRICE);
        Assertions.assertThat(result.get(DOCTOR).get(investigation2)).isEqualTo(PRICE);
    }

    @Test
    public void testGetInvestigationWithPricing_whenDoctorNotExist_thenReturnBadRequest() throws Exception {
        when(investigationService.getInvestigationWithPricing(FAKE_DOCTOR, INVESTIGATION))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/pricing")
                        .param("doctor", FAKE_DOCTOR)
                        .param("investigation", INVESTIGATION));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvestigationWithPricing_whenInvestigationNotNullAndNotExist_thenReturnBadRequest()
            throws Exception {

        when(investigationService.getInvestigationWithPricing(DOCTOR, INVESTIGATION))
                .thenThrow(DataNotFoundException.class);

        ResultActions result = mockMvc.perform(get(BASE_PATH + "/pricing")
                .param("doctor", DOCTOR)
                .param("investigation", INVESTIGATION));

        result.andExpect(status().isBadRequest());
    }

}
