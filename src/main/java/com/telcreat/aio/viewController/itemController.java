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
import org.springframework.web.multipart.MultipartFile;

@Data
@RequestScope
@Controller
public class itemController {

    private final ItemService itemService;
    private final PictureService pictureService;
    private final UserService userService;
    private final ShopService shopService;
    private final FileUploaderService fileUploaderService;
    private final VariantService variantService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner = false;

    @Autowired
    public itemController(ItemService itemService, PictureService pictureService, UserService userService, FileUploaderService fileUploaderService, ShopService shopService, VariantService variantService) {
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.userService = userService;
        this.shopService = shopService;

        loggedUser = userService.getLoggedUser();
        if (loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER){
                isOwner = true;
            }
        }
        this.fileUploaderService = fileUploaderService;
        this.variantService = variantService;
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
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

            modelMap.addAttribute("item", item);
            modelMap.addAttribute("variantList", variantService.findActiveVariantsByItemId(item.getId()));

            modelMap.addAttribute("newVariant", new Variant());

            return "item";
        }
        else{
            return "redirect:/?itemNotFound";
        }
    }

    @RequestMapping(value = "/item/edit/create", method = RequestMethod.GET)
    public String createItem(@RequestParam(name = "shopId") int shopId,
                             ModelMap modelMap){

        Shop shop = shopService.findActiveShopById(shopId);

        if (isLogged && shop != null && loggedId == shop.getOwner().getId()){
            Picture newItemPicture = new Picture("/images/Item.png");
            Picture savedItemPicture = pictureService.createPicture(newItemPicture);
            Item newItem = new Item(shop, null, savedItemPicture, "", "", 0.0f, "", Item.Status.ACTIVE);
            Item savedItem = itemService.createItem(newItem);
            Picture newVariantPicture = new Picture("/images/Item.png");
            Picture savedVariantPicture = pictureService.createPicture(newVariantPicture);
            Variant defaultVariant = new Variant("Default variant", 0, savedVariantPicture,savedItem, Variant.Status.ACTIVE);
            variantService.createVariant(defaultVariant);

            return "redirect:/item/edit?itemId=" + savedItem.getId();
        }
        else{
            return "redirect:/?errorCreatingItem";
        }
    }

    @RequestMapping(value = "/item/edit", method = RequestMethod.GET)
    public String viewEditItem(@RequestParam(name = "itemId") int itemId,
                               @RequestParam(name = "editVariantNumber", required = false, defaultValue = "0") int editVariantNumber,
                               @RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
                               @RequestParam(name = "itemUpdateError", required = false, defaultValue = "false") boolean itemUpdateError,
                               @RequestParam(name = "variantUpdateError", required = false, defaultValue = "false") boolean variantUpdateError,
                               @RequestParam(name = "variantDeleteError", required = false, defaultValue = "false") boolean variantDeleteError,
                               @RequestParam(name = "itemDeleteError", required = false, defaultValue = "false") boolean itemDeleteError,
                               ModelMap modelMap){

        Item item = itemService.findActiveItemById(itemId);

        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

            ItemEditForm itemForm = new ItemEditForm(item.getId(),
                    item.getShortDescription(),
                    item.getLongDescription(),
                    item.getPrice(),
                    item.getName());

            modelMap.addAttribute("itemForm", itemForm);
            modelMap.addAttribute("item", item);

            modelMap.addAttribute("variantList", variantService.findActiveVariantsByItemId(item.getId()));

            modelMap.addAttribute("editVariantNumber", editVariantNumber);
            modelMap.addAttribute("edit", edit);
            modelMap.addAttribute("itemUpdateError", itemUpdateError);
            modelMap.addAttribute("variantUpdateError", variantUpdateError);
            modelMap.addAttribute("variantDeleteError", variantDeleteError);
            modelMap.addAttribute("itemDeleteError", itemDeleteError);

            modelMap.addAttribute("variantForm", new Variant());

            return "addProduct";
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/item/edit", method = RequestMethod.POST)
    public String receiveEditItem(@ModelAttribute(name = "item") Item itemForm,
                                  ModelMap modelMap){

        modelMap.clear();

        Item item = itemService.findActiveItemById(itemForm.getId());

        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){
            Item savedItem = itemService.updateItem(itemForm);
            if (savedItem != null){
                return "redirect:/item/edit?itemId=" + item.getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&itemUpdateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/item/edit/delete", method = RequestMethod.POST)
    public String deactivateItem(@RequestParam(name = "itemId") int itemId,
                                 ModelMap modelMap){
        Item item = itemService.findActiveItemById(itemId);
        boolean control;
        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){
            control = itemService.deactivateItem(item.getId());
            if (control){
                return "redirect:/shop?shopId=" + item.getShop().getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&variantDeleteError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/item/edit/uploadPicture", method = RequestMethod.POST)
    public String uploadItemPicture(@RequestParam(name = "itemPicture") MultipartFile file,
                                    @RequestParam(name = "itemId") int itemId,
                                    ModelMap modelMap){

        Item item = itemService.findActiveItemById(itemId);

        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){ // Security check - Verify logged user

            String imagePath = fileUploaderService.uploadUserPicture(file, itemId, "/item"); // Upload image to server filesystem

            if(imagePath != null){ // Security check - Besides, will always be not null
                Picture itemPicture = item.getPicture(); // Obtain Picture object
                itemPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(itemPicture); // Update Object
                modelMap.clear();

                return "redirect:/item?itemId=" + item.getId(); // Return to User View
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item?itemId=" + item.getId() + "&updateError=true"; // Redirect if imagePath is null
            }
        }
        else{
            return "redirect:/?notAllowed"; // Redirect if not allowed
        }

    }

}
