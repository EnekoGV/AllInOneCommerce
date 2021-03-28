package com.telcreat.aio.service;

import com.telcreat.aio.model.Shop;
import com.telcreat.aio.repo.ShopRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopService {

    private final ShopRepo shopRepo;

    @Autowired
    public ShopService (ShopRepo shopRepo){
        this.shopRepo = shopRepo;
    }

    // BASIC METHODS (BM)
    // BM - Find All Shops
    public List<Shop> findAllShops(){
        return shopRepo.findAll();
    }

    // BM - Find Shops by Id
    public Shop findShopById(int shopId){
        Shop tempShop = null;
        Optional<Shop> foundShop = shopRepo.findById(shopId);
        if(foundShop.isPresent()){
            tempShop = foundShop.get();
        }
        return tempShop;
    }

    // BM - Create a new shop
    public Shop createShop(Shop newShop){
        Shop tempShop = null;
        if(!shopRepo.existsById(newShop.getId())){
            tempShop = newShop;
        }
        return tempShop;
    }

    // BM - Update a Shop
    public Shop updateShop(Shop shop){
        Shop tempShop = null;
        if(shopRepo.existsById(shop.getId())){
            tempShop = shopRepo.save(shop);
        }
        return tempShop;
    }

    //BM - Delete Shop by ID
    public boolean deleteShopById(int shopId){
        boolean control = false;
        if(shopRepo.existsById(shopId)){
            shopRepo.deleteById(shopId);
            control = true;
        }
        return control;
    }


}
