package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.CartService;
import com.telcreat.aio.service.ShopOrderService;
import com.telcreat.aio.service.UserService;
import com.telcreat.aio.service.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestScope
public class cartController {
    private final CartService cartService;
    private final ShopOrderService shopOrderService;
    private final VariantService variantService;

    private final User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private int loggedCartId;

    @Autowired
    public cartController(CartService cartService, ShopOrderService shopOrderService, UserService userService, VariantService variantService) {
        this.cartService = cartService;

        this.loggedUser = userService.getLoggedUser();
        this.shopOrderService = shopOrderService;
        this.variantService = variantService;
        if (this.loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER)
                loggedCartId = this.cartService.findCartByUserId(loggedUser.getId()).getId();
        }
    }

    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String viewCart(@RequestParam(name = "userId")int userId,
                           ModelMap modelMap){
        Cart cart = cartService.findCartByUserId(userId);
        if (cart != null && isLogged && loggedId == userId && cart.getUser().getId() == loggedId){ // Allow editing only each user's profile.

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("loggedCartId", loggedCartId);

            List<CartQuantity> cartVariantsAndQuantities = new ArrayList<>();
            ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants()));
            Variant tempVariant;
            int quantity;
            CartQuantity cartQuantity = new CartQuantity();

            for (Variant variant : uniqueVariantList) {
                tempVariant = variant;
                quantity = Collections.frequency(cart.getVariants(), tempVariant);
                cartQuantity.setQuantity(quantity);
                cartQuantity.setVariant(tempVariant);
                cartVariantsAndQuantities.add(cartQuantity);
            }
           modelMap.addAttribute("variantsAndQuantities", cartVariantsAndQuantities);

            return "cart";
        }else{
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/cart/delete", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam(name = "variantId")int variantId,
                                       @RequestParam(name = "cartId")int cartId){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            cart.getVariants().removeIf(n -> (n.getId() == variantId));
            cartService.updateCart(cart);
            return "redirect:/cart?userId="+loggedUser.getId();
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/increase", method = RequestMethod.POST)
    public String increaseItem(@RequestParam(name = "variantId")int variantId,
                          @RequestParam(name = "cartId")int cartId){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            cart.getVariants().add(variantService.findVariantById(variantId));
            cartService.updateCart(cart);
            return "redirect:/cart?userId="+loggedUser.getId();
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/decrease", method = RequestMethod.POST)
    public String decreaseItem(@RequestParam(name = "variantId")int variantId,
                          @RequestParam(name = "cartId")int cartId){
        Cart cart = cartService.findCartById(cartId);
        boolean control = true;
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            for(int i = 0; i<cart.getVariants().size() && control; i++){
                if(variantId == cart.getVariants().get(i).getId()) {
                    cart.getVariants().remove(i);
                    cartService.updateCart(cart);
                    control = false;
                }
            }
            return "redirect:/cart?userId="+loggedUser.getId();
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/addToCart", method = RequestMethod.POST)
    public String addToCart(@RequestParam(name = "cartId")int cartId,
                            @RequestParam(name = "variantId")int variantId,
                            @RequestParam(name = "userId")int userId,
                            ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        Variant variant = variantService.findActiveVariantById(variantId);
        if(variant != null && cart != null && cart.getUser().getId() == userId){
            cartService.addToCart(cart,variant);
            return "redirect:/cart?userId="+userId;
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/order", method = RequestMethod.POST)
    public String createOrder(@ModelAttribute(name = "cart")int cartId,
                              @RequestParam(name = "userId")int userId){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == userId && loggedId == userId && cart.getVariants().size() != 0){
            List<ShopOrder> shopOrders= shopOrderService.createShopOrderFromCart(cart);
            if(shopOrders == null)
                return "redirect:/";
            else {
                cart.setVariants(new ArrayList<>());
                cartService.updateCart(cart);
                return "redirect:/cart?userId=" + userId;
            }
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/order", method = RequestMethod.GET)
    public String viewOrders(@RequestParam(name = "userId")int userId,
                             ModelMap modelMap){
        List<ShopOrder> shopOrders = shopOrderService.findShopOrdersByUserId(userId);
        if (shopOrders != null && isLogged && loggedId == userId){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("loggedCartId", loggedCartId);

            modelMap.addAttribute("ordersList", shopOrders);
            return "orders";
        }else{
            return "redirect:/";
        }

    }
}
