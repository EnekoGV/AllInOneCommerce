package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;


@Data
@RequestScope
@Controller
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
    private User loggedUser;
    private final HttpServletRequest request;

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
        loggedUser = this.userService.getLoggedUser();
        this.request = request;
    }


    // Search View
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String searchView(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                             @RequestParam(name = "orderCriteriaId", required = false, defaultValue = "0") Integer orderCriteriaId,
                             @RequestParam(name = "search", required = false, defaultValue = "") String itemName,
                             ModelMap modelMap){

        // Get remote IP debug
        modelMap.addAttribute("clientIP", request.getRemoteAddr());
        // FIND CLIENTS IP ADDRESS

        // Item Search - Item List based on Category and Name search
        modelMap.addAttribute("itemSearch", itemService.findItemsContainsNameOrdered(itemName, orderCriteriaId, categoryId));
        modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch

        // DISPLAY LOGGED IN USER'S NAME
        modelMap.addAttribute("loggedUser", loggedUser.getName());
        modelMap.addAttribute("loggedUserId", loggedUser.getId());
        if(userService.getLoggedUser().getUserRole() == User.UserRole.OWNER)
            modelMap.addAttribute("owner",true);
        else
            modelMap.addAttribute("owner",false);
        // SHOP LIST IS PENDING

        return "search"; // Return Search search.html view
    }


    //Register and Login page
    @RequestMapping(value = "/auth" , method = RequestMethod.GET)
    public String register(@RequestParam(name = "registrationError", required = false, defaultValue = "false") boolean registrationError,
                               @RequestParam(name = "loginError", required = false, defaultValue = "false") boolean loginError, // Control param for login error
                               ModelMap modelMap){

        User login = new User();
        User signup = new User();
        modelMap.addAttribute("login", login);
        modelMap.addAttribute("signup", signup);

        // Error control params
        modelMap.addAttribute("loginError", loginError); // Control param to display error message
        modelMap.addAttribute("registrationError", registrationError); // Control param to display error message

        return "auth";
    }

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public String receiveRegister(@ModelAttribute User user, ModelMap modelMap){
        User newUser = userService.signUpUser(user);
        String token;
        if(newUser != null) {
            token = verificationTokenService.findTokenByUserId(newUser.getId());
            modelMap.clear();
            return "redirect:/auth/verification?token=" + token;
        }
        else {
            return "redirect:/auth?registrationError=true";
        }
    }

    @RequestMapping(value = "/auth/verification", method = RequestMethod.GET)
    public String tokenVerification(@RequestParam(name = "token") String token,
                                    @RequestParam(name = "verificationError", required = false, defaultValue = "false") boolean verificationError,
                                    ModelMap modelMap){

        modelMap.addAttribute("token", token);
        modelMap.addAttribute("verificationError", verificationError);

        return "verification";
    }

    @RequestMapping(value = "/auth/verification", method = RequestMethod.POST)
    public String receiveTokenVerification(@RequestParam(name = "token") String token,
                                           @RequestParam(name = "code") String code,
                                           ModelMap modelMap){

        boolean control = userService.validateUser(token, code); // Security check - Token and code integrity
        if (control){
            return "redirect:/auth?OK";
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/auth/verification?token=" + token + "&verificationError=true";
        }
    }


    // View and edit profile
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String viewAndEditProfile(@RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
                                     @RequestParam(name = "userId") int userId,
                                     @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                     ModelMap modelMap){

        if (loggedUser != null && loggedUser.getId() == userId){ // Allow editing only each user's profile.

            modelMap.addAttribute("userAvatar", loggedUser.getPicture().getPath());
            modelMap.addAttribute("userForm", new UserEditForm(loggedUser.getId(),
                    loggedUser.getAlias(),
                    loggedUser.getName(),
                    loggedUser.getLastName(),
                    loggedUser.getBirthDay(),
                    loggedUser.getEmail(),
                    loggedUser.getAddressStreet(),
                    loggedUser.getAddressNumber(),
                    loggedUser.getAddressFlat(),
                    loggedUser.getAddressDoor(),
                    loggedUser.getAddressCountry(),
                    loggedUser.getPostCode(),
                    loggedUser.getAddressCity(),
                    loggedUser.getAddressRegion()));

            modelMap.addAttribute("edit", edit); // Error message variable
            modelMap.addAttribute("updateError", updateError); // Error message variable

            return "editUser";
        }else{
            return "redirect:/";
        }
    }


    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateProfile(@ModelAttribute(name = "userForm") UserEditForm userForm,
                                ModelMap modelMap){
        modelMap.clear();

        if (loggedUser != null && userForm.getId() == loggedUser.getId()){ // Security check - Verify logged user
            loggedUser.setAlias(userForm.getAlias());
            loggedUser.setName(userForm.getName());
            loggedUser.setLastName(userForm.getLastName());
            loggedUser.setBirthDay(userForm.getBirthDay());
            loggedUser.setEmail(userForm.getEmail());
            loggedUser.setAddressStreet(userForm.getAddressStreet());
            loggedUser.setAddressNumber(userForm.getAddressNumber());
            loggedUser.setAddressFlat(userForm.getAddressFlat());
            loggedUser.setAddressDoor(userForm.getAddressDoor());
            loggedUser.setAddressCountry(userForm.getAddressCountry());
            loggedUser.setPostCode(userForm.getPostCode());
            loggedUser.setAddressCity(userForm.getAddressCity());
            loggedUser.setAddressRegion(userForm.getAddressRegion());

            User tempUser = userService.updateUser(loggedUser); // Update user information in DB
            if (tempUser != null){
                return "redirect:/user?userId=" + loggedUser.getId(); // Redirect to user profile
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + loggedUser.getId() + "&updateError=true"; // Redirect to user profile with error flag
            }
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + userForm.getId() + "&updateError=true";// Redirect to user profile with error flag
        }
    }


    @RequestMapping(value = "/user/uploadPicture", method = RequestMethod.POST)
    public String uploadUserPicture(@RequestParam(name = "userPicture") MultipartFile file,
                                    @RequestParam(name = "userId") Integer userId,
                                    ModelMap modelMap){

        if (loggedUser != null && loggedUser.getId() == userId){ // Security check - Verify logged user
            String imagePath = fileUploaderService.uploadUserPicture(file,userId, "/user"); // Upload image to server filesystem
            if(imagePath != null){ // Security check - Besides, will always be not null
                Picture loggedUserPicture = loggedUser.getPicture(); // Obtain Picture object
                loggedUserPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(loggedUserPicture); // Update Object
                modelMap.clear();

                return "redirect:/user?userId=" + loggedUser.getId(); // Return to User View
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + loggedUser.getId() + "&updateError=true"; // Redirect if imagePath is null
            }

        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + userId + "&updateError=true"; // Redirect if not allowed
        }

    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
    public String changeUserPassword(@RequestParam(name = "userId") int userId,
                                     @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                     ModelMap modelMap){

        if (loggedUser != null && loggedUser.getId() == userId){ // Security check - Verify logged user
            modelMap.addAttribute("userId", userId);
            modelMap.addAttribute("updateError", updateError);
            return "changePassword"; // Server view
        }
        else{
            return "redirect:/?updateError=true"; // Redirect to homepage if not allowed
        }
    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.POST)
    public String receiveChangePassword(@RequestParam(name = "userId") int userId,
                                        @RequestParam(name = "newPassword") String newPassword,
                                        @RequestParam(name = "repeatPassword") String repeatPassword,
                                        ModelMap modelMap){

        if (loggedUser != null && loggedUser.getId() == userId && newPassword.equals(repeatPassword)){ // Security check - Logged User and Password validation
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            SendEmail emailSender = new SendEmail();
            loggedUser.setEnabled(false); // Disable user
            loggedUser.setPassword(bCryptPasswordEncoder.encode(newPassword)); // Encode new password
            User savedUser = userService.updateUser(loggedUser); // Update user information in DB
            VerificationToken verificationToken = verificationTokenService.createVerificationToken(savedUser); // Create verification code pair
            emailSender.send(savedUser.getEmail(), verificationToken); // Send verification email
            modelMap.clear(); // Clear view
            return "redirect:/auth/verification?token=" + verificationToken.getToken(); // Redirect to verification page
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user/changePassword?userId=" + userId + "&updateError=true"; // Return to password change page
        }

    }

    @RequestMapping(value = "/auth/recoverPassword", method = RequestMethod.GET)
    public String recoverPassword(@RequestParam(name = "token") String token,
                                  @RequestParam(name = "code") String code,
                                  @RequestParam(name = "recoveryError", required = false, defaultValue = "false") boolean recoveryError,
                                  ModelMap modelMap){

        VerificationToken verificationToken = verificationTokenService.findVerificationTokenById(token); // Obtain code pair (token and code) from token
        if (verificationToken != null && verificationToken.getCode().equals(code)){ // Security check - Token and code integrity
            modelMap.addAttribute("token", token);
            modelMap.addAttribute("code", code);
            modelMap.addAttribute("recoveryError", recoveryError);

            return "recoverPassword"; // Serve view
        }
        else{
            return "redirect:/?linkExpired=true"; // Redirect to homepage if not allowed
        }

    }

    @RequestMapping(value = "/auth/recoverPassword", method = RequestMethod.POST)
    public String receiveRecoverPassword(@RequestParam(name = "token") String token,
                                         @RequestParam(name = "code") String code,
                                         @RequestParam(name = "newPassword") String newPassword,
                                         @RequestParam(name = "repeatPassword") String repeatPassword,
                                         ModelMap modelMap){

        VerificationToken verificationToken = verificationTokenService.findVerificationTokenById(token); // Obtain code pair (code and token) from token
        User user = userService.findUserById(verificationToken.getUser().getId()); // Obtain user related to token

        if (user != null && verificationToken.getCode().equals(code) && newPassword.equals(repeatPassword)){ // Security check - Token and code integrity
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                user.setPassword(bCryptPasswordEncoder.encode(newPassword)); // Encode new password
                user.setEnabled(true); // Enable user
                userService.updateUser(user); // Update user information in DB
                verificationTokenService.deleteVerificationToken(token); // Delete verification code pair (token and code)
                return "redirect:/auth"; // Redirect to login page
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/auth/recoverPassword?token=" + token + "&code=" + code + "&recoveryError=true"; // Redirect to password recovery with error flag
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

        Shop shop = shopService.findActiveShopById(shopId); // Obtain queried shop
        boolean ownerLogged = false; // View-control variable

        if (shop != null){ // If shop exists

            if (loggedUser.getId() == shop.getOwner().getId()){
                ownerLogged = true;
            }

            modelMap.addAttribute("shop", shop); // Send shop object
            modelMap.addAttribute("itemList", itemService.findActiveItemsByShopId(shop.getId())); // Send item list
            modelMap.addAttribute("ownerLogged", ownerLogged); // Send view-control variable
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

    // CheckOut View
    // Comment: it's not necessary to obtain any cart Id

    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String viewCheckout(@RequestParam() ModelMap modelMap){
        return "checkout";
    }

}
