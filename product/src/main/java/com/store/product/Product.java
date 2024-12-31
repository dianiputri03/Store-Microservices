package com.store.product;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String imageUrl;



    // Menggunakan enum sebagai tipe data biasa, bukan asosiasi relasi
    @Enumerated(EnumType.STRING)  // Menyimpan enum sebagai string di database
    @Column(nullable = false)
    private com.store.product.ProductCategory category;

    @Column(nullable = false)
    private String seller;





    // Menambahkan variabel active
    @Column(nullable = false)
    private boolean active = true;

    // Constructor default
    public Product() {}

    // Constructor dengan parameter id
    public Product(Long id) {
        this.id = id;
    }

    // Getter dan Setter untuk seller

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
