package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Item;
import com.telcreat.aio.model.Picture;
import com.telcreat.aio.model.User;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

@Data
@RequestScope
@Controller
@SessionAttributes("searchForm")
public class variantController {

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
    public variantController(ItemService itemService, PictureService pictureService, UserService userService, FileUploaderService fileUploaderService, ShopService shopService, VariantService variantService) {
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

    @RequestMapping(value = "/variant/edit", method = RequestMethod.POST)
    public String receiveEditVariant(@RequestParam(name = "variantId") int variantId,
                                     @RequestParam(name = "variantName") String variantName,
                                     @RequestParam(name = "variantStock") int variantStock,
                                     ModelMap modelMap){

        modelMap.clear();

        Variant variant = variantService.findActiveVariantById(variantId);
        if (isLogged && variant != null && loggedId == variant.getItem().getShop().getOwner().getId()){
            variant.setName(variantName);
            variant.setStock(variantStock);
            Variant savedVariant = variantService.updateVariant(variant);
            if (savedVariant != null){
                return "redirect:/item/edit?itemId=" + variant.getItem().getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + variant.getItem().getId() + "&variantUpdateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/variant/edit/delete", method = RequestMethod.POST)
    public String deactivateVariant(@RequestParam(name = "variantId") int variantId,
                                    ModelMap modelMap){
        Variant variant = variantService.findActiveVariantById(variantId);
        boolean control;
        if (isLogged && variant != null && loggedId == variant.getItem().getShop().getOwner().getId()){
            control = variantService.deactivateVariant(variant.getId());
            if (control){
                return "redirect:/item/edit?itemId=" + variant.getItem().getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + variant.getItem().getId() + "&variantDeleteError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/variant/edit/uploadPicture", method = RequestMethod.POST)
    public String uploadVariantPicture(@RequestParam(name = "variantPicture") MultipartFile file,
                                    @RequestParam(name = "variantId") int variantId,
                                    ModelMap modelMap){

        Variant variant = variantService.findActiveVariantById(variantId);

        if (isLogged && variant != null && loggedId == variant.getItem().getShop().getOwner().getId()){ // Security check - Verify logged user

            String imagePath = fileUploaderService.uploadUserPicture(file, variantId, "/variant"); // Upload image to server filesystem

            if(imagePath != null){ // Security check - Besides, will always be not null
                Picture variantPicture = variant.getPicture(); // Obtain Picture object
                variantPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(variantPicture); // Update Object
                modelMap.clear();

                return "redirect:/item/edit?itemId=" + variant.getItem().getId(); // Return to User View
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item?itemId=" + variant.getItem().getId() + "&updateError=true"; // Redirect if imagePath is null
            }
        }
        else{
            return "redirect:/?notAllowed"; // Redirect if not allowed
        }

    }

    @RequestMapping(value = "/variant/edit/create", method = RequestMethod.POST)
    public String createVariant(@ModelAttribute(name = "variantForm") Variant newVariant,
                                @RequestParam(name = "itemId") int itemId,
                                ModelMap modelMap){

        Item item = itemService.findActiveItemById(itemId);

        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){
            Picture newPicture = new Picture("/images/Item.png");
            Picture savedPicture = pictureService.createPicture(newPicture);
            Variant savedVariant = variantService.createVariant(new Variant(newVariant.getName(), newVariant.getStock(), savedPicture, item, Variant.Status.ACTIVE));
            if (savedVariant != null){
                return "redirect:/item/edit?itemId=" + item.getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item?itemId=" + item.getId() + "&updateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

}
