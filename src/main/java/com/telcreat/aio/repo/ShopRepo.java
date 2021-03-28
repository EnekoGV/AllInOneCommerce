package com.telcreat.aio.repo;

import com.telcreat.aio.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepo extends JpaRepository<Shop, Integer> {
}
