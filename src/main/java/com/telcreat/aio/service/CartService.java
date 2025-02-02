package com.telcreat.aio.service;

import com.telcreat.aio.model.Cart;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepo cartRepo;

    @Autowired
    public CartService(CartRepo cartRepo) {
        this.cartRepo = cartRepo;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//


        //BM - findAllCarts ---> Returns a List of all Carts
    public List<Cart> findAllCarts(){
        return cartRepo.findAll();
    }

        //BM - findCartById ---> Returns a cart object or a null object if not found
    public Cart findCartById(int cartId){
        Cart cart = null;
        Optional<Cart> foundcart = cartRepo.findById(cartId);
        if(foundcart.isPresent()) {
            cart = foundcart.get();
        }
        return cart;
    }

        //BM - createCart ---> Returns new Cart if created or null if not
    public Cart createCart(Cart newCart){
        Cart tempCart = null;
        if (!cartRepo.existsById(newCart.getId())){
            tempCart = cartRepo.save(newCart);
        }
        return tempCart;
    }

        //BM - updateCart ---> Returns updated Cart if ok or null if not found
    public Cart updateCart(Cart cart){
        Cart tempCart = null;
        if (cartRepo.existsById(cart.getId())){
            tempCart = cartRepo.save(cart);
        }
        return tempCart;
    }

        //BM - deleteCartById ---> Returns TRUE if deleted or FALSE if not
    public boolean deleteCartById(int cartId){
        boolean control = false;
        if (cartRepo.existsById(cartId)){
            cartRepo.deleteById(cartId);
            control = true;
        }
        return control;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                            ADVANCED METHODS                            //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //AM - addToCart ---> Returns the Cart object updated.
    public Cart addToCart(Cart cart, Variant variant){
        List<Variant> variants = cart.getVariants();
        int quantity = Collections.frequency(cart.getVariants(),variant);
        if(quantity < variant.getStock()){
            variants.add(variant);
        }
        cart.setVariants(variants);
        updateCart(cart);
        return cart;
    }


       //AM - findCartByUserId ---> Returns the found cart by the related user Id
    public Cart findCartByUserId(int userId){
        Cart cart = null;
        Optional<Cart> foundCart = cartRepo.findCartByUserId(userId);
        if(foundCart.isPresent()){
            cart = foundCart.get();
        }
        return cart;
    }
}
