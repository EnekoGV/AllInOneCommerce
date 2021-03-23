package com.telcreat.aio.service;

import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.repo.ShopOrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopOrderService {

    private final ShopOrderRepo shopOrderRepo;

    @Autowired
    public ShopOrderService(ShopOrderRepo shopOrderRepo) {
        this.shopOrderRepo = shopOrderRepo;
    }

    // BASIC method
    public List<ShopOrder> findAllShopOrders (){
        return shopOrderRepo.findAll();
    }

    // BASIC method
    public ShopOrder findShopOrderById (int shopOrderId){
        ShopOrder tempShopOrder = null;
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findById(shopOrderId);
        if (foundShopOrder.isPresent()){
            tempShopOrder = foundShopOrder.get();
        }
        return tempShopOrder;
    }

    // BASIC method -- This may not be a basic method
    public ShopOrder createShopOrder (ShopOrder newShopOrder){
        ShopOrder tempShopOrder = null;
        if (!shopOrderRepo.existsById(newShopOrder.getId())){
            tempShopOrder = shopOrderRepo.save(newShopOrder);
        }
        return tempShopOrder;
    }

    // BASIC method
    public boolean deleteShopOrderById (int shopOrderId){
        boolean control = false;
        if (shopOrderRepo.existsById(shopOrderId)){
            shopOrderRepo.deleteById(shopOrderId);
            control = true;
        }
        return control;
    }

    // BASIC method -- Be aware not no update orderingDateTime
    public ShopOrder updateShopOrder (ShopOrder updateShopOrder){
        ShopOrder tempShopOrder = null;
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findById(updateShopOrder.getId());
        if (foundShopOrder.isPresent()){
            updateShopOrder.setOrderingDateTime(foundShopOrder.get().getOrderingDateTime());
            tempShopOrder = shopOrderRepo.save(updateShopOrder);
        }
        return tempShopOrder;
    }

}
