package com.telcreat.aio.viewController;

import com.telcreat.aio.model.*;
import com.telcreat.aio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
@SessionAttributes({"searchForm", "categories"})
public class authController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final CategoryService categoryService;

    private final User loggedUser;
    private boolean isLogged = false;
    private int loggedId;

    private final CartService cartService;

    @Autowired
    public authController(ItemService itemService, PictureService pictureService, ShopOrderService shopOrderService, UserService userService, VariantService variantService, CategoryService categoryService, VerificationTokenService verificationTokenService, FileUploaderService fileUploaderService, ShopService shopService, HttpServletRequest request, CartService cartService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        loggedUser = userService.getLoggedUser();
        if(loggedUser != null){
            isLogged = true;
            loggedId = loggedUser.getId();
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


    //Register and Login page
    @RequestMapping(value = "/auth" , method = RequestMethod.GET)
    public String register(@RequestParam(name = "registrationError", required = false, defaultValue = "false") boolean registrationError,
                           @RequestParam(name = "loginError", required = false, defaultValue = "false") boolean loginError, // Control param for login error
                           @RequestParam(name = "accountVerified", required = false, defaultValue = "false") boolean accountVerified,
                           @RequestParam(name = "logout", required = false, defaultValue = "false")boolean logout,
                           ModelMap modelMap){

        User login = new User();
        User signup = new User();
        modelMap.addAttribute("login", login);
        modelMap.addAttribute("signup", signup);

        // Error control params
        modelMap.addAttribute("loginError", loginError); // Control param to display error message
        modelMap.addAttribute("registrationError", registrationError); // Control param to display error message
        modelMap.addAttribute("accountVerified", accountVerified);
        modelMap.addAttribute("logout",logout);


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
            return "redirect:/auth?accountVerified=true";
        }
        else{
            //noinspection SpringMVCViewInspection
            return "redirect:/auth/verification?token=" + token + "&verificationError=true";
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

}
