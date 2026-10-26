package com.example.productservice.controller;

import com.example.productservice.dto.request.CreateProductRequest;
import com.example.productservice.dto.request.PatchProductRequest;
import com.example.productservice.dto.request.ProductSearchRequest;
import com.example.productservice.dto.request.UpdateProductRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

        private final ProductService productService;

        public ProductController(ProductService productService) {
                this.productService = productService;
        }

        

        @PostMapping
        public ResponseEntity<ProductResponse> createProduct(
                        @Valid @RequestBody CreateProductRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(productService.createProduct(request));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductResponse> getProduct(
                        @PathVariable Long id) {

                
                return ResponseEntity.ok(
                                productService.getProduct(id));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ProductResponse> updateProduct(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateProductRequest request) {

                return ResponseEntity.ok(
                                productService.updateProduct(id, request));
        }

        @PatchMapping("/{id}")
        public ResponseEntity<ProductResponse> patchProduct(
                        @PathVariable Long id,
                        @Valid @RequestBody PatchProductRequest request) {

                return ResponseEntity.ok(
                                productService.patchProduct(id, request));
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteProduct(@PathVariable Long id) {
                productService.deleteProduct(id);
        }

        @GetMapping
        public ResponseEntity<Page<ProductResponse>> searchProducts(
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String currency,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) Integer minStock,
                        @RequestParam(required = false) Integer maxStock,
                        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

                ProductSearchRequest request = new ProductSearchRequest(
                                name,
                                currency,
                                minPrice,
                                maxPrice,
                                minStock,
                                maxStock);

                return ResponseEntity.ok(
                                productService.searchProducts(request, pageable));
        }
}
