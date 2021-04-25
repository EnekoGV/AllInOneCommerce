package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Cart;
import com.telcreat.aio.model.User;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestScope
public class cartController {
    private final ItemService itemService;
    private final PictureService pictureService;
    private final UserService userService;
    private final ShopService shopService;
    private final CartService cartService;

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
        if (this.loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER)
                loggedCartId = this.cartService.findCartByUserId(loggedUser.getId()).getId();
        }
    }

    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String viewCart(@RequestParam(name = "cartId")int cartId,
                           @RequestParam(name = "userId")int userId,
                           ModelMap modelMap){
        Cart cart = cartService.findCartById(cartId);
        if (isLogged && loggedId == userId && cart.getUser().getId() == loggedId){ // Allow editing only each user's profile.

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
}
