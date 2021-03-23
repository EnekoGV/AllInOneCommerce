package com.telcreat.aio.repo;

import com.telcreat.aio.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderRepo extends JpaRepository<ShopOrder, Integer> {
}
