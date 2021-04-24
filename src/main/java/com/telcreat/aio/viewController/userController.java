package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Picture;
import com.telcreat.aio.model.User;
import com.telcreat.aio.model.UserEditForm;
import com.telcreat.aio.model.VerificationToken;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestScope
public class userController {


    private final PictureService pictureService;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final FileUploaderService fileUploaderService;
    private final User loggedUser;

    @Autowired
    public userController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request) {
        this.pictureService = pictureService;
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.fileUploaderService = fileUploaderService;
        loggedUser = this.userService.getLoggedUser();
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


}