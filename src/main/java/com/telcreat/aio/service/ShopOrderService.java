package com.telcreat.aio.service;

import com.telcreat.aio.model.Cart;
import com.telcreat.aio.model.Shop;
import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.ShopOrderRepo;
import com.telcreat.aio.repo.VariantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShopOrderService {

    private final ShopOrderRepo shopOrderRepo;
    private final VariantRepo variantRepo;

    @Autowired
    public ShopOrderService(ShopOrderRepo shopOrderRepo, VariantRepo variantRepo) {
        this.shopOrderRepo = shopOrderRepo;
        this.variantRepo = variantRepo;
    }


    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //BM - findAllShopOrders ---> Returns a List of ShopOrders
    public List<ShopOrder> findAllShopOrders (){
        return shopOrderRepo.findAll();
    }

        //BM - findShopOrderById ---> Returns the ShopOrder or a null object if not found
    public ShopOrder findShopOrderById (int shopOrderId){
        ShopOrder tempShopOrder = null;
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findById(shopOrderId);
        if (foundShopOrder.isPresent()){
            tempShopOrder = foundShopOrder.get();
        }
        return tempShopOrder;
    }

        //BM - createShopOrder ---> Returns New ShopOrder if found and Null if not found
    public ShopOrder createShopOrder (ShopOrder newShopOrder){
        ShopOrder tempShopOrder = null;
        if (!shopOrderRepo.existsById(newShopOrder.getId())){
            tempShopOrder = shopOrderRepo.save(newShopOrder);
        }
        return tempShopOrder;
    }

        //BM - updateShopOrder ---> Returns Updated ShopOrder if found and Null if not found
    public ShopOrder updateShopOrder (ShopOrder updateShopOrder){
        ShopOrder tempShopOrder = null;
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findById(updateShopOrder.getId());
        if (foundShopOrder.isPresent()){
            updateShopOrder.setOrderingDateTime(foundShopOrder.get().getOrderingDateTime());
            tempShopOrder = shopOrderRepo.save(updateShopOrder);
        }
        return tempShopOrder;
    }

        //BM - deleteShopOrderById ---> Returns TRUE if deleted and FALSE if not
    public boolean deleteShopOrderById (int shopOrderId){
        boolean control = false;
        if (shopOrderRepo.existsById(shopOrderId)){
            shopOrderRepo.deleteById(shopOrderId);
            control = true;
        }
        return control;
    }

    //________________________________________________________________________________________________________________//
                        /////////////////////////////////////////////////////////////////////////////
                        //                            ADVANCED METHODS                            //
                        ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //AM - createShopOrderFromCart ---> Returns a List of ShopOrder build from incoming cart
    /*public List<ShopOrder> createShopOrderFromCart(Cart cart){
        ArrayList<Shop> tempVariantShops = new ArrayList<>();
        ArrayList<Variant> updateVariantList = new ArrayList<>();
        Variant tempVariant;
        boolean control = true; // Control variable
        int queryStock; // Dummy Variable for each Variant Stock
        ArrayList<ShopOrder> shopOrders = null;
        ArrayList<Shop> uniqueShopList;
        ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants())); // Get unique variants

        for (int i = 0; i < uniqueVariantList.size() && control; i++){
            tempVariant = uniqueVariantList.get(i);
            queryStock = Collections.frequency(cart.getVariants(), tempVariant); // Get repetitions of variant in list
            Optional<Variant> foundVariant = variantRepo.findById(tempVariant.getId());

            if (foundVariant.isPresent() && foundVariant.get().getStock() >= queryStock) { // If variant exists and there is enough stock
                tempVariant = foundVariant.get();
                tempVariant.setStock(tempVariant.getStock()-queryStock); // Update Variant Stock locally
                updateVariantList.add(tempVariant);
            } else {
                control = false; // Exit if there is any incomplete condition.
            }
        }

        if (control){ // If there is enough stock of all variants
            shopOrders = new ArrayList<>();
            for(int i=0; i<cart.getVariants().size(); i++){
                //tempVariantShops.add(cart.getVariants().get(i).getItem().getShop()); // Get Shops from VariantList
                tempVariantShops.add(findItemcart.getVariants().get(i).getItem().getShop()); // Get Shops from VariantList
            }

            uniqueShopList = new ArrayList<>(new HashSet<>(tempVariantShops)); // Get unique Shops to create an order for each

            for (Shop tempShop : uniqueShopList){
                ArrayList<Variant> newVariantList = new ArrayList<>();
                float orderPrice = 0;
                ShopOrder tempShopOrder = new ShopOrder(tempShop, cart.getUser(), new ArrayList<Variant>(), 0, ShopOrder.ShopOrderStatus.PENDING);
                for(int i=0; i<cart.getVariants().size(); i++){
                    /*if (cart.getVariants().get(i).getItem().getShop() == tempShop){
                        orderPrice = orderPrice + cart.getVariants().get(i).getItem().getPrice();
                        newVariantList.add(cart.getVariants().get(i));
                    }*//*
                }
                tempShopOrder.setPrice(orderPrice);
                tempShopOrder.setVariants(newVariantList);
                shopOrders.add(tempShopOrder);
            }
            variantRepo.saveAll(updateVariantList); // Update Variant Stock in DB. Check if this method is possible.
            shopOrderRepo.saveAll(shopOrders); // Create an order for each shop from the Cart.
        }
        return shopOrders;
    }*/
}
