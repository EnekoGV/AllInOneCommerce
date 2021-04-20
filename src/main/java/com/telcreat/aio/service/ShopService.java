package com.telcreat.aio.service;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.telcreat.aio.model.GeoIP;
import com.telcreat.aio.model.Item;
import com.telcreat.aio.model.Shop;
import com.telcreat.aio.repo.ShopRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.Double.parseDouble;

@Service
public class ShopService {

    private final ShopRepo shopRepo;

    private ItemService itemService;

    @Autowired
    public ShopService (ShopRepo shopRepo){
        this.shopRepo = shopRepo;
    }

    // BASIC METHODS (BM)-----------------------------------------------------------------------------------------------
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

    // ESPECIAL METHODS (EM)--------------------------------------------------------------------------------------------
    // EM - findActiveShopById
    public Shop findActiveShopById(int shopId){
        Shop tempShop = findShopById(shopId);
        if(tempShop != null){
            if(tempShop.getStatus() == Shop.Status.INACTIVE){
                tempShop = null;
            }
        }
        return tempShop;
    }

    // EM - Get shop by user Id
    public Shop findShopByUserId(int userId){
        Shop tempShop = null;
        if(shopRepo.existsByOwnerId(userId)) {
            Optional<Shop> foundShop = shopRepo.findShopsByOwnerId(userId);
            if (foundShop.isPresent()){
                tempShop = foundShop.get();
            }
        }
        return tempShop;
    }

    // EM - Find active shop using userId
    public Shop findActiveShopByUserId(int userId){
        Shop tempShop = findShopByUserId(userId);
        if(tempShop != null){
            if(tempShop.getStatus() == Shop.Status.INACTIVE){
                tempShop = null;
            }
        }
        return tempShop;
    }

    //EM - Deactivate a Shop and all the items on it.
    public boolean deactivateShop(Shop shop){
        boolean ctrl = false;
        if(shopRepo.existsById(shop.getId())){
            shop.setStatus(Shop.Status.INACTIVE);
            shopRepo.save(shop);
            ctrl = true;
            List<Item> shopItems = itemService.findItemsByShopId(shop.getId());
            for (Item item:shopItems){
                boolean itemDown = true;
                itemDown = itemService.deactivateItem(item);
                if (itemDown == false){
                    ctrl = itemDown;
                }
            }
        }
        return ctrl;
    }

    // EM - Find shops by ItemContainsName an order them based on distance.
    public List<Shop> orderedShopByItemContainsName(String searchName, String ip, int itemCategoryId) throws IOException, GeoIp2Exception {
        List<Shop> shops = findShopsByItemContainsName(searchName, itemCategoryId);
        List<Shop> orderedShops = orderShops(shops, ip);
        return orderedShops;
    }

    // EM - findShopsByItemContainsName
    private List<Shop> findShopsByItemContainsName(String serchName, int itemCategoryId){
        List<Item> items = itemService.findItemsContainsName(serchName, itemCategoryId);
        List<Shop> shops = null;
        for(Item item:items) {
            if (!shops.contains(item.getShop())) {
                shops.add(item.getShop());
            }
        }
        return shops;
    }

    /*
    public List<Shop> orderShops(List<Shop> shops, String ip) throws IOException, GeoIp2Exception {
        List<Shop> tempshops = null;
        RawDBDemoGeoIPLocationService locationService = new RawDBDemoGeoIPLocationService();
        GeoIP location = locationService.getLocation(ip);
        List<Double> distances = null;
        for (Shop shop:shops){
            distances.add(distance(parseDouble(location.getLatitude()), parseDouble(location.getLongitude()), parseDouble(shop.getLatitude()), parseDouble(shop.getLongitude())));
            tempshops.add(shop);
        }
        Collections.sort(distances);
        return tempshops;
    }
*/
    private List<Shop> orderShops(List<Shop> shops, String ip) throws IOException, GeoIp2Exception {
        List<Shop> tempshops = null;
        RawDBDemoGeoIPLocationService locationService = new RawDBDemoGeoIPLocationService();
        GeoIP location = locationService.getLocation(ip);
        List<Double> distances = null;
        for (Shop shop:shops){
            double dist = distance(parseDouble(location.getLatitude()), parseDouble(location.getLongitude()), parseDouble(shop.getLatitude()), parseDouble(shop.getLongitude()));
            distances.add(dist);
            Collections.sort(distances);

            //ordenar las tiendas en funcion de la distancia, Hablar con aresti
            tempshops.add(shop);
        }
        Collections.sort(distances);

        return tempshops;
    }




    // Auxiliar methods used for calculating the distance between two latitude and longitude
    //
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
