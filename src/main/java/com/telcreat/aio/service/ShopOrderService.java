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

    //
    // ADVANCED method -- Create ShopOrder from incoming cart
    public List<ShopOrder> createShopOrderFromCart(Cart cart){
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

            variantRepo.saveAll(updateVariantList); // Update Variant Stock in DB. Check if this method is possible.

            for(int i=0; i<cart.getVariants().size(); i++){
                tempVariantShops.add(cart.getVariants().get(i).getItem().getShop()); // Get Shops from VariantList
            }

            uniqueShopList = new ArrayList<>(new HashSet<>(tempVariantShops)); // Get unique Shops to create an order for each

            for (Shop tempShop : uniqueShopList){
                ShopOrder tempShopOrder = new ShopOrder(tempShop, cart.getUser(), new ArrayList<Variant>(), 0, ShopOrder.ShopOrderStatus.PENDING);
                for(int i=0; i<cart.getVariants().size(); i++){

                }
                shopOrders.add(tempShopOrder);
            }

        }

        return shopOrders;
    }
}
