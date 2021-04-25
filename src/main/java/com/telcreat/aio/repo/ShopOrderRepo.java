package com.telcreat.aio.repo;

import com.telcreat.aio.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopOrderRepo extends JpaRepository<ShopOrder, Integer> {
    List<ShopOrder> findShopOrderByUserId(int userId);
}
