package com.telcreat.aio.viewController;

import com.telcreat.aio.model.Picture;
import com.telcreat.aio.model.User;
import com.telcreat.aio.model.UserEditForm;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @Autowired
    public viewController(CartService cartService, ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService) {
        this.cartService = cartService;
        this.itemService = itemService;
        this.pictureService = pictureService;
        this.shopOrderService = shopOrderService;
        this.userService = userService;
        this.variantService = variantService;
        this.categoryService = categoryService;
        this.verificationTokenService = verificationTokenService;
        this.fileUploaderService = fileUploaderService;
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
    public String registerView(@RequestParam(name = "registrationError", required = false, defaultValue = "false") boolean registrationError,
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
    public String tokenVerificationView(@RequestParam(name = "token") String token,
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

        boolean control = userService.validateUser(token, code);
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

        if (userForm.getId() == userService.getLoggedUser().getId()){
            User tempUser = userService.updateUser(user);
            if (tempUser != null){
                return "redirect:/user?userId=" + userService.getLoggedUser().getId();
            }
            else{
                //noinspection SpringMVCViewInspection
                return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true";
            }
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true";
        }
    }


    @RequestMapping(value = "/user/uploadPicture", method = RequestMethod.POST)
    public String uploadUserPicture(@RequestParam(name = "userPicture") MultipartFile file,
                                    @RequestParam(name = "userId") Integer userId,
                                    ModelMap modelMap){

        if (userService.getLoggedUser().getId() == userId){
            String imagePath = fileUploaderService.uploadUserPicture(file,userId, "/user");
            if(imagePath != null){
                User loggedUser = userService.getLoggedUser(); // Obtain Logged User
                Picture loggedUserPicture = loggedUser.getPicture(); // Obtain Picture object
                loggedUserPicture.setPath(imagePath); // Set new path
                pictureService.updatePicture(loggedUserPicture); // Update Object
                modelMap.clear();

                return "redirect:/user?userId=" + userService.getLoggedUser().getId(); // Return to User View
            }
            else{
                return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true";
            }

        }
        else{
            return "redirect:/user?userId=" + userService.getLoggedUser().getId() + "&updateError=true";
        }

    }

}
