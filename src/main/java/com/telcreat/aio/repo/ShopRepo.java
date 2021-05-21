package com.telcreat.aio.repo;

import com.telcreat.aio.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepo extends JpaRepository<Shop, Integer> {

    boolean existsByOwnerId(int OwnerId);
    Optional<Shop> findShopByOwnerId(int OwnerId);
    Optional<Shop> findShopByIdAndStatus(int shopId, Shop.Status shopStatus);
    Optional<Shop> findShopByOwnerIdAndStatus(int ownerId, Shop.Status shopStatus);
    List<Shop> findShopsByStatus(Shop.Status status);
}
