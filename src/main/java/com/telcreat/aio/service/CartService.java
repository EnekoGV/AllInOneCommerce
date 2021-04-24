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
        if(checkStock(variant)){
            variants.add(variant);
        }
        cart.setVariants(variants);
        return cart;
    }

        //AM - checkStock ---> Returns TRUE if there is enough stock for a concrete variant and FALSE if not.
    public boolean checkStock(Variant variant){
        return variant.getStock() > 0;
    }

        //AM - checkOutCart ---> Returns TRUE if there is enough stock in all variants and FALSE if not.
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
