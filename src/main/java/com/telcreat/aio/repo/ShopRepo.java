package com.telcreat.aio.repo;

import com.telcreat.aio.model.Shop;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepo extends JpaRepository<Shop, Integer> {

    //Add a function to get shops by user IDs
    boolean existsByOwnerId(int OwnerId);
    Optional <Shop> findShopsByOwnerId(int OwnerId);

}
