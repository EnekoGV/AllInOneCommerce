package com.telcreat.aio.service;

import com.telcreat.aio.model.Cart;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepo cartRepo;
    private int code = 23;

    @Autowired
    public CartService(CartRepo cartRepo) {
        this.cartRepo = cartRepo;
    }

    // Basic method
    public List<Cart> findAllCart(){
        return cartRepo.findAll();
    }

    // Basic method
    public Cart findCartById(int cartId){

        Cart cart = null;
        Optional<Cart> opt=cartRepo.findById(cartId);
        if(opt.isPresent()) {
            cart = opt.get();
            code = 0;
        }else{
            code = 1;
        }
        return cart;
    }

    // Basic method - Create New Cart
    public Cart createCart(Cart newCart){
        Cart tempCart = null;
        if (!cartRepo.existsById(newCart.getId())){
            tempCart = cartRepo.save(newCart);
        }
        return tempCart;
    }

    // Basic method - Update Cart
    public Cart updateCart(Cart cart){
        Cart tempCart = null;
        if (cartRepo.existsById(cart.getId())){
            tempCart = cartRepo.save(cart);
        }
        return tempCart;
    }

    // Basic method - Delete Cart
    public boolean deleteCartById(int cartId){
        boolean control = false;
        if (cartRepo.existsById(cartId)){
            cartRepo.deleteById(cartId);
            control = true;
        }
        return control;
    }

    public Cart addToCart(Cart cart, Variant variant){

        List<Variant> variants = cart.getVariants();
        if(checkStock(variant)){
            variants.add(variant);
        }
        cart.setVariants(variants);
        return cart;

    }

    public boolean checkStock(Variant variant){

        boolean control = false;
        if (variant.getStock()>0){
            control = true;
        }
        return control;
    }

    // Devuelve false si no hay stock de algun item
    public boolean checkOutCart(Cart cart){

        boolean control = true;

        List<Variant> variants = cart.getVariants();
        for(Variant variant:variants){
            if(!checkStock(variant)){
                control = false;
            }
        }
        return control;
    }

}
