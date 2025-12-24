package com.se114p12.backend.controllers.variation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.variation.VariationOptionRequestDTO;
import com.se114p12.backend.dtos.variation.VariationOptionResponseDTO;
import com.se114p12.backend.services.variation.VariationOptionService;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VariationOptionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private VariationOptionService variationOptionService;

    @BeforeEach
    void setUp() {
        variationOptionService = Mockito.mock(VariationOptionService.class);
        VariationOptionController controller = new VariationOptionController(variationOptionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/variation-options";
    }

    @Test
    @DisplayName("GET /variation-options with variationId and pageable returns 200 and page content")
    void getByVariationId_returns200_withPageable() throws Exception {
        VariationOptionResponseDTO o1 = new VariationOptionResponseDTO();
        o1.setId(1L);
        o1.setVariationId(10L);
        o1.setValue("Red");
        o1.setAdditionalPrice(1.5);

        VariationOptionResponseDTO o2 = new VariationOptionResponseDTO();
        o2.setId(2L);
        o2.setVariationId(10L);
        o2.setValue("Blue");
        o2.setAdditionalPrice(0.0);

        PageVO<VariationOptionResponseDTO> page = PageVO.<VariationOptionResponseDTO>builder()
                .page(0)
                .size(2)
                .totalElements(2L)
                .totalPages(1)
                .numberOfElements(2)
                .content(List.of(o1, o2))
                .build();

        when(variationOptionService.getByVariationId(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(basePath())
                        .param("variationId", "10")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].variationId", is(10)))
                .andExpect(jsonPath("$.content[0].value", is("Red")))
                .andExpect(jsonPath("$.content[0].additionalPrice", is(1.5)))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].variationId", is(10)))
                .andExpect(jsonPath("$.content[1].value", is("Blue")))
                .andExpect(jsonPath("$.content[1].additionalPrice", is(0.0)));

        verify(variationOptionService).getByVariationId(eq(10L), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /variation-options with variationId only (unpaged) returns 200 and empty content")
    void getByVariationId_returns200_unpaged() throws Exception {
        PageVO<VariationOptionResponseDTO> page = PageVO.<VariationOptionResponseDTO>builder()
                .page(0)
                .size(0)
                .totalElements(0L)
                .totalPages(0)
                .numberOfElements(0)
                .content(List.of())
                .build();

        when(variationOptionService.getByVariationId(eq(11L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(basePath())
                        .param("variationId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(variationOptionService).getByVariationId(eq(11L), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /variation-options returns 200 with created option")
    void create_returns200() throws Exception {
        VariationOptionRequestDTO req = new VariationOptionRequestDTO();
        req.setValue("XL");
        req.setAdditionalPrice(2.0);
        req.setVariationId(20L);

        VariationOptionResponseDTO res = new VariationOptionResponseDTO();
        res.setId(100L);
        res.setValue("XL");
        res.setAdditionalPrice(2.0);
        res.setVariationId(20L);

        when(variationOptionService.create(any(VariationOptionRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post(basePath())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.value", is("XL")))
                .andExpect(jsonPath("$.additionalPrice", is(2.0)))
                .andExpect(jsonPath("$.variationId", is(20)));

        ArgumentCaptor<VariationOptionRequestDTO> captor = ArgumentCaptor.forClass(VariationOptionRequestDTO.class);
        verify(variationOptionService).create(captor.capture());
        VariationOptionRequestDTO captured = captor.getValue();
        assert captured.getValue().equals("XL");
        assert captured.getAdditionalPrice().equals(2.0);
        assert captured.getVariationId().equals(20L);
    }

    @Test
    @DisplayName("PUT /variation-options/{id} returns 200 with updated option")
    void update_returns200() throws Exception {
        VariationOptionRequestDTO req = new VariationOptionRequestDTO();
        req.setValue("XXL");
        req.setAdditionalPrice(3.5);
        req.setVariationId(21L);

        VariationOptionResponseDTO res = new VariationOptionResponseDTO();
        res.setId(101L);
        res.setValue("XXL");
        res.setAdditionalPrice(3.5);
        res.setVariationId(21L);

        when(variationOptionService.update(eq(101L), any(VariationOptionRequestDTO.class))).thenReturn(res);

        mockMvc.perform(put(basePath() + "/{id}", 101)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.value", is("XXL")))
                .andExpect(jsonPath("$.additionalPrice", is(3.5)))
                .andExpect(jsonPath("$.variationId", is(21)));

        ArgumentCaptor<VariationOptionRequestDTO> captor = ArgumentCaptor.forClass(VariationOptionRequestDTO.class);
        verify(variationOptionService).update(eq(101L), captor.capture());
        VariationOptionRequestDTO captured = captor.getValue();
        assert captured.getValue().equals("XXL");
        assert captured.getAdditionalPrice().equals(3.5);
        assert captured.getVariationId().equals(21L);
    }

    @Test
    @DisplayName("DELETE /variation-options/{id} returns 204 and calls service")
    void delete_returns204() throws Exception {
        doNothing().when(variationOptionService).delete(300L);

        mockMvc.perform(delete(basePath() + "/{id}", 300))
                .andExpect(status().isNoContent());

        verify(variationOptionService).delete(300L);
    }
}
