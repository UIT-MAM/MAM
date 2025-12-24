package com.se114p12.backend.controllers.variation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.variation.VariationRequestDTO;
import com.se114p12.backend.dtos.variation.VariationResponseDTO;
import com.se114p12.backend.services.variation.VariationService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VariationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private VariationService variationService;

    @BeforeEach
    void setUp() {
        variationService = Mockito.mock(VariationService.class);
        VariationController controller = new VariationController(variationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/variations";
    }

    @Test
    @DisplayName("GET /variations with productId and filters returns 200 and page content")
    void getVariationsByProduct_returns200_withFilters() throws Exception {
        VariationResponseDTO v1 = new VariationResponseDTO();
        v1.setId(10L);
        v1.setName("Color");
        v1.setProductId(1L);
        v1.setIsMultipleChoice(true);

        VariationResponseDTO v2 = new VariationResponseDTO();
        v2.setId(11L);
        v2.setName("Size");
        v2.setProductId(1L);
        v2.setIsMultipleChoice(false);

        PageVO<VariationResponseDTO> page = PageVO.<VariationResponseDTO>builder()
                .page(0)
                .size(2)
                .totalElements(2L)
                .totalPages(1)
                .numberOfElements(2)
                .content(List.of(v1, v2))
                .build();

        when(variationService.getVariationsByProductId(eq(1L), eq("Color"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(basePath())
                        .param("productId", "1")
                        .param("name", "Color")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(10)))
                .andExpect(jsonPath("$.content[0].name", is("Color")))
                .andExpect(jsonPath("$.content[1].id", is(11)))
                .andExpect(jsonPath("$.content[1].name", is("Size")));

        verify(variationService).getVariationsByProductId(eq(1L), eq("Color"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /variations with productId only returns 200")
    void getVariationsByProduct_returns200_withoutName() throws Exception {
        PageVO<VariationResponseDTO> page = PageVO.<VariationResponseDTO>builder()
                .page(0)
                .size(0)
                .totalElements(0L)
                .totalPages(0)
                .numberOfElements(0)
                .content(List.of())
                .build();

        when(variationService.getVariationsByProductId(eq(2L), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(basePath())
                        .param("productId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(variationService).getVariationsByProductId(eq(2L), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /variations returns 200 with created variation")
    void createVariation_returns200() throws Exception {
        VariationRequestDTO req = new VariationRequestDTO();
        req.setName("Material");
        req.setIsMultipleChoice(false);
        req.setProductId(3L);

        VariationResponseDTO res = new VariationResponseDTO();
        res.setId(100L);
        res.setName("Material");
        res.setProductId(3L);
        res.setIsMultipleChoice(false);

        when(variationService.create(any(VariationRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post(basePath())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Material")))
                .andExpect(jsonPath("$.productId", is(3)))
                .andExpect(jsonPath("$.isMultipleChoice", is(false)));

        ArgumentCaptor<VariationRequestDTO> captor = ArgumentCaptor.forClass(VariationRequestDTO.class);
        verify(variationService).create(captor.capture());
        VariationRequestDTO captured = captor.getValue();
        assert captured.getName().equals("Material");
        assert captured.getProductId().equals(3L);
        assert captured.getIsMultipleChoice().equals(false);
    }

    @Test
    @DisplayName("PUT /variations/{id} returns 200 with updated variation")
    void updateVariation_returns200() throws Exception {
        VariationRequestDTO req = new VariationRequestDTO();
        req.setName("Flavor");
        req.setIsMultipleChoice(true);
        req.setProductId(5L);

        VariationResponseDTO res = new VariationResponseDTO();
        res.setId(200L);
        res.setName("Flavor");
        res.setProductId(5L);
        res.setIsMultipleChoice(true);

        when(variationService.update(eq(200L), any(VariationRequestDTO.class))).thenReturn(res);

        mockMvc.perform(put(basePath() + "/{id}", 200)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(200)))
                .andExpect(jsonPath("$.name", is("Flavor")))
                .andExpect(jsonPath("$.productId", is(5)))
                .andExpect(jsonPath("$.isMultipleChoice", is(true)));

        ArgumentCaptor<VariationRequestDTO> captor = ArgumentCaptor.forClass(VariationRequestDTO.class);
        verify(variationService).update(eq(200L), captor.capture());
        VariationRequestDTO captured = captor.getValue();
        assert captured.getName().equals("Flavor");
        assert captured.getProductId().equals(5L);
        assert captured.getIsMultipleChoice().equals(true);
    }

    @Test
    @DisplayName("DELETE /variations/{id} returns 204 and calls service")
    void deleteVariation_returns204() throws Exception {
        doNothing().when(variationService).delete(300L);

        mockMvc.perform(delete(basePath() + "/{id}", 300))
                .andExpect(status().isNoContent());

        verify(variationService).delete(300L);
    }
}
