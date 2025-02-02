package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;

@Controller
@RequestScope
@SessionAttributes({"searchForm", "categories", "cartItemNumber"})

public class cartController {
    private final CartService cartService;
    private final ShopOrderService shopOrderService;
    private final VariantService variantService;
    private final ShopService shopService;
    private final CategoryService categoryService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner = false;
    private int loggedCartId;

    @Autowired
    public cartController(CartService cartService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, ShopService shopService, CategoryService categoryService) {
        this.cartService = cartService;

        this.loggedUser = userService.getLoggedUser();
        this.shopOrderService = shopOrderService;
        this.variantService = variantService;
        loggedUser = userService.getLoggedUser();
        this.shopService = shopService;
        if (loggedUser != null){
            loggedCartId = cartService.findCartByUserId(loggedUser.getId()).getId();
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER){
                isOwner = true;
            }
        }
        this.categoryService = categoryService;
    }

    @ModelAttribute("searchForm")
    public SearchForm setUpSearchForm(){
        return new SearchForm();
    }

    @ModelAttribute("categories")
    public List<Category> setUpSearchCategories(){
        return categoryService.findAllCategories();
    }

    @ModelAttribute("cartItemNumber")
    public int updateCartItemNumber(){
        if (isLogged){
            Cart cart = cartService.findCartByUserId(loggedId);
            List<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants()));
            return uniqueVariantList.size();
        }
        else{
            return 0;
        }
    }



    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String viewCart(@RequestParam(name = "userId")int userId,
                           @RequestParam(name = "stockOverflow", required = false, defaultValue = "false")boolean stockOverflow,
                           ModelMap modelMap){
        Cart cart = cartService.findCartByUserId(userId);
        if (cart != null && isLogged && loggedId == userId && cart.getUser().getId() == loggedId){ // Allow editing only each user's profile.

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop loggedShop = shopService.findActiveShopByOwnerId(loggedId);
            if (loggedShop != null){
                modelMap.addAttribute("loggedShopId", loggedShop.getId());
            }
            modelMap.addAttribute("loggedCartId",loggedCartId);

            modelMap.addAttribute("stockOverflow", stockOverflow);
            List<CartQuantity> cartVariantsAndQuantities = new ArrayList<>();
            ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants()));
            Collections.sort(uniqueVariantList);
            Variant tempVariant;
            int quantity;

            for (int i=0; i<uniqueVariantList.size(); i++) {
                CartQuantity cartQuantity = new CartQuantity();
                tempVariant = uniqueVariantList.get(i);
                quantity = Collections.frequency(cart.getVariants(), tempVariant);
                cartQuantity.setQuantity(quantity);
                cartQuantity.setVariant(tempVariant);
                cartVariantsAndQuantities.add(cartQuantity);
            }
            int totalPrice = 0;
            for(int i=0 ;i<cartVariantsAndQuantities.size(); i++){
                totalPrice += cartVariantsAndQuantities.get(i).getVariant().getItem().getPrice() * cartVariantsAndQuantities.get(i).getQuantity();
            }
            modelMap.addAttribute("totalPrice", totalPrice);
            modelMap.addAttribute("variantsAndQuantities", cartVariantsAndQuantities);

            return "cart";
        }else{
            return "redirect:/auth";
        }
    }

    @RequestMapping(value = "/cart/delete", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam(name = "variantId")int variantId,
                                 @RequestParam(name = "cartId")int cartId,
                                 ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            cart.getVariants().removeIf(n -> (n.getId() == variantId));
            cartService.updateCart(cart);
            modelMap.put("cartItemNumber", updateCartItemNumber());
            return "redirect:/cart?userId="+loggedUser.getId();
        }else
            return "redirect:/";
    }

    /*@RequestMapping(value = "/cart/increase", method = RequestMethod.POST)
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
    }*/
    @RequestMapping(value = "/cart/update", method = RequestMethod.POST)
    public String updateItemQuantity(@RequestParam(name = "variantId")int variantId,
                                     @RequestParam(name = "cartId")int cartId,
                                     @RequestParam(name = "quantity") int newQuantity,
                                     ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(cart.getVariants()));
            Collections.sort(uniqueVariantList);
            Variant tempVariant;
            tempVariant = variantService.findVariantById(variantId);
            int quantity = Collections.frequency(cart.getVariants(), tempVariant);
            if(newQuantity > quantity){
                for(int i = 0; i< (newQuantity-quantity); i++){
                    cart.getVariants().add(tempVariant);
                }
            }else if(newQuantity < quantity){
                for(int i = 0; i< (quantity-newQuantity); i++){
                    cart.getVariants().remove(tempVariant);
                }
            }

            cartService.updateCart(cart);
            modelMap.put("cartItemNumber", updateCartItemNumber());
            return "redirect:/cart?userId="+loggedUser.getId();
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/addToCart", method = RequestMethod.POST)
    public String addToCart(@RequestParam(name = "cartId")int cartId,
                            @RequestParam(name = "variantId")int variantId,
                            ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        Variant variant = variantService.findActiveVariantById(variantId);
        if(variant != null && cart != null && cart.getUser().getId() == loggedId){
            if(variant.getStock() == Collections.frequency(cart.getVariants(),variant))
                return "redirect:/cart?userId="+loggedId+"&stockOverflow=true";
            cartService.addToCart(cart,variant);
            modelMap.put("cartItemNumber", updateCartItemNumber());
            return "redirect:/cart?userId="+loggedId;
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/createOrder", method = RequestMethod.POST)
    public String createOrder(@ModelAttribute(name = "cart")int cartId,
                              @RequestParam(name = "userId")int userId,
                              ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == userId && loggedId == userId){
            if (cart.getVariants().size() != 0){
                List<ShopOrder> shopOrders= shopOrderService.createShopOrderFromCart(cart);
                if(shopOrders == null) {
                    cart.setVariants(new ArrayList<>());
                    cartService.updateCart(cart);
                    modelMap.put("cartItemNumber", updateCartItemNumber());
                    return "redirect:/?NotEnoughStock";
                }else {
                    cart.setVariants(new ArrayList<>());
                    cartService.updateCart(cart);
                    modelMap.put("cartItemNumber", updateCartItemNumber());
                    return "redirect:/user/myOrders?userId=" + userId;
                }
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/cart?userId=" + userId + "&emptyCart=true";
            }

        }else
            return "redirect:/?notAllowed";
    }

}
