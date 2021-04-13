package com.telcreat.aio.repo;

import com.telcreat.aio.model.Item;
import com.telcreat.aio.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepo  extends JpaRepository<Item, Integer> {
    List<Item> findItemsByItemCategory_IdAndName(int itemCategoryId,String itemName);
    List<Item> findItemsByShop_Id(int shopId);
}
