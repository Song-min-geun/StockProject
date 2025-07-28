package com.example.stockservice.repository;

import com.example.stockservice.domain.Stock;
import org.springframework.data.repository.CrudRepository;


public interface StockRepository extends CrudRepository<Stock,String> {
}