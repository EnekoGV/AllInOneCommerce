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
    public List<ShopOrder> createShopOrderFromCart(Cart cart){
        ArrayList<Shop> tempVariantShops = new ArrayList<>();
        ArrayList<Variant> updateVariantList = new ArrayList<>();
        Variant tempVariant;
        boolean control = true; // Control variable
        int askedVariantQuantity; // Dummy Variable for each Variant Stock
        ArrayList<ShopOrder> shopOrders = null;
        ArrayList<Shop> uniqueShopList;
        ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants())); // Get unique variants

        // For each variant in cart, obtain asked variant quantity and check if there is enough stock.
        for (int i = 0; i < uniqueVariantList.size() && control; i++){
            tempVariant = uniqueVariantList.get(i);
            askedVariantQuantity = Collections.frequency(cart.getVariants(), tempVariant); // Get repetitions of variant in list
            Optional<Variant> foundVariant = variantRepo.findById(tempVariant.getId());

            if (foundVariant.isPresent() && foundVariant.get().getStock() >= askedVariantQuantity) { // If variant exists and there is enough stock
                tempVariant = foundVariant.get(); // Obtain variant from DB
                tempVariant.setStock(tempVariant.getStock()-askedVariantQuantity); // Update Variant Stock locally
                updateVariantList.add(tempVariant); // Add variant to local list, which will contain all variants that must be updated.
            } else {
                control = false; // Exit if there is any incomplete condition. No variant will be updated.
            }
        }

        if (control){ // If there is enough stock of all variants
            shopOrders = new ArrayList<>();
            for(int i=0; i<uniqueVariantList.size(); i++){ // From unique Variant List (from cart)...
                tempVariantShops.add(uniqueVariantList.get(i).getItem().getShop()); // ... get Shop from each Variant
            }
            uniqueShopList = new ArrayList<>(new HashSet<>(tempVariantShops)); // Get unique Shops to create an order for each

            // For each unique Shop (from cart)...
            for (Shop tempShop : uniqueShopList){
                ArrayList<Variant> shopVariantList = new ArrayList<>();
                float orderPrice = 0;
                ShopOrder tempShopOrder = new ShopOrder(tempShop, cart.getUser(), new ArrayList<>(), 0, ShopOrder.ShopOrderStatus.BAIEZTATZEKE);
                for(int i=0; i<cart.getVariants().size(); i++){
                    if (cart.getVariants().get(i).getItem().getShop() == tempShop){ // Does current Variant belong to current Shop?
                        orderPrice = orderPrice + cart.getVariants().get(i).getItem().getPrice(); // Update order price locally
                        shopVariantList.add(cart.getVariants().get(i));
                    }
                }
                tempShopOrder.setPrice(orderPrice); // Set order price
                tempShopOrder.setVariants(shopVariantList); // Set shop order's variant list
                shopOrders.add(tempShopOrder); // Add to order list

                // Send verification emails to Shop Owners
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendNewOrderNotificationToOwner(tempShopOrder);
            }
            variantRepo.saveAll(updateVariantList); // Update Variant Stock in DB. Check if this method is possible.
            shopOrderRepo.saveAll(shopOrders); // Create an order for each shop from the Cart.
        }
        return shopOrders;
    }

    //AM - findPendingShopOrderById ---> Returns Active Order with status PENDING
    public ShopOrder findPendingShopOrderById(int shopOrderId){
        ShopOrder tempShopOrder = null;
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findShopOrderByIdAndShopOrderStatus(shopOrderId, ShopOrder.ShopOrderStatus.BAIEZTATZEKE);
        if (foundShopOrder.isPresent()){
            tempShopOrder = foundShopOrder.get();
        }
        return tempShopOrder;
    }

    //AM - findNotCanceledNotDeliveredShopOrderById
    public ShopOrder findNotCanceledNotDeliveredShopOrderById(int shopOrderId){
        ShopOrder tempShopOrder = null;
        // Optional<ShopOrder> foundShopOrder = shopOrderRepo.findShopOrderByIdAndShopOrderStatusNotOrShopOrderStatusNot(shopOrderId, ShopOrder.ShopOrderStatus.CANCELLED, ShopOrder.ShopOrderStatus.DELIVERED);
        Optional<ShopOrder> foundShopOrder = shopOrderRepo.findById(shopOrderId);
        if (foundShopOrder.isPresent() && (foundShopOrder.get().getShopOrderStatus() != ShopOrder.ShopOrderStatus.EZEZTATUTA && foundShopOrder.get().getShopOrderStatus() != ShopOrder.ShopOrderStatus.ENTREGATUTA)){
            tempShopOrder = foundShopOrder.get();
        }
        return tempShopOrder;
    }

    //AM - findShopOderByUserId
    public List<ShopOrder> findShopOrdersByUserId(int userId){
        return shopOrderRepo.findShopOrdersByUser_Id(userId);
    }

    //AM - findShopOrderByShopId
    public List<ShopOrder> findShopOrdersByShopId(int shopId){
        return shopOrderRepo.findShopOrdersByShop_Id(shopId);
    }

}
