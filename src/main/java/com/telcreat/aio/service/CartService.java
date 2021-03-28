package com.telcreat.aio.service;

import com.telcreat.aio.model.Cart;
import com.telcreat.aio.repo.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepo cartRepo;
    private int code = 23;

    @Autowired//Para que lo cree solo, culpas a aresti
    public CartService(CartRepo cartRepo) {
        this.cartRepo = cartRepo;
    }

    public List<Cart> findAllCart(){
        return cartRepo.findAll();
    }

    public Cart findCartById(int id){
        Cart cart = null;
        Optional<Cart> opt=cartRepo.findById(id);
        if(opt.isPresent()) {
            cart = opt.get();
            code = 0;
        }else{
            code = 1;
        }
        return cart;
    }

}
