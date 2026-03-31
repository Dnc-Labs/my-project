package com.ecommerce.api.services;

import com.ecommerce.api.dto.request.ProductRequestDto;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public Product create(ProductRequestDto productRequestDto) {
        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        this.productRepository.save(product);
        return product;
    }

    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    public Product getProduct(Long id){
        return this.productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public String deleteProduct(Long id) {
        this.productRepository.deleteById(id);
        return "Product with " + id + " was removed";
    }
}
