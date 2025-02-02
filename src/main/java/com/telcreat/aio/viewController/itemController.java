package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@RequestScope
@Controller
@SessionAttributes({"searchForm", "categories", "cartItemNumber"})
public class itemController {

    private final ItemService itemService;
    private final PictureService pictureService;
    private final UserService userService;
    private final ShopService shopService;
    private final FileUploaderService fileUploaderService;
    private final VariantService variantService;
    private final CategoryService categoryService;
    private final CartService cartService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner = false;

    @Autowired
    public itemController(ItemService itemService, PictureService pictureService, UserService userService, FileUploaderService fileUploaderService, ShopService shopService, VariantService variantService, CategoryService categoryService, CartService cartService) {
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.userService = userService;
        this.shopService = shopService;

        loggedUser = userService.getLoggedUser();
        this.cartService = cartService;
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
            Cart cart = cartService.findCartByUserId(loggedId);
            if(cart != null){
                modelMap.addAttribute("cartId",cart.getId());
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

            return "redirect:/item/edit?itemId=" + savedItem.getId() + "&createSuccessful=true";
        }
        else{
            return "redirect:/?errorCreatingItem";
        }
    }

    @RequestMapping(value = "/item/edit", method = RequestMethod.GET)
    public String viewEditItem(@RequestParam(name = "itemId") int itemId,
                               @RequestParam(name = "editVariantNumber", required = false, defaultValue = "0") int editVariantNumber,
                               @RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
                               @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                               @RequestParam(name = "updateSuccessful", required = false, defaultValue = "false") boolean updateSuccessful,
                               @RequestParam(name = "createSuccessful", required = false, defaultValue = "false") boolean createSuccessful,
                               @RequestParam(name = "variantDeleteError", required = false, defaultValue = "false") boolean variantDeleteError,
                               @RequestParam(name = "variantDeleteSuccessful", required = false, defaultValue = "false") boolean variantDeleteSuccessful,
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
                    item.getName(),
                    item.getItemCategory());

            modelMap.addAttribute("itemForm", itemForm);
            modelMap.addAttribute("item", item);
            modelMap.addAttribute("categoryList", categoryService.findAllCategories());

            modelMap.addAttribute("variantList", variantService.findActiveVariantsByItemId(item.getId()));

            modelMap.addAttribute("editVariantNumber", editVariantNumber);
            modelMap.addAttribute("edit", edit);

            modelMap.addAttribute("updateError", updateError);
            modelMap.addAttribute("updateSuccessful", updateSuccessful);
            modelMap.addAttribute("createSuccessful", createSuccessful);

            modelMap.addAttribute("variantDeleteError", variantDeleteError);
            modelMap.addAttribute("variantDeleteSuccessful", variantDeleteSuccessful);

            modelMap.addAttribute("variantForm", new Variant());

            return "addProduct";
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/item/edit", method = RequestMethod.POST)
    public String receiveEditItem(@ModelAttribute(name = "itemForm") ItemEditForm itemForm,
                                  ModelMap modelMap){

        modelMap.clear();

        Item item = itemService.findActiveItemById(itemForm.getId());

        if (isLogged && item != null && loggedId == item.getShop().getOwner().getId()){
            item.setName(itemForm.getName());
            item.setPrice(itemForm.getPrice());
            item.setShortDescription(itemForm.getShortDescription());
            item.setLongDescription(itemForm.getLongDescription());
            item.setItemCategory(itemForm.getItemCategory());

            Item savedItem = itemService.updateItem(item);
            if (savedItem != null){
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&updateSuccessful=true";
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&updateError=true";
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

                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&updateSuccessful=true";
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/item/edit?itemId=" + item.getId() + "&updateError=true"; // Redirect if imagePath is null
            }
        }
        else{
            return "redirect:/?notAllowed"; // Redirect if not allowed
        }

    }

}
