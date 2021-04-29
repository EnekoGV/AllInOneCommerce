package com.telcreat.aio.repo;

import com.telcreat.aio.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopOrderRepo extends JpaRepository<ShopOrder, Integer> {
    Optional<ShopOrder> findShopOrderByIdAndShopOrderStatus(int id, ShopOrder.ShopOrderStatus shopOrderStatus);
    Optional<ShopOrder> findShopOrderByIdAndShopOrderStatusNotOrShopOrderStatusNot(int id, ShopOrder.ShopOrderStatus shopOrderStatus, ShopOrder.ShopOrderStatus shopOrderStatus2);
    List<ShopOrder> findShopOrdersByUser_Id(int userId);
    List<ShopOrder> findShopOrdersByShop_Id(int shopId);
}
