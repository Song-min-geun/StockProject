package com.example.stockservice.consumer;

import com.example.dto.ProductCreatedEvent;
import com.example.stockservice.domain.Stock;
import com.example.stockservice.repository.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductConsumerTest {

    @InjectMocks
    private ProductConsumer productConsumer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StockRepository stockRepository;

    @Test
    @DisplayName("상품 생성 이벤트: 성공 - 메시지를 성공적으로 역직렬화하고 재고 정보를 저장한다.")
    void listenProductCreation_Success() throws JsonProcessingException {
        // given
        String productId = "product-id-101";
        int initialStock = 100;
        
        ProductCreatedEvent event = new ProductCreatedEvent(productId, initialStock);
        String message = "{\"productId\":\"product-id-101\",\"initialStock\":100}";
        
        when(objectMapper.readValue(message, ProductCreatedEvent.class)).thenReturn(event);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // when
        productConsumer.listenProductCreation(message);
        
        // then
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(objectMapper).readValue(message, ProductCreatedEvent.class);
        verify(stockRepository).save(stockCaptor.capture());
        
        Stock capturedStock = stockCaptor.getValue();
        assertThat(capturedStock.getProductId()).isEqualTo(productId);
        assertThat(capturedStock.getQuantity()).isEqualTo(initialStock);
    }

    @Test
    @DisplayName("상품 생성 이벤트: 실패 - 메시지 역직렬화에 실패하면 예외를 처리한다.")
    void listenProductCreation_Fail_JsonProcessingException() throws JsonProcessingException {
        // given
        String invalidMessage = "invalid-json-message";
        
        when(objectMapper.readValue(invalidMessage, ProductCreatedEvent.class))
                .thenThrow(new JsonProcessingException("JSON 파싱 에러") {});
        
        // when
        productConsumer.listenProductCreation(invalidMessage);
        
        // then
        verify(objectMapper).readValue(invalidMessage, ProductCreatedEvent.class);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("상품 생성 이벤트: 실패 - 재고 정보 저장에 실패하면 예외를 처리한다.")
    void listenProductCreation_Fail_RepositoryException() throws JsonProcessingException {
        // given
        String productId = "product-id-101";
        int initialStock = 100;
        
        ProductCreatedEvent event = new ProductCreatedEvent(productId, initialStock);
        String message = "{\"productId\":\"product-id-101\",\"initialStock\":100}";
        
        when(objectMapper.readValue(message, ProductCreatedEvent.class)).thenReturn(event);
        when(stockRepository.save(any(Stock.class))).thenThrow(new RuntimeException("저장 실패"));
        
        // when
        productConsumer.listenProductCreation(message);
        
        // then
        verify(objectMapper).readValue(message, ProductCreatedEvent.class);
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("상품 삭제 이벤트: 성공 - 상품 ID를 받아 재고 정보를 삭제한다.")
    void listenProductDeletion_Success() {
        // given
        String productId = "product-id-101";
        doNothing().when(stockRepository).deleteById(productId);
        
        // when
        productConsumer.listenProductDeletion(productId);
        
        // then
        verify(stockRepository).deleteById(productId);
    }

    @Test
    @DisplayName("상품 삭제 이벤트: 실패 - 재고 정보 삭제에 실패하면 예외를 처리한다.")
    void listenProductDeletion_Fail_RepositoryException() {
        // given
        String productId = "product-id-101";
        doThrow(new RuntimeException("삭제 실패")).when(stockRepository).deleteById(productId);
        
        // when
        productConsumer.listenProductDeletion(productId);
        
        // then
        verify(stockRepository).deleteById(productId);
    }
}