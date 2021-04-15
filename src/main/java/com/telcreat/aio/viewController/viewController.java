package com.telcreat.aio.viewController;

import com.telcreat.aio.model.User;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class viewController {

    private final CartService cartService;
    private final ItemService itemService;
    private final PictureService pictureService;
    private final ShopOrderService shopOrderService;
    private final UserService userService;
    private final VariantService variantService;
    private final CategoryService categoryService;


    @Autowired
    public viewController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService) {
        this.cartService = cartService;
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.shopOrderService = shopOrderService;
        this.userService = userService;
        this.variantService = variantService;
        this.categoryService = categoryService;
    }

    //Register and Login page

    @RequestMapping(value = "/auth" , method = RequestMethod.GET)
    public String registerView(@RequestParam(name = "registrationError", required = false, defaultValue = "false") boolean registrationError,
                               @RequestParam(name = "loginError", required = false, defaultValue = "false") boolean loginError, // Control param for login error
                               ModelMap modelMap){

        User login = new User();
        User signup = new User();
        modelMap.addAttribute("login", login);
        modelMap.addAttribute("signup", signup);

        // Error control params
        modelMap.addAttribute("loginError", loginError); // Control param to display error message
        modelMap.addAttribute("registrationError", registrationError); // Control param to display error message

        return "auth";
    }

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public String receiveRegister(@ModelAttribute User user, ModelMap modelMap){
        if(userService.signUpUser(user) != null)
            modelMap.clear();
        else
            return "redirect:/auth?registrationError=true";
        return "redirect:/auth";
    }


    // Search View
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String searchView(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                           @RequestParam(name = "orderCriteriaId", required = false, defaultValue = "0") Integer orderCriteriaId,
                           @RequestParam(name = "search", required = false, defaultValue = "") String itemName,
                           ModelMap modelMap){

        // Item Search - Item List based on Category and Name search
        modelMap.addAttribute("itemSearch", itemService.findItemsContainsNameOrdered(itemName, orderCriteriaId, categoryId));
        modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch

        // DISPLAY LOGGED IN USER'S NAME
        modelMap.addAttribute("loggedUser", userService.getLoggedUser().getName());

        // SHOP LIST IS PENDING


        return "search"; // Return Search search.html view
    }
}
