package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Data
@RequestScope
@Controller
@SessionAttributes({"searchForm", "categories", "cartItemNumber"})
public class orderController {

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
    public orderController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request) {
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

    // Search View
    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public String viewOrder(@RequestParam(name = "orderId") int orderId,
                             ModelMap modelMap){
        ShopOrder shopOrder = shopOrderService.findShopOrderById(orderId);
        if(loggedUser != null && shopOrder!= null && (loggedId == shopOrder.getUser().getId() || loggedId == shopOrder.getShop().getOwner().getId())){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

            List<CartQuantity> orderVariantsAndQuantities = new ArrayList<>();
            ArrayList<Variant> uniqueVariantList = new ArrayList<>(new HashSet<>(shopOrder.getVariants()));
            int quantity;

            for (int i=0; i<uniqueVariantList.size(); i++) {
                CartQuantity cartQuantity = new CartQuantity();
                Variant tempVariant = uniqueVariantList.get(i);
                quantity = Collections.frequency(shopOrder.getVariants(), tempVariant);
                cartQuantity.setQuantity(quantity);
                cartQuantity.setVariant(tempVariant);
                orderVariantsAndQuantities.add(cartQuantity);
            }

            // View Order - ShopOrder List based on orderId
            modelMap.addAttribute("order", shopOrder);
            modelMap.addAttribute("orderVariantQuantList", orderVariantsAndQuantities);


            return "orderProducts"; // Return order to order.html view
        }else{
            return "redirect:/?notAllowed"; //In case any of the conditions required fails
        }
    }

    // Change Shop Order status
    @RequestMapping(value = "/order/edit/changeStatus", method = RequestMethod.POST)
    public String changeOrderStatus(@RequestParam(name = "shopOrderStatus") ShopOrder.ShopOrderStatus updateStatus,
                                    @RequestParam(name = "orderId") int orderId,
                                    ModelMap modelMap){
        ShopOrder shopOrder = shopOrderService.findNotCanceledNotDeliveredShopOrderById(orderId);

        if (isLogged && shopOrder != null && loggedId == shopOrder.getShop().getOwner().getId()){
            shopOrder.setShopOrderStatus(updateStatus);
            ShopOrder savedShopOrder = shopOrderService.updateShopOrder(shopOrder);
            if (savedShopOrder != null){

                // If owner cancels the order, Stock must be updated
                if (shopOrder.getShopOrderStatus() == ShopOrder.ShopOrderStatus.EZEZTATUTA){
                    for (Variant tempVariant : shopOrder.getVariants()){
                        int tempStock = tempVariant.getStock();
                        tempVariant.setStock(tempStock + 1);
                        variantService.updateVariant(tempVariant);
                    }
                }

                // Send notification email
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendOrderStatusUpdateNotificationToUser(savedShopOrder);

                return "redirect:/order?orderId=" + shopOrder.getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/order?orderId=" + shopOrder.getId() + "&orderUpdateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    // Cancel Shop Order - Client can Cancel the Shop Order)
    @RequestMapping(value = "/order/edit/cancel", method = RequestMethod.POST)
    public String cancelOrder(@RequestParam(name = "orderId") int orderId,
                              ModelMap modelMap){

        ShopOrder shopOrder = shopOrderService.findPendingShopOrderById(orderId); // Can only be cancelled when it's PENDING

        if (isLogged && shopOrder != null && (loggedId == shopOrder.getUser().getId() || loggedId == shopOrder.getShop().getOwner().getId())){
            shopOrder.setShopOrderStatus(ShopOrder.ShopOrderStatus.EZEZTATUTA);
            ShopOrder savedShopOrder = shopOrderService.updateShopOrder(shopOrder);

            if (savedShopOrder != null){

                for (Variant tempVariant : shopOrder.getVariants()){
                    int tempStock = tempVariant.getStock();
                    tempVariant.setStock(tempStock + 1);
                    variantService.updateVariant(tempVariant);
                }

                // Send notification email
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendOrderCancelledNotification(savedShopOrder);

                return "redirect:/user/myOrders?userId=" + shopOrder.getUser().getId();
            }
            else{
                return "redirect:/user/myOrders?userId=" + shopOrder.getUser().getId() + "&orderUpdateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }




}
