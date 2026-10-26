package com.example.productservice.service;

import com.example.productservice.dto.request.CreateProductRequest;
import com.example.productservice.dto.request.PatchProductRequest;
import com.example.productservice.dto.request.ProductSearchRequest;
import com.example.productservice.dto.request.UpdateProductRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.specification.ProductSpecification;


import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

@Service
public class ProductService {

        private final ProductRepository productRepository;
        private final CacheManager cacheManager;

        public ProductService(ProductRepository productRepository, CacheManager cacheManager) {
                this.productRepository = productRepository;
                this.cacheManager = cacheManager;
        }

        private void evictProductCacheAfterCommit(Long id) {
                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {
                        throw new IllegalStateException(
                                        "No active transaction synchronization");
                }

                TransactionSynchronizationManager.registerSynchronization(
                                new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                                Cache cache = cacheManager.getCache("products");

                                                if (cache != null) {
                                                        cache.evict(id);
                                                }
                                        }
                                });
        }

        @Transactional
        public ProductResponse createProduct(CreateProductRequest request) {

                Instant now = Instant.now();

                Product product = new Product();
                product.setName(request.name());
                product.setDescription(request.description());
                product.setPrice(request.price());
                product.setCurrency(request.currency());
                product.setStockQuantity(request.stockQuantity());
                product.setCreatedAt(now);
                product.setUpdatedAt(now);

                Product savedProduct = productRepository.save(product);

                return toResponse(savedProduct);
        }

        @Transactional(readOnly = true)
        @Cacheable(cacheNames = "products", key = "#id", sync = true)
        public ProductResponse getProduct(Long id) {

                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

                return toResponse(product);
        }

        @Transactional
        public ProductResponse updateProduct(
                        Long id,
                        UpdateProductRequest request) {

                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Product not found: " + id));

                if (!product.getVersion().equals(request.version())) {
                        throw new ObjectOptimisticLockingFailureException(
                                        Product.class,
                                        id);
                }

                product.setName(request.name());
                product.setDescription(request.description());
                product.setPrice(request.price());
                product.setCurrency(request.currency());
                product.setStockQuantity(request.stockQuantity());
                product.setUpdatedAt(Instant.now());

                productRepository.flush();

                ProductResponse response = toResponse(product);

                evictProductCacheAfterCommit(id);

                return response;
        }

        @Transactional
        public void deleteProduct(Long id) {

                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Product not found: " + id));

                productRepository.delete(product);
                evictProductCacheAfterCommit(id); 
        }

        @Transactional(readOnly = true)
        public Page<ProductResponse> searchProducts(
                        ProductSearchRequest request,
                        Pageable pageable) {

                Specification<Product> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

                if (request.name() != null && !request.name().isBlank()) {
                        specification = specification.and(
                                        ProductSpecification.hasName(request.name()));
                }

                if (request.currency() != null && !request.currency().isBlank()) {
                        specification = specification.and(
                                        ProductSpecification.hasCurrency(request.currency()));
                }

                if (request.minPrice() != null) {
                        specification = specification.and(
                                        ProductSpecification.priceGreaterThanOrEqualTo(
                                                        request.minPrice()));
                }

                if (request.maxPrice() != null) {
                        specification = specification.and(
                                        ProductSpecification.priceLessThanOrEqualTo(
                                                        request.maxPrice()));
                }

                if (request.minStock() != null) {
                        specification = specification.and(
                                        ProductSpecification.stockGreaterThanOrEqualTo(
                                                        request.minStock()));
                }

                if (request.maxStock() != null) {
                        specification = specification.and(
                                        ProductSpecification.stockLessThanOrEqualTo(
                                                        request.maxStock()));
                }

                return productRepository
                                .findAll(specification, pageable)
                                .map(this::toResponse);
        }

        private ProductResponse toResponse(Product product) {

                return new ProductResponse(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getCurrency(),
                                product.getStockQuantity(),
                                product.getCreatedAt(),
                                product.getUpdatedAt(),
                                product.getVersion());
        }

        @Transactional
        public ProductResponse patchProduct(
                        Long id,
                        PatchProductRequest request) {

                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Product not found: " + id));

                // Require a version to preserve optimistic locking.
                if (request.version() == null) {
                        throw new IllegalArgumentException(
                                        "Version is required for product updates");
                }

                if (!product.getVersion().equals(request.version())) {
                        throw new ObjectOptimisticLockingFailureException(
                                        Product.class,
                                        id);
                }

                // Update only fields supplied in the PATCH request.
                if (request.name() != null) {
                        product.setName(request.name());
                }

                if (request.description() != null) {
                        product.setDescription(request.description());
                }

                if (request.price() != null) {
                        product.setPrice(request.price());
                }

                if (request.currency() != null) {
                        product.setCurrency(request.currency());
                }

                if (request.stockQuantity() != null) {
                        product.setStockQuantity(request.stockQuantity());
                }

                product.setUpdatedAt(Instant.now());

                productRepository.flush();

                ProductResponse response = toResponse(product);

                evictProductCacheAfterCommit(id);

                return response;
        }

}