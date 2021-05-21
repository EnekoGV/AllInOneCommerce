package com.telcreat.aio.repo;

import com.telcreat.aio.model.Item;
import com.telcreat.aio.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepo  extends JpaRepository<Item, Integer>{
    List<Item> findItemsByItemCategory_IdAndNameContainsAndStatus(int itemCategoryId,String itemName, Item.Status itemStatus);
    List<Item> findItemsByShop_Id(int shopId);
    Optional<Item> findItemByIdAndStatus(int itemId, Item.Status itemStatus);
    List<Item> findItemsByShopIdAndStatus(int shopId, Item.Status itemStatus);
    List<Item> findItemsByNameContainingAndStatus(String search, Item.Status itemStatus);
}
