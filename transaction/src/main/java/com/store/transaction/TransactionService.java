package com.store.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {


    private final TransactionRepository transactionRepository;
    @Value("${product.service.url}")
    private String productServiceUrl;

    private final RestTemplate restTemplate;

    @Configuration
    public static class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }


    public List<TransactionDto> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto transactionDto, String token) {
        String productUrl = productServiceUrl + "/api/products/" + transactionDto.getProductId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Product> response = restTemplate.exchange(productUrl, HttpMethod.GET, requestEntity, Product.class);
        Product product = response.getBody();
        System.out.println(product);

        if (product == null) {
            throw new RuntimeException("Product not found for id: " + transactionDto.getProductId());
        }

        // Ubah quantity menjadi BigDecimal dan hitung total price
        BigDecimal quantity = new BigDecimal(transactionDto.getQuantity());
        BigDecimal totalPrice = product.getPrice().multiply(quantity);  // Perkalian BigDecimal

            System.out.println(transactionDto.getUserId());
        // Buat transaksi baru
        Transaction transaction = new Transaction();
        transaction.setUser(transactionDto.getUserId());
        transaction.setProduct(transactionDto.getProductId());
        transaction.setQuantity(transactionDto.getQuantity()); // Simpan quantity dalam Integer
        transaction.setTotalPrice(totalPrice.doubleValue());  // Simpan totalPrice sebagai Double
        transaction.setCreatedAt(LocalDateTime.now());

            // Kurangi stok produk
            int newStock = product.getStock() - transactionDto.getQuantity();
            System.out.println(product.getStock());
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update stok di product service
            product.setStock(newStock);
            updateProductStock(product, token); // Kirim request untuk update stok produk

            // Simpan transaksi
            Transaction savedTransaction = transactionRepository.save(transaction);
            return mapToDTO(savedTransaction);

        } catch (Exception e) {
            throw new RuntimeException("Error processing transaction: " + e.getMessage());
        }
    }
    private void updateProductStock(Product product, String token) {
        String updateProductUrl = productServiceUrl + "/api/products/" + product.getId();

        // Buat body untuk request PUT
        Product updatedProduct = new Product();
        updatedProduct.setName(product.getName());
        updatedProduct.setDescription(product.getDescription());
        updatedProduct.setPrice(product.getPrice());
        updatedProduct.setCategory(product.getCategory());
        updatedProduct.setStock(product.getStock()); // Stok yang sudah dikurangi

        // Buat headers dan request entity
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        HttpEntity<Product> requestEntity = new HttpEntity<>(updatedProduct, headers);

        // Kirim PUT request ke product service
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.exchange(updateProductUrl, HttpMethod.PUT, requestEntity, Product.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product stock: " + e.getMessage());
        }
    }

    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
        return mapToDTO(transaction);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionDto transactionDto, String token) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));

        // Fetch product details from product service
        String productUrl = productServiceUrl + "/api/products/" + transactionDto.getProductId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Product> response = restTemplate.exchange(productUrl, HttpMethod.GET, requestEntity, Product.class);
            Product product = response.getBody();

            if (product == null) {
                throw new RuntimeException("Product not found for id: " + transactionDto.getProductId());
            }

            // Adjust stock for the updated transaction
            int previousQuantity = existingTransaction.getQuantity();
            int newQuantity = transactionDto.getQuantity();
            int stockAdjustment = previousQuantity - newQuantity;

            int updatedStock = product.getStock() + stockAdjustment;

            // Update product stock in the product microservice
            product.setStock(updatedStock);
            updateProductStock(product, token);

            // Update transaction details
            BigDecimal quantity = BigDecimal.valueOf(newQuantity);
            BigDecimal totalPrice = product.getPrice().multiply(quantity);

            existingTransaction.setUser(transactionDto.getUserId());
            existingTransaction.setProduct(transactionDto.getProductId());
            existingTransaction.setQuantity(newQuantity);
            existingTransaction.setTotalPrice(totalPrice.doubleValue());
            existingTransaction.setCreatedAt(LocalDateTime.now());

            Transaction updatedTransaction = transactionRepository.save(existingTransaction);
            return mapToDTO(updatedTransaction);

        } catch (Exception e) {
            throw new RuntimeException("Error updating transaction: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
            // Delete transaction
            transactionRepository.delete(transaction);
    }

    private TransactionDto mapToDTO(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser()); // Map user field to userId
        dto.setProductId(transaction.getProduct()); // Map product field to productId
        dto.setQuantity(transaction.getQuantity());
        dto.setTotalPrice(transaction.getTotalPrice());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}

