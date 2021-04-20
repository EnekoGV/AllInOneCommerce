package com.telcreat.aio.service;

import com.telcreat.aio.model.Item;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.ItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final VariantService variantService;
    private final ItemRepo itemRepo;
    private int code = 23;

    @Autowired
    public ItemService(ItemRepo itemRepo, VariantService variantService){
        this.itemRepo = itemRepo;
        this.variantService = variantService;
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
        Optional<Item> opt = itemRepo.findById(itemId);
        if(opt.isPresent()){
            item = opt.get();
            code = 0;
        }else{
            code = 1;
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

        //AM - findItemsByShopId --->
    public List<Item> findItemsByShopId(int shopId){
        return itemRepo.findItemsByShop_Id(shopId);
    }

        //AM - findActiveItemsByShopId ---> Find Active Items By Shop ID
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
            // orderCriteriaId = 0 -> PRECIO
            // orderCriteriaId = 1 -> DISTANCIA
            // itemCategoryId = 0 -> DUMMY VARIABLE SOLO EN FRONTEND. Primera categoría empieza por ID=1.
    public List<Item> findItemsContainsNameOrdered(String itemName, int orderCriteriaId, int itemCategoryId){
        List<Item> items = findItemsContainsName(itemName,itemCategoryId);
        if(orderCriteriaId==0){
            for(int i=0;i<(items.size()-1);i++){
                for(int j=i+1;j<items.size();j++){
                    if(items.get(i).getPrice()>items.get(j).getPrice()){
                        float aux=items.get(i).getPrice();
                        items.get(i).setPrice(items.get(j).getPrice());
                        items.get(j).setPrice(aux);
                    }
                }
            }
        }else{

            /// ORDENAR PRODUCTOS POR DISTANCIA

        }
        return items;
    }

        //AM - findItemsContainsName --->
    public List<Item> findItemsContainsName(String itemName, int itemCategoryId){
        return itemRepo.findItemsByItemCategory_IdAndNameIsContaining(itemCategoryId,itemName);
    }

        //AM - deactivateItem --->
    public boolean deactivateItem(Item item){
        boolean ctrl = false;
        Item itemTemp = null;
        if(itemRepo.existsById(item.getId()) && item.getStatus().toString().equals("ACTIVE")){
            itemTemp = item;
            List<Variant> variants = variantService.findVariantsByItemId(itemTemp.getId());
            for (Variant variant:variants) {
                variantService.deactivateVariant(variant);
            }
            itemTemp.setStatus(Item.Status.valueOf("INACTIVE"));
            itemRepo.save(itemTemp);
            ctrl = true;
        }
        return ctrl;
    }
}
