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
import java.util.Collections;
import java.util.List;

@Data
@Controller
@RequestScope
@SessionAttributes({"searchForm", "categories"})
public class userController {


    private final PictureService pictureService;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final FileUploaderService fileUploaderService;
    private final ShopService shopService;
    private final ShopOrderService shopOrderService;

    private User loggedUser;
    private boolean isLogged = false;
    private User.UserRole loggedRole = User.UserRole.CLIENT;
    private int loggedId;
    private boolean isOwner;

    @Autowired
    public userController(CartService cartService, ItemService itemService, PictureService pictureService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request, ShopOrderService shopOrderService) {
        this.pictureService = pictureService;
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.fileUploaderService = fileUploaderService;
        this.shopService = shopService;
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
    }


    // View and edit profile
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String viewAndEditProfile(@RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
                                     @RequestParam(name = "userId") int userId,
                                     @RequestParam(name = "updateError", required = false, defaultValue = "false") boolean updateError,
                                     @RequestParam(name = "updateSuccessful", required = false, defaultValue = "false") boolean updateSuccessful,
                                     ModelMap modelMap){

        if (isLogged && loggedId == userId){ // Allow editing only each user's profile.

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

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
            modelMap.addAttribute("updateSuccessful", updateSuccessful); // Error message variable

            return "editUser";
        }else{
            return "redirect:/";
        }
    }


    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateProfile(@ModelAttribute(name = "userForm") UserEditForm userForm,
                                ModelMap modelMap){

        modelMap.clear();

        if (isLogged && userForm.getId() == loggedId){ // Security check - Verify logged user

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
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + loggedUser.getId() + "&updateSuccessful=true"; // Redirect to user profile
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

        if (isLogged && loggedId == userId){ // Security check - Verify logged user

            String imagePath = fileUploaderService.uploadUserPicture(file,userId, "/user"); // Upload image to server filesystem

            if(imagePath != null){ // Security check - Besides, will always be not null
                Picture loggedUserPicture = loggedUser.getPicture(); // Obtain Picture object
                loggedUserPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(loggedUserPicture); // Update Object
                modelMap.clear();

                return "redirect:/user?userId=" + loggedUser.getId() + "&updateSuccessful=true"; // Return to User View
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

        if (isLogged && loggedId == userId){ // Security check - Verify logged user

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

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

        if (isLogged && loggedId == userId && newPassword.equals(repeatPassword)){ // Security check - Logged User and Password validation

            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            SendEmail emailSender = new SendEmail();
            loggedUser.setEnabled(false); // Disable user
            loggedUser.setPassword(bCryptPasswordEncoder.encode(newPassword)); // Encode new password
            User savedUser = userService.updateUser(loggedUser); // Update user information in DB
            VerificationToken verificationToken = verificationTokenService.createVerificationToken(savedUser); // Create verification code pair
            emailSender.sendVerification(savedUser.getEmail(), verificationToken); // Send verification email
            modelMap.clear(); // Clear view
            return "redirect:/auth/verification?token=" + verificationToken.getToken(); // Redirect to verification page
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user/changePassword?userId=" + userId + "&updateError=true"; // Return to password change page
        }
    }

    // List User's order list
    @RequestMapping(value = "/user/myOrders", method = RequestMethod.GET)
    public String viewUserOrders(@RequestParam(name = "userId") int userId,
                                 ModelMap modelMap){

        if (isLogged && loggedId == userId){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }
            List<ShopOrder> shopOrders = shopOrderService.findShopOrdersByUserId(userId);
            Collections.reverse(shopOrders);
            modelMap.addAttribute("orderList", shopOrders);

            return "userOrders";
        }
        else{
            return "redirect:/?notAllowed";
        }

    }

    @RequestMapping(value = "/user/favoriteShops", method = RequestMethod.GET)
    public String viewUserFavorites(@RequestParam(name = "userId") int userId, ModelMap modelMap){

        if (isLogged && loggedId == userId){

            // DEFAULT INFORMATION IN ALL VIEWS
            modelMap.addAttribute("isLogged", isLogged);
            modelMap.addAttribute("loggedUserId", loggedId);
            modelMap.addAttribute("loggedUserRole", loggedRole);
            modelMap.addAttribute("isOwner", isOwner);
            Shop shop = shopService.findActiveShopByOwnerId(loggedId);
            if (shop != null){
                modelMap.addAttribute("loggedShopId",shop.getId());
            }

            modelMap.addAttribute("pageTitle", "DendaGogokoenak");

            List<Shop> shops = userService.findFavoriteActiveShops(loggedId);
            modelMap.addAttribute("favoriteShops", shops);

            return "favoriteShops";
        }
        else{
            return "redirect:/?notAllowed";
        }
    }

    @RequestMapping(value = "/unmarkfavorite", method = RequestMethod.GET)
    public String unMarkFavorite(@RequestParam(name = "shopId") int shopId, ModelMap modelMap){
        Shop shop = shopService.findActiveShopById(shopId);
        if (isLogged && shop != null){
            userService.deleteFavoriteShop(loggedUser, shopService.findShopById(shopId));
            return "redirect:/user/favoriteShops?userId=" + loggedId;
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user/favoriteShops?userId=" + loggedId + "&notAllowed";
        }
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    public String favoriteShop(@RequestParam(name = "shopId") int shopId, ModelMap modelMap){
        Shop shop = shopService.findActiveShopById(shopId);
        if (isLogged && shop != null){
            userService.addFavoriteShop(loggedUser, shopService.findShopById(shopId));
            return "redirect:/shop?shopId=" + shopId;
        }
        else{
            return "redirect:/shop?shopId=" + shopId + "&notAllowed";
        }

    }

    @RequestMapping(value = "/nofavorite", method = RequestMethod.GET)
    public String noFavoriteShop(@RequestParam(name = "shopId") int shopId, ModelMap modelMap){
        Shop shop = shopService.findActiveShopById(shopId);
        if (isLogged && shop != null){
            userService.deleteFavoriteShop(loggedUser, shopService.findShopById(shopId));
            return "redirect:/shop?shopId=" + shopId;
        }
        else{
            return "redirect:/shop?shopId=" + shopId + "&notAllowed";
        }

    }
}
