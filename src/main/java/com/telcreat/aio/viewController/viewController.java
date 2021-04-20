package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.model.Shop;
import com.telcreat.aio.service.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
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


    @Autowired
    public viewController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService) {
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
    }


    // Search View
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String searchView(@RequestParam(name = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                             @RequestParam(name = "orderCriteriaId", required = false, defaultValue = "0") Integer orderCriteriaId,
                             @RequestParam(name = "search", required = false, defaultValue = "") String itemName,
                             ModelMap modelMap){

        // Item Search - Item List based on Category and Name search
        modelMap.addAttribute("itemSearch", itemService.findItemsContainsNameOrdered(itemName, orderCriteriaId, categoryId));
        modelMap.addAttribute("categories", categoryService.findAllCategories()); // Category List for ItemSearch

        // DISPLAY LOGGED IN USER'S NAME
        modelMap.addAttribute("loggedUser", userService.getLoggedUser().getName());
        modelMap.addAttribute("loggedUserId", userService.getLoggedUser().getId());

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

        if (userService.getLoggedUser().getId() == userId){ // Allow editing only each user's profile.
            User user = userService.findUserById(userId); // We don't send all the information to frontend.

            modelMap.addAttribute("userAvatar", user.getPicture().getPath());
            modelMap.addAttribute("userForm", new UserEditForm(user.getId(),
                    user.getAlias(),
                    user.getName(),
                    user.getLastName(),
                    user.getBirthDay(),
                    user.getEmail(),
                    user.getAddressStreet(),
                    user.getAddressNumber(),
                    user.getAddressFlat(),
                    user.getAddressDoor(),
                    user.getAddressCountry(),
                    user.getPostCode(),
                    user.getAddressCity(),
                    user.getAddressRegion()));

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

        User user = userService.findUserById(userForm.getId());

        user.setAlias(userForm.getAlias());
        user.setName(userForm.getName());
        user.setLastName(userForm.getLastName());
        user.setBirthDay(userForm.getBirthDay());
        user.setEmail(userForm.getEmail());
        user.setAddressStreet(userForm.getAddressStreet());
        user.setAddressNumber(userForm.getAddressNumber());
        user.setAddressFlat(userForm.getAddressFlat());
        user.setAddressDoor(userForm.getAddressDoor());
        user.setAddressCountry(userForm.getAddressCountry());
        user.setPostCode(userForm.getPostCode());
        user.setAddressCity(userForm.getAddressCity());
        user.setAddressRegion(userForm.getAddressRegion());

        if (userForm.getId() == userService.getLoggedUser().getId()){ // Security check - Verify logged user
            User tempUser = userService.updateUser(user); // Update user information in DB
            if (tempUser != null){
                return "redirect:/user?userId=" + userService.getLoggedUser().getId(); // Redirect to user profile
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true"; // Redirect to user profile with error flag
            }
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true";// Redirect to user profile with error flag
        }
    }


    @RequestMapping(value = "/user/uploadPicture", method = RequestMethod.POST)
    public String uploadUserPicture(@RequestParam(name = "userPicture") MultipartFile file,
                                    @RequestParam(name = "userId") Integer userId,
                                    ModelMap modelMap){

        if (userService.getLoggedUser().getId() == userId){ // Security check - Verify logged user
            String imagePath = fileUploaderService.uploadUserPicture(file,userId, "/user"); // Upload image to server filesystem
            if(imagePath != null){ // Security check - Besides, will always be not null
                User loggedUser = userService.getLoggedUser(); // Obtain Logged User
                Picture loggedUserPicture = loggedUser.getPicture(); // Obtain Picture object
                loggedUserPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(loggedUserPicture); // Update Object
                modelMap.clear();

                return "redirect:/user?userId=" + userService.getLoggedUser().getId(); // Return to User View
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true"; // Redirect if imagePath is null
            }

        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true"; // Redirect if not allowed
        }

    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
    public String changeUserPassword(@RequestParam(name = "userId") int userId,
                                     @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                     ModelMap modelMap){
        User loggedUser = userService.getLoggedUser(); // Get logged user
        if (loggedUser.getId() == userId){ // Security check - Verify logged user
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
        User loggedUser = userService.getLoggedUser(); // Get logged user
        if (loggedUser.getId() == userId && newPassword.equals(repeatPassword)){ // Security check - Logged User and Password validation
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

    //Shop edit view

   /* @RequestMapping(value = "/shop", method = RequestMethod.GET)
    public String shopEditView(ModelMap modelMap){
        User user =userService.getLoggedUser();
        //Shop shop =shopService.findShopByUser_Id(user.getId())
        //modelMap.addAttribute("shop", shop);
        return "shop";
    }*/

    @RequestMapping(value = "/shop/edit", method = RequestMethod.POST)
    public String receiveEditedShop(@ModelAttribute Shop shop, ModelMap modelMap){
       /* if(shopService.updateShop != null)
            modelMap.clear();
        else
            return "redirect:/shop/edit/fail";*/
        return "redirect:/shop/edit/OK";
    }

    // Shop Page View

    @RequestMapping(value = "/shop", method = RequestMethod.GET)
    public String viewShop(@RequestParam(name = "shopId") int shopId,
                           ModelMap modelMap){

        User loggedUser = userService.getLoggedUser(); // Obtain logged user
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

        User loggedUser = userService.getLoggedUser();
        Shop shop = shopService.findActiveShopById(shopId);

        if (loggedUser != null && shop != null && loggedUser.getId() == shop.getOwner().getId()){ // Security check
            modelMap.addAttribute("shop", shop); // Send shop object
            modelMap.addAttribute("itemList", itemService.findActiveItemsByShopId(shop.getId())); // Send item list
        }
        else{
            return "error/error-404";
        }
    }

    // CheckOut View
    // Comment: it's not necessary to obtain any cart Id

    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String viewCheckout(@RequestMapping() ModelMap modelMap){

    }

}
