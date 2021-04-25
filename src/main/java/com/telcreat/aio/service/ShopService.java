package com.telcreat.aio.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.telcreat.aio.model.*;
import com.telcreat.aio.repo.ShopRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static java.lang.Double.parseDouble;

@Service
public class ShopService {

    private final ShopRepo shopRepo;
    private final GeoIPLocationService locationService;
    private final ItemService itemService;

    @Autowired
    public ShopService (ShopRepo shopRepo, GeoIPLocationService locationService, ItemService itemService){
        this.shopRepo = shopRepo;
        this.locationService = locationService;
        this.itemService = itemService;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        // BM - Find All Shops ---> Returns a list of all the shops of the DataBase.
    public List<Shop> findAllShops(){
        return shopRepo.findAll();
    }

        // BM - Find Shops by Id ---> Returns the Shop Object according to the specified ShopId.
    public Shop findShopById(int shopId){
        Shop tempShop = null;
        Optional<Shop> foundShop = shopRepo.findById(shopId);
        if(foundShop.isPresent()){
            tempShop = foundShop.get();
        }
        return tempShop;
    }

        // BM - Create a new shop ---> Returns the Shop Object according to the specified ShopId.
    public Shop createShop(Shop newShop){
        Shop tempShop = null;
        if(!shopRepo.existsById(newShop.getId())){
            tempShop = shopRepo.save(newShop);
        }
        return tempShop;
    }

        // BM - Update a Shop ---> Returns the Shop Object that has been updated.
    public Shop updateShop(Shop shop){
        Shop tempShop = null;
        if(shopRepo.existsById(shop.getId())){
            tempShop = shopRepo.save(shop);
        }
        return tempShop;
    }

        // BM - Delete Shop by ID ---> Returns a true if the object has been deleted successfully.
    public boolean deleteShopById(int shopId){
        boolean control = false;
        if(shopRepo.existsById(shopId)){
            shopRepo.deleteById(shopId);
            control = true;
        }
        return control;
    }
    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                            ADVANCED METHODS                            //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        // AM - findActiveShopById ---> Returns the Shop Object according to the specified ShopId if the Shop is Active.
    public Shop findActiveShopById(int shopId){
        Shop tempShop = findShopById(shopId);
        if(tempShop != null){
            if(tempShop.getStatus() == Shop.Status.INACTIVE){
                tempShop = null;
            }
        }
        return tempShop;
    }

        // AM - findShopByOwnerId ---> Returns the Shop Object according to the specified UserId.
    public Shop findShopByOwnerId(int userId){
        Shop tempShop = null;
        if(shopRepo.existsByOwnerId(userId)) {
            Optional<Shop> foundShop = shopRepo.findShopByOwnerId(userId);
            if (foundShop.isPresent()){
                tempShop = foundShop.get();
            }
        }
        return tempShop;
    }

        // AM - findActiveShopByOwnerId ---> Returns the Shop Object according to the specified UserId if the
        // Shop is Active.
    public Shop findActiveShopByOwnerId(int userId){
        Shop tempShop = findShopByOwnerId(userId);
        if(tempShop != null){
            if(tempShop.getStatus() == Shop.Status.INACTIVE){
                tempShop = null;
            }
        }
        return tempShop;
    }

        //AM - deactivateShop ---> Returns a true boolean if the shop is been deactivated successfully.
    public boolean deactivateShop(int shopId){
        boolean control = false;
        Shop tempShop;
        Optional<Shop> foundShop = shopRepo.findById(shopId);
        if(foundShop.isPresent() && foundShop.get().getStatus() == Shop.Status.ACTIVE){
            tempShop = foundShop.get();
            List<Item> shopItems = itemService.findItemsByShopId(tempShop.getId());
            for (Item item:shopItems){
                itemService.deactivateItem(item.getId());
            }
            tempShop.setStatus(Shop.Status.INACTIVE);
            User owner = tempShop.getOwner();
            owner.setUserRole(User.UserRole.CLIENT);
            shopRepo.save(tempShop);
            control = true;
        }
        return control;
    }

        // AM - orderedShopsByItemContainsName ---> Returns a list of Shops matching the searching criteria of the user
        // and ordered base on the distance.
    public List<ShopDistance> orderedShopByItemContainsName(String searchName, String ip, int itemCategoryId) throws IOException, GeoIp2Exception {
        List<Shop> shops = findShopsByItemContainsName(searchName, itemCategoryId);
        List<ShopDistance> orderShops = getShopDistance(shops,ip);
        orderShops.sort(Comparator.comparing(ShopDistance::getDistance));
        return orderShops;
    }

        // AM - findShopsByItemContainsName ---> Returns a list of Shops matching the searching criteria of the user.
    private List<Shop> findShopsByItemContainsName(String serchName, int itemCategoryId){
        List<Item> items = itemService.findItemsContainsName(serchName, itemCategoryId);
        List<Shop> shops = new ArrayList<>();
        for(Item item:items) {
            if (!shops.contains(item.getShop())) {
                shops.add(item.getShop());
            }
        }
        return shops;
    }

        // AM - orderShops ---> Returns a list of Shops ordered based on the distance.
    /*private List<shopDistance> orderShops(List<Shop> shops, String ip) throws IOException, GeoIp2Exception {
        List<shopDistance> orderShops = getShopDistance(shops,ip);
        orderShops.sort(Comparator.comparing(shopDistance::getDistance));
        return orderShops;
    }*/

    private List<ShopDistance> getShopDistance(List<Shop> shops, String ip) throws IOException, GeoIp2Exception {
        //GeoIPLocationService locationService = new GeoIPLocationService();
        GeoIP location = locationService.getLocation(ip);
        ShopDistance shopDistance = new ShopDistance();
        List<ShopDistance> shopsDistances = new ArrayList<>();
        for (Shop shop:shops){
            double dist = locationService.distance(parseDouble(location.getLatitude()), parseDouble(location.getLongitude()), parseDouble(shop.getLatitude()), parseDouble(shop.getLongitude()));
            shopDistance.setShop(shop);
            shopDistance.setDistance(dist);
            shopsDistances.add(shopDistance);
        }
        return shopsDistances;
    }
}
