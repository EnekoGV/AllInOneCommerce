package com.telcreat.aio.repo;

import com.telcreat.aio.model.Shop;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepo extends JpaRepository<Shop, Integer> {

    //Add a function to get shops by user IDs
    boolean existsByOwnerId(int OwnerId);
    Shop findShopsByOwnerId(int OwnerId);

}
