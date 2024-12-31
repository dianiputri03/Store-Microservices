package com.store.product;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import com.store.product.ProductCategory;

@Data // Lombok otomatis menghasilkan getter dan setter
public class ProductDto {
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Category is required")
    private ProductCategory category;  // Menggunakan enum ProductCategory

    private String imageUrl;
    private Long sellerId;
    private String sellerUsername;
    private Boolean active;


}
