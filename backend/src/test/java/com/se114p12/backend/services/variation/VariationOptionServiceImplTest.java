package com.se114p12.backend.services.variation;

import com.se114p12.backend.dtos.variation.VariationOptionRequestDTO;
import com.se114p12.backend.dtos.variation.VariationOptionResponseDTO;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.variation.Variation;
import com.se114p12.backend.entities.variation.VariationOption;
import com.se114p12.backend.exceptions.DataConflictException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.variation.VariationOptionMapper;
import com.se114p12.backend.repositories.variation.VariationOptionRepository;
import com.se114p12.backend.repositories.variation.VariationRepository;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VariationOptionServiceImplTest {

    private VariationOptionRepository variationOptionRepository;
    private VariationRepository variationRepository;
    private VariationOptionMapper variationOptionMapper;
    private VariationOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        variationOptionRepository = mock(VariationOptionRepository.class);
        variationRepository = mock(VariationRepository.class);
        variationOptionMapper = mock(VariationOptionMapper.class);
        
        service = new VariationOptionServiceImpl(
            variationOptionRepository,
            variationRepository,
            variationOptionMapper
        );
    }

    // Helper methods to create test data
    private Variation createVariation(Long id) {
        Variation variation = new Variation();
        variation.setId(id);
        variation.setName("Test Variation");
        variation.setIsMultipleChoice(true);
        variation.setCreatedAt(Instant.now());
        variation.setUpdatedAt(Instant.now());
        return variation;
    }

    private VariationOption createVariationOption(Long id, String value, Double additionalPrice, Variation variation) {
        VariationOption option = new VariationOption();
        option.setId(id);
        option.setValue(value);
        option.setAdditionalPrice(additionalPrice);
        option.setVariation(variation);
        option.setCartItems(new ArrayList<>());
        option.setCreatedAt(Instant.now());
        option.setUpdatedAt(Instant.now());
        return option;
    }

    private VariationOption createVariationOptionWithCartItems(Long id, String value, Double additionalPrice, Variation variation, int cartItemCount) {
        VariationOption option = createVariationOption(id, value, additionalPrice, variation);
        List<CartItem> cartItems = new ArrayList<>();
        for (int i = 0; i < cartItemCount; i++) {
            CartItem cartItem = mock(CartItem.class);
            cartItems.add(cartItem);
        }
        option.setCartItems(cartItems);
        return option;
    }

    private VariationOptionRequestDTO createRequestDTO(String value, Double additionalPrice, Long variationId) {
        VariationOptionRequestDTO dto = new VariationOptionRequestDTO();
        dto.setValue(value);
        dto.setAdditionalPrice(additionalPrice);
        dto.setVariationId(variationId);
        return dto;
    }

    private VariationOptionResponseDTO createResponseDTO(Long id, String value, Double additionalPrice, Long variationId) {
        VariationOptionResponseDTO dto = new VariationOptionResponseDTO();
        dto.setId(id);
        dto.setValue(value);
        dto.setAdditionalPrice(additionalPrice);
        dto.setVariationId(variationId);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ==================== getByVariationId Tests ====================

    @Test
    @DisplayName("getByVariationId - should return paginated variation options")
    void getByVariationId_success() {
        // Given
        Long variationId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("value"));
        
        Variation variation = createVariation(variationId);
        VariationOption option1 = createVariationOption(1L, "Small", 0.0, variation);
        VariationOption option2 = createVariationOption(2L, "Medium", 5.0, variation);
        
        List<VariationOption> options = List.of(option1, option2);
        Page<VariationOption> page = new PageImpl<>(options, pageable, 2);
        
        VariationOptionResponseDTO dto1 = createResponseDTO(1L, "Small", 0.0, variationId);
        VariationOptionResponseDTO dto2 = createResponseDTO(2L, "Medium", 5.0, variationId);
        
        when(variationOptionRepository.findByVariationId(variationId, pageable)).thenReturn(page);
        when(variationOptionMapper.toDto(option1)).thenReturn(dto1);
        when(variationOptionMapper.toDto(option2)).thenReturn(dto2);
        
        // When
        PageVO<VariationOptionResponseDTO> result = service.getByVariationId(variationId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Small", result.getContent().get(0).getValue());
        assertEquals("Medium", result.getContent().get(1).getValue());
        
        verify(variationOptionRepository, times(1)).findByVariationId(variationId, pageable);
    }

    @Test
    @DisplayName("getByVariationId - should return empty page when no options found")
    void getByVariationId_noOptionsFound_returnsEmptyPage() {
        // Given
        Long variationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<VariationOption> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(variationOptionRepository.findByVariationId(variationId, pageable)).thenReturn(emptyPage);
        
        // When
        PageVO<VariationOptionResponseDTO> result = service.getByVariationId(variationId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        
        verify(variationOptionRepository, times(1)).findByVariationId(variationId, pageable);
    }

    @Test
    @DisplayName("getByVariationId - should handle large page correctly")
    void getByVariationId_largePage_success() {
        // Given
        Long variationId = 1L;
        Pageable pageable = PageRequest.of(2, 5);
        
        Variation variation = createVariation(variationId);
        VariationOption option = createVariationOption(11L, "Extra Large", 15.0, variation);
        
        Page<VariationOption> page = new PageImpl<>(List.of(option), pageable, 11);
        VariationOptionResponseDTO dto = createResponseDTO(11L, "Extra Large", 15.0, variationId);
        
        when(variationOptionRepository.findByVariationId(variationId, pageable)).thenReturn(page);
        when(variationOptionMapper.toDto(option)).thenReturn(dto);
        
        // When
        PageVO<VariationOptionResponseDTO> result = service.getByVariationId(variationId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getPage());
        assertEquals(5, result.getSize());
        assertEquals(11, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(1, result.getNumberOfElements());
    }

    // ==================== create Tests ====================

    @Test
    @DisplayName("create - should successfully create variation option when variation exists and value is unique")
    void create_variationExistsAndValueUnique_success() {
        // Given
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Large", 10.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption option = createVariationOption(null, "Large", 10.0, variation);
        VariationOption savedOption = createVariationOption(1L, "Large", 10.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(1L, "Large", 10.0, variationId);
        
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.existsByValueAndVariationId("Large", variationId)).thenReturn(false);
        when(variationOptionMapper.toEntity(requestDTO)).thenReturn(option);
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(savedOption);
        when(variationOptionMapper.toDto(savedOption)).thenReturn(responseDTO);
        
        // When
        VariationOptionResponseDTO result = service.create(requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Large", result.getValue());
        assertEquals(10.0, result.getAdditionalPrice());
        assertEquals(variationId, result.getVariationId());
        
        verify(variationRepository, times(1)).findById(variationId);
        verify(variationOptionRepository, times(1)).existsByValueAndVariationId("Large", variationId);
        verify(variationOptionMapper, times(1)).toEntity(requestDTO);
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
        verify(variationOptionMapper, times(1)).toDto(savedOption);
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when variation not found")
    void create_variationNotFound_throwsException() {
        // Given
        Long variationId = 999L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Large", 10.0, variationId);
        
        when(variationRepository.findById(variationId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(requestDTO)
        );
        
        assertEquals("Variation not found", exception.getMessage());
        
        verify(variationRepository, times(1)).findById(variationId);
        verify(variationOptionRepository, never()).existsByValueAndVariationId(anyString(), anyLong());
        verify(variationOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should not throw exception when duplicate value exists (bug in original code)")
    void create_duplicateValue_noExceptionThrown() {
        // Given - This test documents a bug in the original code where duplicate check doesn't throw exception
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Small", 0.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption option = createVariationOption(null, "Small", 0.0, variation);
        VariationOption savedOption = createVariationOption(2L, "Small", 0.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(2L, "Small", 0.0, variationId);
        
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.existsByValueAndVariationId("Small", variationId)).thenReturn(true);
        when(variationOptionMapper.toEntity(requestDTO)).thenReturn(option);
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(savedOption);
        when(variationOptionMapper.toDto(savedOption)).thenReturn(responseDTO);
        
        // When - Bug: code creates errorDetails but doesn't throw exception
        VariationOptionResponseDTO result = service.create(requestDTO);
        
        // Then - The method completes successfully despite duplicate
        assertNotNull(result);
        assertEquals("Small", result.getValue());
        
        verify(variationOptionRepository, times(1)).existsByValueAndVariationId("Small", variationId);
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
    }

    @Test
    @DisplayName("create - should create option with zero additional price")
    void create_zeroAdditionalPrice_success() {
        // Given
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Free Option", 0.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption option = createVariationOption(null, "Free Option", 0.0, variation);
        VariationOption savedOption = createVariationOption(1L, "Free Option", 0.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(1L, "Free Option", 0.0, variationId);
        
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.existsByValueAndVariationId("Free Option", variationId)).thenReturn(false);
        when(variationOptionMapper.toEntity(requestDTO)).thenReturn(option);
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(savedOption);
        when(variationOptionMapper.toDto(savedOption)).thenReturn(responseDTO);
        
        // When
        VariationOptionResponseDTO result = service.create(requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getAdditionalPrice());
    }

    // ==================== update Tests ====================

    @Test
    @DisplayName("update - should successfully update variation option with new value")
    void update_withNewValue_success() {
        // Given
        Long optionId = 1L;
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Updated Large", 15.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption existingOption = createVariationOptionWithCartItems(optionId, "Large", 10.0, variation, 2);
        VariationOption updatedOption = createVariationOption(optionId, "Updated Large", 15.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(optionId, "Updated Large", 15.0, variationId);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationOptionRepository.existsByValueAndVariationId("Updated Large", variationId)).thenReturn(false);
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(updatedOption);
        when(variationOptionMapper.toDto(updatedOption)).thenReturn(responseDTO);
        
        // When
        VariationOptionResponseDTO result = service.update(optionId, requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Large", result.getValue());
        assertEquals(15.0, result.getAdditionalPrice());
        
        // Verify cart items were marked as unavailable
        existingOption.getCartItems().forEach(cartItem -> 
            verify(cartItem, times(1)).setAvailable(false)
        );
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationOptionRepository, times(1)).existsByValueAndVariationId("Updated Large", variationId);
        verify(variationRepository, times(1)).findById(variationId);
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
    }

    @Test
    @DisplayName("update - should successfully update with same value")
    void update_withSameValue_success() {
        // Given
        Long optionId = 1L;
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Large", 15.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption existingOption = createVariationOption(optionId, "Large", 10.0, variation);
        VariationOption updatedOption = createVariationOption(optionId, "Large", 15.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(optionId, "Large", 15.0, variationId);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(updatedOption);
        when(variationOptionMapper.toDto(updatedOption)).thenReturn(responseDTO);
        
        // When
        VariationOptionResponseDTO result = service.update(optionId, requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("Large", result.getValue());
        assertEquals(15.0, result.getAdditionalPrice());
        
        // Should not check for duplicates when value is the same
        verify(variationOptionRepository, never()).existsByValueAndVariationId(anyString(), anyLong());
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when option not found")
    void update_optionNotFound_throwsException() {
        // Given
        Long optionId = 999L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Large", 10.0, 1L);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.update(optionId, requestDTO)
        );
        
        assertEquals("Variation option not found", exception.getMessage());
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationRepository, never()).findById(anyLong());
        verify(variationOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw DataConflictException when new value is duplicate")
    void update_duplicateValue_throwsException() {
        // Given
        Long optionId = 1L;
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Medium", 10.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption existingOption = createVariationOption(optionId, "Large", 10.0, variation);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationOptionRepository.existsByValueAndVariationId("Medium", variationId)).thenReturn(true);
        
        // When & Then
        DataConflictException exception = assertThrows(
            DataConflictException.class,
            () -> service.update(optionId, requestDTO)
        );
        
        assertEquals("Data conflict occurred", exception.getMessage());
        assertNotNull(exception.getErrors());
        assertTrue(exception.getErrors().containsKey("value"));
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationOptionRepository, times(1)).existsByValueAndVariationId("Medium", variationId);
        verify(variationRepository, never()).findById(anyLong());
        verify(variationOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when variation not found")
    void update_variationNotFound_throwsException() {
        // Given
        Long optionId = 1L;
        Long oldVariationId = 1L;
        Long newVariationId = 2L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Large", 10.0, newVariationId);
        
        Variation oldVariation = createVariation(oldVariationId);
        VariationOption existingOption = createVariationOption(optionId, "Large", 10.0, oldVariation);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationRepository.findById(newVariationId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.update(optionId, requestDTO)
        );
        
        assertEquals("Variation not found", exception.getMessage());
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationRepository, times(1)).findById(newVariationId);
        verify(variationOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should mark cart items as unavailable when updating")
    void update_marksCartItemsUnavailable() {
        // Given
        Long optionId = 1L;
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Updated", 10.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption existingOption = createVariationOptionWithCartItems(optionId, "Original", 5.0, variation, 3);
        VariationOption updatedOption = createVariationOption(optionId, "Updated", 10.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(optionId, "Updated", 10.0, variationId);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationOptionRepository.existsByValueAndVariationId("Updated", variationId)).thenReturn(false);
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(updatedOption);
        when(variationOptionMapper.toDto(updatedOption)).thenReturn(responseDTO);
        
        // When
        service.update(optionId, requestDTO);
        
        // Then - Verify all cart items were marked unavailable
        assertEquals(3, existingOption.getCartItems().size());
        existingOption.getCartItems().forEach(cartItem -> 
            verify(cartItem, times(1)).setAvailable(false)
        );
    }

    @Test
    @DisplayName("update - should handle option with no cart items")
    void update_noCartItems_success() {
        // Given
        Long optionId = 1L;
        Long variationId = 1L;
        VariationOptionRequestDTO requestDTO = createRequestDTO("Updated", 10.0, variationId);
        
        Variation variation = createVariation(variationId);
        VariationOption existingOption = createVariationOption(optionId, "Original", 5.0, variation);
        VariationOption updatedOption = createVariationOption(optionId, "Updated", 10.0, variation);
        VariationOptionResponseDTO responseDTO = createResponseDTO(optionId, "Updated", 10.0, variationId);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(existingOption));
        when(variationOptionRepository.existsByValueAndVariationId("Updated", variationId)).thenReturn(false);
        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(updatedOption);
        when(variationOptionMapper.toDto(updatedOption)).thenReturn(responseDTO);
        
        // When
        VariationOptionResponseDTO result = service.update(optionId, requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getValue());
        // No exceptions should be thrown even with empty cart items list
    }

    // ==================== delete Tests ====================

    @Test
    @DisplayName("delete - should successfully delete variation option and mark cart items unavailable")
    void delete_success() {
        // Given
        Long optionId = 1L;
        Variation variation = createVariation(1L);
        VariationOption option = createVariationOptionWithCartItems(optionId, "Large", 10.0, variation, 2);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(option);
        doNothing().when(variationOptionRepository).delete(any(VariationOption.class));

        // When
        service.delete(optionId);
        
        // Then
        // Verify cart items were marked as unavailable
        option.getCartItems().forEach(cartItem -> 
            verify(cartItem, times(1)).setAvailable(false)
        );
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
        verify(variationOptionRepository, times(1)).delete(any(VariationOption.class));
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException when option not found")
    void delete_optionNotFound_throwsException() {
        // Given
        Long optionId = 999L;
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.delete(optionId)
        );
        
        assertEquals("Variation option not found", exception.getMessage());
        
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationOptionRepository, never()).save(any());
        verify(variationOptionRepository, never()).delete(any(VariationOption.class));
    }

    @Test
    @DisplayName("delete - should handle option with no cart items")
    void delete_noCartItems_success() {
        // Given
        Long optionId = 1L;
        Variation variation = createVariation(1L);
        VariationOption option = createVariationOption(optionId, "Large", 10.0, variation);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(option);
        doNothing().when(variationOptionRepository).delete(any(VariationOption.class));

        // When
        service.delete(optionId);
        
        // Then
        verify(variationOptionRepository, times(1)).findById(optionId);
        verify(variationOptionRepository, times(1)).save(any(VariationOption.class));
        verify(variationOptionRepository, times(1)).delete(any(VariationOption.class));
        // No exceptions should be thrown with empty cart items
    }

    @Test
    @DisplayName("delete - should mark multiple cart items as unavailable")
    void delete_multipleCartItems_marksAllUnavailable() {
        // Given
        Long optionId = 1L;
        Variation variation = createVariation(1L);
        VariationOption option = createVariationOptionWithCartItems(optionId, "Large", 10.0, variation, 5);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(option);
        doNothing().when(variationOptionRepository).delete(any(VariationOption.class));

        // When
        service.delete(optionId);
        
        // Then
        assertEquals(5, option.getCartItems().size());
        option.getCartItems().forEach(cartItem -> 
            verify(cartItem, times(1)).setAvailable(false)
        );
    }

    @Test
    @DisplayName("delete - should save before deleting")
    void delete_savesBeforeDeleting() {
        // Given
        Long optionId = 1L;
        Variation variation = createVariation(1L);
        VariationOption option = createVariationOption(optionId, "Large", 10.0, variation);
        
        when(variationOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(option);
        doNothing().when(variationOptionRepository).delete(any(VariationOption.class));

        // When
        service.delete(optionId);
        
        // Then - Verify operations happen in correct order
        var inOrder = inOrder(variationOptionRepository);
        inOrder.verify(variationOptionRepository).findById(optionId);
        inOrder.verify(variationOptionRepository).save(any(VariationOption.class));
        inOrder.verify(variationOptionRepository).delete(any(VariationOption.class));
    }
}

