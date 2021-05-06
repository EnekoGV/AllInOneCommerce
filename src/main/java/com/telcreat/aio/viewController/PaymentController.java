package com.telcreat.aio.viewController;

import javax.servlet.http.HttpServletRequest;

import com.telcreat.aio.config.PaypalPaymentIntent;
import com.telcreat.aio.config.PaypalPaymentMethod;
import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.User;
import com.telcreat.aio.service.PaypalService;
import com.telcreat.aio.service.SendEmail;
import com.telcreat.aio.service.ShopOrderService;
import com.telcreat.aio.service.UserService;
import com.telcreat.aio.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.annotation.RequestScope;

@Controller
@RequestScope
@SessionAttributes({"searchForm", "categories", "cartItemNumber"})
public class PaymentController {

    public static final String PAYPAL_SUCCESS_URL = "/payment/success";
    public static final String PAYPAL_CANCEL_URL = "/payment/cancel";

    private Logger log = LoggerFactory.getLogger(getClass());

    private final PaypalService paypalService;
    private final ShopOrderService shopOrderService;
    private final UserService userService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner = false;

    @Autowired
    public PaymentController(PaypalService paypalService, ShopOrderService shopOrderService, UserService userService) {
        this.paypalService = paypalService;
        this.shopOrderService = shopOrderService;

        loggedUser = userService.getLoggedUser();
        if (loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
            loggedRole = loggedUser.getUserRole();
            if (loggedRole == User.UserRole.OWNER){
                isOwner = true;
            }
        }
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/payment")
    public String pay(@RequestParam(name = "orderId") int orderId,
                      HttpServletRequest request){
        ShopOrder shopOrder = shopOrderService.findShopOrderById(orderId);

        if (isLogged && shopOrder != null && loggedId == shopOrder.getUser().getId()){
            String cancelUrl = URLUtils.getBaseURl(request) +  PAYPAL_CANCEL_URL + "?orderId=" + shopOrder.getId();
            String successUrl = URLUtils.getBaseURl(request) + PAYPAL_SUCCESS_URL + "?orderId=" + shopOrder.getId();
            try {
                Payment payment = paypalService.createPayment(
                        (double) shopOrder.getPrice(),
                        "EUR",
                        PaypalPaymentMethod.paypal,
                        PaypalPaymentIntent.sale,
                        "AIOCommerce Payment",
                        cancelUrl,
                        successUrl);
                for(Links links : payment.getLinks()){
                    if(links.getRel().equals("approval_url")){
                        return "redirect:" + links.getHref();
                    }
                }
            } catch (PayPalRESTException e) {
                log.error(e.getMessage());
            }
            return "redirect:/?paymentError=true";
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = PAYPAL_CANCEL_URL)
    public String cancelPay(@RequestParam(name = "orderId") int orderId){
        ShopOrder shopOrder = shopOrderService.findShopOrderById(orderId);

        //noinspection SpringMVCViewInspection
        return "redirect:/user/myOrders?userId=" + shopOrder.getUser().getId() + "&paymentConfirmation=false";
    }

    @RequestMapping(method = RequestMethod.GET, value =
            PAYPAL_SUCCESS_URL)
    public String successPay(@RequestParam(name = "orderId") int orderId,
                             @RequestParam("paymentId") String paymentId,
                             @RequestParam("PayerID") String payerId){
        ShopOrder shopOrder = shopOrderService.findShopOrderById(orderId);
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved") && shopOrder != null){
                shopOrder.setShopOrderStatus(ShopOrder.ShopOrderStatus.BAIEZTATUTA);
                ShopOrder savedShopOrder = shopOrderService.updateShopOrder(shopOrder);

                // Send notification email
                SendEmail sendEmail = new SendEmail();
                sendEmail.sendOrderStatusUpdateNotificationToUser(savedShopOrder);

                //noinspection SpringMVCViewInspection
                return "redirect:/user/myOrders?userId=" + savedShopOrder.getUser().getId() + "&paymentConfirmation=true";
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "redirect:/";
    }

}
