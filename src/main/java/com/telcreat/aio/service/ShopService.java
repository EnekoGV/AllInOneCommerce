package com.telcreat.aio.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.telcreat.aio.model.*;
import com.telcreat.aio.repo.ShopRepo;
import com.telcreat.aio.repo.UserRepo;
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
    private final UserRepo userRepo;

    @Autowired
    public ShopService(ShopRepo shopRepo, GeoIPLocationService locationService, ItemService itemService, UserRepo userRepo){
        this.shopRepo = shopRepo;
        this.locationService = locationService;
        this.itemService = itemService;
        this.userRepo = userRepo;
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
        Shop tempShop = null;
        Optional<Shop> foundShop = shopRepo.findShopByIdAndStatus(shopId, Shop.Status.ACTIVE);
        if (foundShop.isPresent()){
            tempShop = foundShop.get();
        }
        return tempShop;
    }

        // AM - findShopByOwnerId ---> Returns the Shop Object according to the specified UserId.
    public Shop findShopByOwnerId(int ownerId){
        Shop tempShop = null;
        Optional<Shop> foundShop = shopRepo.findShopByOwnerId(ownerId);
        if (foundShop.isPresent()){
            tempShop = foundShop.get();
        }
        return tempShop;
    }

        // AM - findActiveShopByOwnerId ---> Returns the Shop Object according to the specified UserId if the
        // Shop is Active.
    public Shop findActiveShopByOwnerId(int ownerId){
        Shop tempShop = null;
        Optional<Shop> foundShop = shopRepo.findShopByOwnerIdAndStatus(ownerId, Shop.Status.ACTIVE);
        if (foundShop.isPresent()){
            tempShop = foundShop.get();
        }
        return tempShop;
    }

        //AM - deactivateShop ---> Returns a true boolean if the shop is been deactivated successfully.
    public boolean deactivateShop(int shopId){
        boolean control = false;
        Shop tempShop;
        Optional<Shop> foundShop = shopRepo.findShopByIdAndStatus(shopId, Shop.Status.ACTIVE);
        if(foundShop.isPresent()){
            tempShop = foundShop.get();
            List<Item> shopItems = itemService.findItemsByShopId(tempShop.getId());
            for (Item item:shopItems){
                itemService.deactivateItem(item.getId());
            }
            tempShop.setStatus(Shop.Status.INACTIVE);
            User owner = tempShop.getOwner();
            owner.setUserRole(User.UserRole.CLIENT);

            userRepo.save(owner);
            shopRepo.save(tempShop);
            control = true;
        }
        return control;
    }

        // AM - orderedShopsByItemContainsName ---> Returns a list of Shops matching the searching criteria of the user
        // and ordered base on the distance.
    public List<ShopDistance> orderedShopByItemContainsName(String searchName, int itemCategoryId, String ip) throws IOException, GeoIp2Exception {
        List<Shop> shops = findShopsByItemContainsName(searchName, itemCategoryId);
        List<ShopDistance> orderShops = getShopDistance(shops,ip);
        orderShops.sort(Comparator.comparing(ShopDistance::getDistance));
        return orderShops;
    }

        // AM - findShopsByItemContainsName ---> Returns a list of Shops matching the searching criteria of the user.
    public List<Shop> findShopsByItemContainsName(String serchName, int itemCategoryId){
        List<Item> items = itemService.findItemsContainsName(serchName, itemCategoryId);
        List<Shop> shops = new ArrayList<>();
        for(Item item:items) {
            if (!shops.contains(item.getShop()) && item.getShop().getStatus()==Shop.Status.ACTIVE) {
                shops.add(item.getShop());
            }
        }
        return shops;
    }

        // AM - getShopDistance ---> Returns a list of shopDistance with object and its distance.
    public List<ShopDistance> getShopDistance(List<Shop> shops, String ip) throws IOException, GeoIp2Exception {
        //GeoIPLocationService locationService = new GeoIPLocationService();
        GeoIP location = locationService.getLocation(ip);
        List<ShopDistance> shopsDistances = new ArrayList<>();
        for (Shop shop:shops){
            ShopDistance shopDistance = new ShopDistance();
            double dist = locationService.distance(parseDouble(location.getLatitude()), parseDouble(location.getLongitude()), parseDouble(shop.getLatitude()), parseDouble(shop.getLongitude()));
            shopDistance.setShop(shop);
            shopDistance.setDistance(dist);
            shopsDistances.add(shopDistance);
        }
        return shopsDistances;
    }

    public List<Shop> findShopsByStatus(Shop.Status status){
        return shopRepo.findShopsByStatus(status);
    }
}
