package com.telcreat.aio.viewController;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


@Data
@RequestScope
@Controller
@SessionAttributes({"searchForm", "categories"})
public class viewController {

    private final CartService cartService;
    private final ItemService itemService;
    private final PictureService pictureService;
    private final ShopOrderService shopOrderService;
    private final UserService userService;
    private final VariantService variantService;
    private final CategoryService categoryService;
    private final VerificationTokenService verificationTokenService;
    private final FileUploaderService fileUploaderService;
    private final ShopService shopService;
    private final HttpServletRequest request;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner;

    @Autowired
    public viewController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request) {
        this.cartService = cartService;
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.shopOrderService = shopOrderService;
        this.userService = userService;
        this.variantService = variantService;
        this.categoryService = categoryService;
        this.verificationTokenService = verificationTokenService;
        this.fileUploaderService = fileUploaderService;
        this.shopService = shopService;
        this.request = request;

        loggedUser = userService.getLoggedUser();
        if (loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER){
                isOwner = true;
            }
        }
    }


    @ModelAttribute("searchForm")
    public SearchForm setUpSearchForm(){
        return new SearchForm();
    }

    @ModelAttribute("categories")
    public List<Category> setUpSearchCategories(){
        return categoryService.findAllCategories();
    }

    // Search View
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String searchView(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                             @RequestParam(name = "orderCriteriaId", required = false, defaultValue = "0") Integer orderCriteriaId,
                             @RequestParam(name = "orderDirection", required = false, defaultValue = "0") Integer orderDirection,
                             @RequestParam(name = "search", required = false, defaultValue = "") String search,
                             ModelMap modelMap) throws IOException, GeoIp2Exception {

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);
        Shop shop = shopService.findActiveShopByOwnerId(loggedId);
        int shops = shopService.findAllShops().size();
        int users = userService.findAllUsers().size();
        int products = variantService.findAllVariants().size();

        if (shop != null){
            modelMap.addAttribute("loggedShopId",shop.getId());
        }


        ContactForm contactForm = new ContactForm();
        modelMap.addAttribute("contactForm", contactForm);

        // Get remote IP debug
        // modelMap.addAttribute("clientIP", request.getRemoteAddr());
        // FIND CLIENTS IP ADDRESS

        modelMap.addAttribute("categoryId", categoryId);
        modelMap.addAttribute("orderCriteriaId", orderCriteriaId);
        modelMap.addAttribute("orderDirection", orderDirection);
        modelMap.addAttribute("search", search);
        modelMap.addAttribute("pageTitle", "AIO");

        modelMap.addAttribute("shopKop", shops);
        modelMap.addAttribute("userKop", users);
        modelMap.addAttribute("prodKop", products);

        // Item Search - Item List based on Category and Name search
        // modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch

        return "index"; // Return Search search.html view
    }
    @RequestMapping(value = "/contact", method = RequestMethod.POST)
    public String contactForm(@ModelAttribute(name = "contactForm") ContactForm contactForm, ModelMap modelMap){
        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);

        // Send notification email
        SendEmail sendEmail = new SendEmail();
        sendEmail.sendContactMailToUser(contactForm);
        sendEmail.sendContactMail(contactForm);

        return "redirect:/";
    }

    // Product Search View
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String viewSearch(@ModelAttribute(name = "searchForm") SearchForm searchForm,
                             ModelMap modelMap) throws IOException, GeoIp2Exception {

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);
        Shop shop = shopService.findActiveShopByOwnerId(loggedId);
        if (shop != null){
            modelMap.addAttribute("loggedShopId",shop.getId());
        }

        modelMap.addAttribute("categoryId", searchForm.getCategoryId());
        modelMap.addAttribute("orderCriteriaId", searchForm.getOrderCriteriaId());
        modelMap.addAttribute("orderDirection", searchForm.getOrderDirection());
        modelMap.addAttribute("search", searchForm.getSearch());
        modelMap.addAttribute("pageTitle", "Bilaketa");

        modelMap.addAttribute("itemSearch", itemService.findItemsContainsNameOrdered(searchForm.getSearch(),  searchForm.getCategoryId(), searchForm.getOrderCriteriaId(), searchForm.getOrderDirection(), "1.1.1.1"));
        modelMap.addAttribute("shopSearch", shopService.orderedShopByItemContainsName(searchForm.getSearch(), searchForm.getCategoryId(), "1.1.1.1"));
        // modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch
        return "search";
    }

    @RequestMapping(value = "/cookie-politika", method = RequestMethod.GET)
    public String viewCookiak(ModelMap modelMap){

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);
        // modelMap.addAttribute("categories", categoryService.findAllCategories());
        modelMap.addAttribute("pageTitle", "CookiePolitika");

        return "cookie-politika";
    }

    @RequestMapping(value = "/pribatutasun-politika", method = RequestMethod.GET)
    public String viewPribatutasuna(ModelMap modelMap){

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);
        // modelMap.addAttribute("categories", categoryService.findAllCategories());
        modelMap.addAttribute("pageTitle", "PribatutasunPolitika");

        return "pribatutasun-politika";
    }

    @RequestMapping(value = "/kontaktua", method = RequestMethod.GET)
    public String viewKontaktua(ModelMap modelMap){

        // DEFAULT INFORMATION IN ALL VIEWS
        modelMap.addAttribute("isLogged", isLogged);
        modelMap.addAttribute("loggedUserId", loggedId);
        modelMap.addAttribute("loggedUserRole", loggedRole);
        modelMap.addAttribute("isOwner", isOwner);
        // modelMap.addAttribute("categories", categoryService.findAllCategories());
        modelMap.addAttribute("pageTitle", "Kontaktua");

        ContactForm contactForm = new ContactForm();
        modelMap.addAttribute("contactForm", contactForm);

        return "kontaktua";
    }




    // CheckOut View
    // Comment: it's not necessary to obtain any cart Id

    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String viewCheckout(@RequestParam() ModelMap modelMap){
        return "checkout";
    }

}
