package profect.group1.goormdotcom.stock.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.group1.goormdotcom.stock.config.RetryConfig;
import profect.group1.goormdotcom.stock.controller.dto.ProductStockAdjustmentRequestDto;
import profect.group1.goormdotcom.stock.controller.dto.StockAdjustmentRequestDto;
import profect.group1.goormdotcom.stock.controller.dto.StockAdjustmentResponseDto;
import profect.group1.goormdotcom.stock.controller.dto.StockRequestDto;
import profect.group1.goormdotcom.stock.controller.dto.StockResponseDto;
import profect.group1.goormdotcom.stock.controller.mapper.StockDtoMapper;
import profect.group1.goormdotcom.stock.domain.Stock;
import profect.group1.goormdotcom.stock.domain.exception.InsufficientStockException;
import profect.group1.goormdotcom.stock.service.AdjustStockStatus;
import profect.group1.goormdotcom.stock.service.StockService;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.ErrorStatus;
import profect.group1.goormdotcom.apiPayload.code.status.SuccessStatus;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Slf4j
public class StockController implements StockApiDocs {

    private final StockService stockService;
    private final RetryConfig retryConfig;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<StockResponseDto> registerStock(
        @RequestBody StockRequestDto stockRequestDto
    ) {
        Stock stock = stockService.registerStock(stockRequestDto.productId(), stockRequestDto.stockQuantity());
        return ApiResponse.of(SuccessStatus._OK, StockDtoMapper.toStockResponseDto(stock));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<StockResponseDto> updateStock(
        @PathVariable(value="productId") UUID productId, 
        @RequestBody int stockQuantity
    ) {
        Stock stock = stockService.updateStock(productId, stockQuantity);
        return ApiResponse.of(SuccessStatus._OK, StockDtoMapper.toStockResponseDto(stock));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<StockResponseDto> getStock(
        @PathVariable(value = "productId") UUID productId
    ) {
        
        Stock stock = stockService.getStock(productId);
        return ApiResponse.of(SuccessStatus._OK, StockDtoMapper.toStockResponseDto(stock));        
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<UUID> deleteStock(
        @PathVariable(value = "productId") UUID productId
    ) {
        stockService.deleteStock(productId);
        return ApiResponse.of(SuccessStatus._OK, productId);
    }
    
    @PostMapping("/decrease")
    public ApiResponse<StockAdjustmentResponseDto> decreaseStocks(
        @RequestBody @Valid StockAdjustmentRequestDto stockAdjustmentRequestDto
    ) {
        Map<UUID, Integer> requestedQuantityMap = new HashMap<UUID, Integer>();
        for (ProductStockAdjustmentRequestDto dto : stockAdjustmentRequestDto.products()) {
            requestedQuantityMap.put(dto.productId(), dto.requestedStockQuantity());
        }

        // 재고 차감 시도
        AdjustStockStatus status = stockService.decreaseStocks(requestedQuantityMap);
        
        // 재고 차감 실패시
        if (status == AdjustStockStatus.FAILED) {
            return ApiResponse.onFailure(
                ErrorStatus._ADJUST_STOCK_FAILED.getCode() , 
                ErrorStatus._ADJUST_STOCK_FAILED.getMessage(), 
                new StockAdjustmentResponseDto(false, new ArrayList<UUID>(requestedQuantityMap.keySet()))
            );
        } else if (status == AdjustStockStatus.INSUFFICIENT) {
            return ApiResponse.onFailure(
                ErrorStatus._INSUFFICIENT_STOCK_QUANTITY.getCode() , 
                ErrorStatus._INSUFFICIENT_STOCK_QUANTITY.getMessage(), 
                new StockAdjustmentResponseDto(false, new ArrayList<UUID>(requestedQuantityMap.keySet()))
            );
        } else if (status == AdjustStockStatus.INTERRUPT) {
            return ApiResponse.onFailure(
                ErrorStatus._CONFLICT.getCode(),
                "Interrupted",
                new StockAdjustmentResponseDto(false, new ArrayList<>(requestedQuantityMap.keySet()))
            );
        } 
        
        // 재고 차감 성공 시
        return ApiResponse.of(SuccessStatus._OK, new StockAdjustmentResponseDto(true, new ArrayList<UUID>(requestedQuantityMap.keySet()))); 
    }

    @PostMapping("/increase")
    public ApiResponse<StockAdjustmentResponseDto> increaseStocks(
        @RequestBody @Valid StockAdjustmentRequestDto stockAdjustmentRequestDto
    ) {
        Map<UUID, Integer> requestedQuantityMap = new HashMap<UUID, Integer>();
        for (ProductStockAdjustmentRequestDto dto : stockAdjustmentRequestDto.products()) {
            requestedQuantityMap.put(dto.productId(), dto.requestedStockQuantity());
        }

        // 재고 증가 시도
        AdjustStockStatus status = stockService.increaseStocks(requestedQuantityMap);

        // 재고 증가 실패시
        if (status == AdjustStockStatus.FAILED) {
            return ApiResponse.onFailure(
                ErrorStatus._ADJUST_STOCK_FAILED.getCode() , 
                ErrorStatus._ADJUST_STOCK_FAILED.getMessage(), 
                new StockAdjustmentResponseDto(false, new ArrayList<UUID>(requestedQuantityMap.keySet()))
            );
        } else if (status == AdjustStockStatus.INTERRUPT) {
            return ApiResponse.onFailure(
                ErrorStatus._CONFLICT.getCode(),
                ErrorStatus._CONFLICT.getMessage(),
                new StockAdjustmentResponseDto(false, new ArrayList<>(requestedQuantityMap.keySet()))
            );
        } 
        
        // 재고 증가 성공 시
        return ApiResponse.of(SuccessStatus._OK, new StockAdjustmentResponseDto(true, new ArrayList<UUID>(requestedQuantityMap.keySet()))); 
    }
    
}
