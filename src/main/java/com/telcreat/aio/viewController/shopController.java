package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
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

        loggedUser = userService.getLoggedUser();
        if (loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER)
                loggedShopId = this.shopService.findShopByOwnerId(loggedId).getId();
        }
    }

    // Create Shop - There is no view here. Directly redirected to edition page.
    @RequestMapping(value = "/shop/create", method = RequestMethod.GET)
    public String createShop(ModelMap modelMap){

        if(isLogged && loggedRole == User.UserRole.CLIENT){ // Only allowed when you are CLIENT and logged
            Picture newPicture = new Picture("");
            Picture savedPicture = pictureService.createPicture(newPicture); // Create picture in DB
            Picture newBackPicture = new Picture("");
            Picture savedBackPicture = pictureService.createPicture(newBackPicture); // Create picture in DB
            Shop newShop = new Shop(null, savedPicture, savedBackPicture, loggedUser, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", Shop.Status.ACTIVE);
            Shop savedShop = shopService.createShop(newShop); // Create Shop in DB

            modelMap.addAttribute("shop", savedShop);//Se mandan a la siguiente vista siendo redirect??

            // Update User role
            loggedUser.setUserRole(User.UserRole.OWNER);
            userService.updateUser(loggedUser);

            //noinspection SpringMVCViewInspection
            return "redirect:/shop/edit?shopId=" + newShop.getId() + "&edit=true"; // After creation go to Shop Edit page
        }else
            return "redirect:/?createShopError"; // Error creating new shop: not logged or is already owner
    }

    // Edit Shop View
    @RequestMapping(value ="/shop/edit", method = RequestMethod.GET)
    public String viewAndEditShop(@RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
                                  @RequestParam(name = "shopId") int shopId,
                                  @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                  ModelMap modelMap){

        Shop shop = shopService.findActiveShopById(shopId);

        if(isLogged && shop != null && loggedId == shop.getOwner().getId()){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("loggedShopId", loggedShopId);

            modelMap.addAttribute("shopForm", new ShopEditForm(shop.getId(),
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

            // Send shop picture paths to HTML view
            modelMap.addAttribute("shopPicture", shop.getPicture().getPath());
            modelMap.addAttribute("shopBackgroundPicture", shop.getBackgroundPicture().getPath());

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

        if(isLogged && shop != null && shop.getOwner().getId() == loggedId){

            shop.setAddressCity(shopEditForm.getAddressCity());
            shop.setAddressCountry(shopEditForm.getAddressCountry());
            shop.setAddressAddress(shopEditForm.getAddressAddress());
            shop.setAddressName(shopEditForm.getAddressName());
            shop.setAddressSurname(shopEditForm.getAddressSurname());
            shop.setAddressPostNumber(shopEditForm.getAddressPostNumber());
            shop.setAddressTelNumber(shopEditForm.getAddressTelNumber());
            shop.setBillingAddress(shopEditForm.getBillingAddress());
            shop.setName(shopEditForm.getName());
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

    // Shop Page View - Public

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

    @RequestMapping(value = "/shop/edit/products", method = RequestMethod.GET)
    public String viewShopProducts(@RequestParam(name = "shopId") int shopId,
                                   ModelMap modelMap){

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("loggedShopId", loggedShopId);

        Shop shop = shopService.findActiveShopById(shopId);

        if (isLogged && shop != null && loggedId == shop.getOwner().getId()){ // Security check
            modelMap.addAttribute("shop", shop); // Send shop object
            modelMap.addAttribute("itemList", itemService.findActiveItemsByShopId(shop.getId())); // Send item list
            return "shopProducts";
        }
        else{
            return "error/error-404";
        }
    }

    @RequestMapping(value = "/item", method = RequestMethod.GET)
    public String viewItem(@RequestParam(name = "itemId") int itemId,
                           ModelMap modelMap){

        Item item = itemService.findActiveItemById(itemId);

        if (item != null){
            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("loggedShopId", loggedShopId);

            modelMap.addAttribute("item", item);

            return "item"; // Return Item view
        }
        else{
            return "redirect:/?itemNotFound";
        }
    }


    @RequestMapping(value = "/shop/uploadPicture", method = RequestMethod.POST)
    public String uploadUserPicture(@RequestParam(name = "shopPicture") MultipartFile file,
                                    @RequestParam(name = "shopId") int shopId,
                                    @RequestParam(name = "type") int type,
                                    ModelMap modelMap){

        Shop shop = shopService.findActiveShopById(shopId);
        String dir = "";
        Picture shopPicture = new Picture();

        if (isLogged && shop != null && loggedShopId == shopId && (type == 0 || type == 1)){ // Security check - Verify logged user

            if (type == 0) // Principal image
                dir = "/shop";
            else if (type == 1) // Background image
                dir = "/shopBackground";

            String imagePath = fileUploaderService.uploadUserPicture(file, shopId, dir); // Upload image to server filesystem

            if(imagePath != null){ // Security check - Besides, will always be not null
                if (type == 0){ // Principal image
                    shopPicture = shop.getPicture();
                }
                else if (type == 1){ // Background image
                    shopPicture = shop.getBackgroundPicture();
                }
                shopPicture.setPath(imagePath);
                pictureService.updatePicture(shopPicture); // Update Object
                modelMap.clear();
                return "redirect:/shop?shopId=" + shop.getId(); // Return to User View
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/shop?shopId=" + shop.getId()+ "&updateError=true"; // Redirect if imagePath is null
            }

        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + shopId + "&updateError=true"; // Redirect if not allowed
        }

    }



}
