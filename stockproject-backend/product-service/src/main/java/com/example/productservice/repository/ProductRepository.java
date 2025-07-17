package com.example.productservice.repository;

import com.example.productservice.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@EnableMongoRepositories
public interface ProductRepository extends MongoRepository<Product,Long> {
    List<Product> findAllByIdIn (Iterable<String> Ids);
}
