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

    @RequestMapping(value = "/register" , method = RequestMethod.GET)
    public String register(ModelMap modelMap){

        User login = new User();
        User signup = new User();
        modelMap.addAttribute("login", login);
        modelMap.addAttribute("signup", signup);

        return "register";
    }

    @RequestMapping(value = "/register/send", method = RequestMethod.POST)//Cuando se usa POST no podemos enviar el html sinmas porque ya estas usando la URL para mandar la info y si no usas redirect esa URL no cambia.
    public String recieveLogin(@ModelAttribute User user, ModelMap modelMap){
        //Login Service
        return "redirect:/";
    }

    @RequestMapping(value = "/register/register", method = RequestMethod.POST)
    public String reciveRegister(@ModelAttribute User user, ModelMap modelMap){
        if(userService.createUser(user) != null)
            modelMap.clear();
        else
            return "redirect:/fail";
        return "redirect:/";
    }


    // HomePage View
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String homePage(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                           @RequestParam(name = "search", required = false, defaultValue = "") String itemName,
                           ModelMap modelMap){
        // Debug
        //modelMap.addAttribute("categoryId", categoryId);
        //modelMap.addAttribute("itemName", itemName);

        // Item Search - Item List based on Category and Name search
        modelMap.addAttribute("itemSearch", itemService.getItemsContainsName(itemName, categoryId));
        modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch

        // SHOP LIST IS PENDING


        return "index"; // Return HomePage index.html view
    }
}
