package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Picture;
import com.telcreat.aio.model.Shop;
import com.telcreat.aio.model.ShopEditForm;
import com.telcreat.aio.model.User;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;

@Data
@Controller
@RequestScope
public class shopController {

    private final ItemService itemService;
    private final PictureService pictureService;
    private final UserService userService;
    private final ShopService shopService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private int loggedShopId;

    @Autowired
    public shopController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request) {
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.userService = userService;
        this.shopService = shopService;

        this.loggedUser = userService.getLoggedUser();
        if (this.loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER)
                loggedShopId = this.shopService.findShopByOwnerId(loggedId).getId();
        }
    }

    //Create, view and edit Shop

    @RequestMapping(value = "/shop/create", method = RequestMethod.GET)
    public String createShop(ModelMap modelMap){

        Shop newShop;
        if(loggedUser != null && loggedUser.getUserRole() == User.UserRole.CLIENT){
            Picture shopPicture = new Picture("");
            shopPicture = pictureService.createPicture(shopPicture);
            Picture shopBackPicture = new Picture("");
            shopBackPicture = pictureService.createPicture(shopBackPicture);
            newShop = new Shop(null, shopPicture, shopBackPicture, loggedUser, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", Shop.Status.ACTIVE);
            newShop = shopService.createShop(newShop);
            modelMap.addAttribute("shop", newShop);//Se mandan a la siguiente vista siendo redirect??
            loggedUser.setUserRole(User.UserRole.OWNER);
            userService.updateUser(loggedUser);

            //noinspection SpringMVCViewInspection
            return "redirect:/shop/edit?shopId=" + newShop.getId() + "&edit=true"; // After creation go to Shop Edit page
        }else
            return "redirect:/?createShopError"; // Error creating new shop: not logged or is already owner
    }

    @RequestMapping(value ="/shop/edit", method = RequestMethod.GET)
    public String viewAndEditShop(@RequestParam(name = "edit",required = false, defaultValue = "false")boolean edit,
                                  @RequestParam(name = "shopId") int shopId,
                                  @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                  ModelMap modelMap){

        Shop shop = shopService.findActiveShopById(shopId);

        if(loggedUser != null && loggedUser.getId() == shop.getOwner().getId()){
            modelMap.addAttribute("shopForm", new ShopEditForm(shop.getId(),
                    shop.getPicture(),
                    shop.getBackgroundPicture(),
                    shop.getName(),
                    shop.getDescription(),
                    shop.getAddressName(),
                    shop.getAddressSurname(),
                    shop.getAddressAddress(),
                    shop.getAddressPostNumber(),
                    shop.getAddressCity(),
                    shop.getAddressCountry(),
                    shop.getAddressTelNumber(),
                    shop.getBillingName(),
                    shop.getBillingSurname(),
                    shop.getBillingAddress(),
                    shop.getBillingPostNumber(),
                    shop.getBillingCity(),
                    shop.getBillingCountry(),
                    shop.getBillingTelNumber()));

            modelMap.addAttribute("edit", edit);
            modelMap.addAttribute("updateError", updateError);

            return "editShop";

        }else{
            return "redirect:/?updateShopError";
        }

    }

    @RequestMapping(value ="/shop/edit", method = RequestMethod.POST)
    public String updateShopProfile(@ModelAttribute(name = "shopForm") ShopEditForm shopEditForm,
                                    ModelMap modelMap){

        modelMap.clear();
        Shop shop = shopService.findActiveShopById(shopEditForm.getId());

        if(loggedUser != null && shop.getOwner().getId() == loggedUser.getId()){

            shop.setAddressCity(shopEditForm.getAddressCity());
            shop.setAddressCountry(shopEditForm.getAddressCountry());
            shop.setAddressAddress(shopEditForm.getAddressAddress());
            shop.setAddressName(shopEditForm.getAddressName());
            shop.setAddressSurname(shopEditForm.getAddressSurname());
            shop.setAddressPostNumber(shopEditForm.getAddressPostNumber());
            shop.setAddressTelNumber(shopEditForm.getAddressTelNumber());
            shop.setBillingAddress(shopEditForm.getBillingAddress());
            shop.setName(shopEditForm.getName());
            shop.setPicture(shopEditForm.getPicture());
            shop.setBackgroundPicture(shopEditForm.getBackgroundPicture());
            shop.setBillingCity(shopEditForm.getBillingCity());
            shop.setBillingCountry(shopEditForm.getBillingCountry());
            shop.setBillingName(shopEditForm.getBillingName());
            shop.setBillingPostNumber(shopEditForm.getBillingPostNumber());
            shop.setBillingAddress(shopEditForm.getBillingAddress());
            shop.setBillingSurname(shopEditForm.getBillingSurname());
            shop.setBillingTelNumber(shopEditForm.getBillingTelNumber());
            shop.setDescription(shopEditForm.getDescription());

            Shop savedShop = shopService.updateShop(shop);

            if(savedShop != null)
                return "redirect:/shop/?shopId=" + shop.getId();
            else
                //noinspection SpringMVCViewInspection
                return "redirect:/shop/edit?shopId=" + shop.getId() + "&updateError=true";
        }else
            //noinspection SpringMVCViewInspection
            return "redirect:/shop?shopId=" + shop.getId() + "&updateError=true";
    }

    // Shop Page View

    @RequestMapping(value = "/shop", method = RequestMethod.GET)
    public String viewShop(@RequestParam(name = "shopId") int shopId,
                           ModelMap modelMap){

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("loggedShopId", loggedShopId);

        Shop shop = shopService.findActiveShopById(shopId); // Obtain queried shop
        if (shop != null){ // If shop exists
            modelMap.addAttribute("shop", shop); // Send shop object
            modelMap.addAttribute("itemList", itemService.findActiveItemsByShopId(shop.getId())); // Send item list
            return "shop";
        }
        else{
            return "error/error-404";
        }
    }

    // Shop Products View - Only accessible for owner
    // WARNING - NOT FINISHED!

    @RequestMapping(value = "/shop/products", method = RequestMethod.GET)
    public String viewShopProducts(@RequestParam(name = "shopId") int shopId,
                                   ModelMap modelMap){

        Shop shop = shopService.findActiveShopById(shopId);

        if (loggedUser != null && shop != null && loggedUser.getId() == shop.getOwner().getId()){ // Security check
            modelMap.addAttribute("shop", shop); // Send shop object
            modelMap.addAttribute("itemList", itemService.findActiveItemsByShopId(shop.getId())); // Send item list
            return "shopProducts";
        }
        else{
            return "error/error-404";
        }
    }

}
