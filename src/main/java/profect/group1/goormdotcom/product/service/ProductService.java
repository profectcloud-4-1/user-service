package profect.group1.goormdotcom.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.product.domain.Product;
import profect.group1.goormdotcom.product.infrastructure.client.StockClient;
import profect.group1.goormdotcom.product.infrastructure.client.dto.StockRequestDto;
import profect.group1.goormdotcom.product.infrastructure.client.dto.StockResponseDto;
import profect.group1.goormdotcom.product.repository.ProductImageRepository;
import profect.group1.goormdotcom.product.repository.ProductRepository;
import profect.group1.goormdotcom.product.repository.entity.ProductEntity;
import profect.group1.goormdotcom.product.repository.entity.ProductImageEntity;
import profect.group1.goormdotcom.product.repository.mapper.ProductMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final StockClient stockClient;

    public UUID createProduct(
        final UUID brandId,
        final UUID categoryId,
        final String productName,
        final int price,
        final int stockQuantity,
        final String description
    ) {
        final UUID productId = UUID.randomUUID();
        
        ProductEntity productEntity = new ProductEntity(
            productId, 
            brandId, 
            categoryId, 
            productName, 
            price, 
            description
        );

        
        StockRequestDto stockRequestDto = new StockRequestDto(productId, stockQuantity);
        ApiResponse<StockResponseDto> response = stockClient.registerStock(stockRequestDto);
        StockResponseDto stockResponseDto = response.getResult();
        if (stockResponseDto == null) {
            throw new IllegalStateException("Failed to register stock");
        }
        
        productRepository.save(productEntity);
            
        return productId;
    }

    public Product updateProduct(
        final UUID productId,
        final UUID bradnId,
        final UUID categoryId,
        final String productName,
        final int price,
        final String description
    ) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

        if (productEntity.getBrandId() != bradnId) {
            throw new IllegalStateException("Product is not owned by your brand.");
        }

        ProductEntity newProductEntity = new ProductEntity(
            productId, productEntity.getBrandId(), categoryId, productName, price, description
        );

        productRepository.save(newProductEntity);
        
        return ProductMapper.toDomain(newProductEntity, productImageRepository.findByProductId(productId));
    }

    public void deleteProduct(
        final UUID productId,
        final UUID brandId
    ) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

        if (productEntity.getBrandId() != brandId) {
            throw new IllegalStateException("Product is not owned by your brand.");
        }
        productRepository.deleteById(productId);
        List<ProductImageEntity> imageEntities = productImageRepository.findByProductId(productId);
        List<UUID> imageIds = imageEntities.stream().map(ProductImageEntity::getId).toList();
        deleteProductImages(imageIds);
    }

    public void deleteProducts(
        final List<UUID> productIds,
        final UUID brandId
    ) {
        // TODO: brandId 체크 로직 추가 (상품의 소유권을 지닌 브랜드만 수정가능해야함.)
        for (UUID productId: productIds) {
            ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

            if (productEntity.getBrandId() != brandId) {
                throw new IllegalStateException("Product is not owned by your brand.");
            }
        }
        
        productRepository.deleteAllById(productIds);
        
    }

    public Product getProduct(
        final UUID productId
    ) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Prdocut not found"));

        List<ProductImageEntity> imageEntities = productImageRepository.findByProductId(productId);
        return ProductMapper.toDomain(productEntity, imageEntities);
    }

    public void createProductImages(List<UUID> imageIds, UUID productId) {
        List<ProductImageEntity> productImageEntities = imageIds.stream()
            .map(id -> new ProductImageEntity(id, productId)).toList();
        
        productImageRepository.saveAll(productImageEntities);
    }

    public UUID uploadProductImage() { 
        UUID imageId = UUID.randomUUID();

        return imageId;
    }

    public void deleteProductImage(final UUID imageId) {
        productImageRepository.deleteById(imageId);
    }

    public void deleteProductImages(final List<UUID> imageIds) {
        productImageRepository.deleteAllById(imageIds);
    }
}
