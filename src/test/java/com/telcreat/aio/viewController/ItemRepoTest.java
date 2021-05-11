package com.telcreat.aio.viewController;


import com.telcreat.aio.model.*;
import com.telcreat.aio.repo.ItemRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRepoTest {
    @Autowired private TestEntityManager testEntityManager;
    @Autowired private ItemRepo itemRepo;

    @Test
    public void saveShop() {
        Picture picture1 = new Picture("path");
        Picture picture2 = new Picture("path2");
        Picture picture3 = new Picture("path3");
        User user = new User("testUser", "user", "test", LocalDate.now(), "emailTest@aio.com", "1234",picture1, "addressStreet", "addressNumber", "addressFlat", "addressDoor", "addressCountry", 48014, "addressCity", "Region", new ArrayList<Shop>());
        Shop shop = new Shop(new ArrayList<Category>(),picture2, picture3, user, "testShop", "this is a test", "address", "postNumber", "city", "country", "telNumber", "bilingName", "billingSurname", "billingAddress", "billingPostNumber", "billingCity", "billingCountry", "billingTelNumber", "longitude", "latitude",Shop.Status.ACTIVE);
        testEntityManager.persistAndFlush(picture1);
        testEntityManager.persistAndFlush(picture2);
        testEntityManager.persistAndFlush(picture3);
        testEntityManager.persistAndFlush(user);
        testEntityManager.persistAndFlush(shop);
        assertThat(shop.getId()).isNotNull();
    }
    @Test
    public void deleteShop() {
        Picture picture1 = new Picture("path");
        Picture picture2 = new Picture("path2");
        Picture picture3 = new Picture("path3");
        Picture picture4 = new Picture("path4");
        Picture picture5 = new Picture("path5");
        Picture picture6 = new Picture("path6");
        User user = new User("testUser", "user", "test", LocalDate.now(), "emailTest@aio.com", "1234",picture1, "addressStreet", "addressNumber", "addressFlat", "addressDoor", "addressCountry", 48014, "addressCity", "Region", new ArrayList<Shop>());
        User user2 = new User("testUser2", "user2", "test2", LocalDate.now(), "emailTest2@aio.com", "1234",picture2, "addressStreet", "addressNumber", "addressFlat", "addressDoor", "addressCountry", 48014, "addressCity", "Region", new ArrayList<Shop>());
        testEntityManager.persistAndFlush(picture1);
        testEntityManager.persistAndFlush(picture2);
        testEntityManager.persistAndFlush(picture3);
        testEntityManager.persistAndFlush(picture4);
        testEntityManager.persistAndFlush(picture5);
        testEntityManager.persistAndFlush(picture6);
        testEntityManager.persistAndFlush(user);
        testEntityManager.persistAndFlush(user2);
        testEntityManager.persistAndFlush(new Shop(new ArrayList<Category>(),picture3, picture4, user, "testShop", "this is a test", "address", "postNumber", "city", "country", "telNumber", "bilingName", "billingSurname", "billingAddress", "billingPostNumber", "billingCity", "billingCountry", "billingTelNumber", "longitude", "latitude",Shop.Status.ACTIVE));
        testEntityManager.persistAndFlush(new Shop(new ArrayList<Category>(),picture5, picture6, user2, "testShop2", "this is a test2", "address2", "postNumber2", "city2", "country2", "telNumber2", "bilingName2", "billingSurname2", "billingAddress2", "billingPostNumber2", "billingCity2", "billingCountry2", "billingTelNumber2", "longitude2", "latitude2",Shop.Status.ACTIVE));
        itemRepo.deleteAll();
        assertThat(itemRepo.findAll()).isEmpty();
    }
}

