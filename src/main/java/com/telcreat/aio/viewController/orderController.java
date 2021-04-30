package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Shop;
import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.User;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;

@Data
@RequestScope
@Controller
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

            // View Order - ShopOrder List based on orderId
            modelMap.addAttribute("order", shopOrder);

            return "order"; // Return order to order.html view
        }else{
            return "redirect:/?orderVisualizationsRequiredCondiotionsFailed"; //In case any of the conditions required fails
        }
    }

    // Change Shop Order status
    @RequestMapping(value = "/order/edit/changeStatus", method = RequestMethod.POST)
    public String changeOrderStatus(@RequestParam(name = "updateStatus") ShopOrder.ShopOrderStatus updateStatus,
                                    @RequestParam(name = "orderId") int orderId,
                                    ModelMap modelMap){
        ShopOrder shopOrder = shopOrderService.findNotCanceledNotDeliveredShopOrderById(orderId);

        if (isLogged && shopOrder != null && loggedId == shopOrder.getShop().getOwner().getId()){
            shopOrder.setShopOrderStatus(updateStatus);
            ShopOrder savedShopOrder = shopOrderService.updateShopOrder(shopOrder);
            if (savedShopOrder != null){

                // Send notification email
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendOrderStatusUpdateNotificationToUser(savedShopOrder);

                return "redirect:/order?orderId=" + shopOrder.getId();
            }
            else{
                return "redirect:/order?orderId=" + shopOrder.getId() + "&orderUpdateError=true";
            }
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    // Cancel Shop Order (Owner or Client can Cancel the Shop Order)
    @RequestMapping(value = "/order/edit/cancel", method = RequestMethod.POST)
    public String cancelOrder(@RequestParam(name = "orderId") int orderId,
                              ModelMap modelMap){

        ShopOrder shopOrder = shopOrderService.findPendingShopOrderById(orderId); // Can only be cancelled when it's PENDING

        if (isLogged && shopOrder != null && (loggedId == shopOrder.getUser().getId() || loggedId == shopOrder.getShop().getOwner().getId())){
            shopOrder.setShopOrderStatus(ShopOrder.ShopOrderStatus.CANCELLED);
            ShopOrder savedShopOrder = shopOrderService.updateShopOrder(shopOrder);
            if (savedShopOrder != null){

                // Send notification email
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendOrderCancelledNotification(savedShopOrder);

                return "redirect:/order?orderId=" + shopOrder.getId();
            }
            else{
                return "redirect:/order?orderId=" + shopOrder.getId() + "&orderUpdateError=true";
            }
        }
        else{
            return "redirect:/notAllowed";
        }
    }




}
