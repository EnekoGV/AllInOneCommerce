package com.telcreat.aio.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.telcreat.aio.model.*;
import com.telcreat.aio.repo.ItemRepo;
import com.telcreat.aio.model.itemDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Double.parseDouble;

@Service
public class ItemService {

    private final VariantService variantService;
    private final ItemRepo itemRepo;
    private final GeoIPLocationService locationService;

    @Autowired
    public ItemService(ItemRepo itemRepo, VariantService variantService, GeoIPLocationService locationService){
        this.itemRepo = itemRepo;
        this.variantService = variantService;
        this.locationService = locationService;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //BM - findAllItems ---> Returns a List of all Items
    public List<Item> findAllItems(){
        return itemRepo.findAll();
    }

        //BM - findItemById ---> Returns the Item or a null object if not found
    public Item findItemById(int itemId){
        Item item = null;
        Optional<Item> foundItem = itemRepo.findById(itemId);
        if(foundItem.isPresent()){
            item = foundItem.get();
        }
        return item;
    }

        //BM - createItem ---> Returns New Item if found and Null if not found
    public Item createItem(Item newItem){
        Item tempItem = null;
        if (!itemRepo.existsById(newItem.getId())){
            tempItem = itemRepo.save(newItem);
        }
        return tempItem;
    }

        //BM - updateItem ---> Returns Updated Item if found and Null if not found
    public Item updateItem(Item item){
        Item tempItem = null;
        if (itemRepo.existsById(item.getId())){
            tempItem = itemRepo.save(item);
        }
        return tempItem;
    }

    //BM - deleteItemById ---> Returns TRUE if deleted and FALSE if not
    public boolean deleteItemById(int itemId){
        boolean control = false;
        if (itemRepo.existsById(itemId)){
            itemRepo.deleteById(itemId);
            control = true;
        }
        return control;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                            ADVANCED METHODS                            //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //AM - findItemsByShopId ---> Returns a List of Items that matches the Shop ID
    public List<Item> findItemsByShopId(int shopId){
        return itemRepo.findItemsByShop_Id(shopId);
    }

        //AM - findActiveItemsByShopId ---> Returns a List of Active Items that matches the ShopId
    public List<Item> findActiveItemsByShopId(int ShopId){
        List<Item> items = findItemsByShopId(ShopId);
        List<Item> activeItems = null;
        if (items != null){
            for (Item item:items){
                if(item.getStatus()== Item.Status.ACTIVE){
                    activeItems.add(item);
                }
            }
        }
        return activeItems;
    }

        //AM - findItemsContainsNameOrdered ---> Returns the list of Item matching the searching
        //criteria (words, order and category)
            // orderCriteriaId = 0) Price 1) Distance
            // itemCategoryId = 0) Dummy 1)... and next are real categories.
    public List<itemDistance> findItemsContainsNameOrdered(String itemName, int orderCriteriaId, int itemCategoryId, String ip) throws IOException, GeoIp2Exception {
        List<Item> items = findItemsContainsName(itemName,itemCategoryId);
        List<itemDistance> orderedItems;
        if(orderCriteriaId==0){
            items.sort(Comparator.comparingDouble(Item::getPrice));
            orderedItems = getItemDistance(items,ip);
            /*for(int i=0;i<(items.size()-1);i++){
                for(int j=i+1;j<items.size();j++){
                    if(items.get(i).getPrice()>items.get(j).getPrice()){
                        float aux=items.get(i).getPrice();
                        items.get(i).setPrice(items.get(j).getPrice());
                        items.get(j).setPrice(aux);
                    }
                }
            }*/
        }else{
            orderedItems = getItemDistance(items,ip);
            orderedItems.sort(Comparator.comparingDouble(itemDistance::getDistance));
        }
        return orderedItems;
    }

        //PRIVATE AM - getItemDistance ---> Returns a List of itemDistance which came with the item object and its distance.
    private List<itemDistance> getItemDistance(List<Item> items, String ip) throws IOException, GeoIp2Exception {
        GeoIP location = locationService.getLocation(ip);
        itemDistance itemDistance = null;
        List<itemDistance> itemsDistances = null;
        for (Item item:items){
            double dist = locationService.distance(parseDouble(location.getLatitude()), parseDouble(location.getLongitude()), parseDouble(item.getShop().getLatitude()), parseDouble(item.getShop().getLongitude()));
            itemDistance.setItem(item);
            itemDistance.setDistance(dist);
            itemsDistances.add(itemDistance);
        }
        return itemsDistances;
    }

        //AM - findItemsContainsName ---> Returns a List of items that matches the searching criteria.
    public List<Item> findItemsContainsName(String itemName, int itemCategoryId){
        return itemRepo.findItemsByItemCategory_IdAndNameIsContaining(itemCategoryId,itemName);
    }

        //AM - deactivateItem ---> Returns a TRUE if the item is been deactivated and a FALSE if not.
    public boolean deactivateItem(int itemId){
        boolean control = false; // Control variable
        Item tempItem; // Temp variable
        Optional<Item> foundItem = itemRepo.findById(itemId);
        if(foundItem.isPresent() && foundItem.get().getStatus() == Item.Status.ACTIVE){ // Check if Item exists in DB and is ACTIVE
            tempItem = foundItem.get();
            List<Variant> variants = tempItem.getVariants();
            for (Variant variant:variants) {
                variantService.deactivateVariant(variant.getId());
            }
            tempItem.setStatus(Item.Status.INACTIVE);
            itemRepo.save(tempItem);
            control = true;
        }
        return control;
    }
}
