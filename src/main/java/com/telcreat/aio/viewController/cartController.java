package com.telcreat.aio.viewController;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.telcreat.aio.model.Cart;
import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.User;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
@RequestScope
public class cartController {
    private final ItemService itemService;
    private final PictureService pictureService;
    private final UserService userService;
    private final ShopService shopService;
    private final CartService cartService;
    private final ShopOrderService shopOrderService;
    private final VariantService variantService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private int loggedCartId;

    @Autowired
    public cartController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request) {
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.userService = userService;
        this.shopService = shopService;
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

           modelMap.addAttribute("cart", cart);

            return "cart";
        }else{
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/cart/eliminate", method = RequestMethod.POST)
    public String eliminateCartItem(@RequestParam(name = "variantId")int variantId,
                                       @RequestParam(name = "cartId")int cartId){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            for(int i=0; i<cart.getVariants().size(); i++){
                if(variantId == cart.getVariants().get(i).getId())
                    cart.getVariants().remove(i);
            }
            cartService.updateCart(cart);
            return "redirect:/cart";
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/add", method = RequestMethod.POST)
    public String addItem(@RequestParam(name = "variantId")int variantId,
                          @RequestParam(name = "cartId")int cartId){
        Cart cart = cartService.findCartById(cartId);
        if(cart != null && cart.getUser().getId() == loggedUser.getId()){
            cart.getVariants().add(variantService.findVariantById(variantId));
            cartService.updateCart(cart);
            return "redirect:/cart";
        }else
            return "redirect:/";
    }

    @RequestMapping(value = "/cart/order", method = RequestMethod.POST)
    public String createOrder(@ModelAttribute(name = "cart")Cart cart,
                              @RequestParam(name = "userId")int userId){
        if(cart != null && cart.getUser().getId() == userId && loggedId == userId){
            List<ShopOrder> shopOrders= shopOrderService.createShopOrderFromCart(cart);
            if(shopOrders == null)
                return "redirect:/";
            else
                return "redirect:/cart/order";
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
