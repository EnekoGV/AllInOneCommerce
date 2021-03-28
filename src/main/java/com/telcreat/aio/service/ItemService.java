package com.telcreat.aio.service;

import com.telcreat.aio.repo.ItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepo itemRepo;
    private int code = 23;

    @Autowired
    public ItemService(ItemRepo itemRepo){
        this.itemRepo = itemRepo;
    }



}
