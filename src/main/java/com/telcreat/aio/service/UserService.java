package com.telcreat.aio.service;

import com.telcreat.aio.model.*;
import com.telcreat.aio.repo.UserRepo;
import com.telcreat.aio.repo.VerificationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final SendEmail emailSender;
    private final PictureService pictureService;
    private final ShopService shopService;
    private final CartService cartService;


    @Autowired
    public UserService(UserRepo userRepo, VerificationTokenService verificationTokenService, VerificationTokenRepo verificationTokenRepo, PictureService pictureService, ShopService shopService, CartService cartService){
        this.userRepo = userRepo;
        this.cartService = cartService;
        bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.verificationTokenService = verificationTokenService;
        emailSender = new SendEmail();
        this.pictureService = pictureService;
        this.shopService = shopService;
    }

    //________________________________________________________________________________________________________________//
    /////////////////////////////////////////////////////////////////////////////
    //                             BASIC METHODS                               //
    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

    //BM - findAllUsers ---> Returns a List of all users
    public List<User> findAllUsers(){
        return userRepo.findAll();
    }

    //BM - findUserById ---> Returns de user or a null object if not found
    public User findUserById(int id){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findById(id);
        if(foundUser.isPresent())
            userTemp = foundUser.get();
        return userTemp;
    }

    //BM - createUser ---> Returns new user if created or null if not
    public User createUser(User newUser){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findUserByEmail(newUser.getEmail());
        if(!userRepo.existsById(newUser.getId()) && !foundUser.isPresent())
            userTemp = userRepo.save(newUser);
        return userTemp;
    }

    //BM - updateUser ---> Returns updated user if ok or null if not found
    public User updateUser(User user){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findUserByEmail(user.getEmail());
        if(userRepo.existsById(user.getId()) && foundUser.isPresent() && foundUser.get().getId() == user.getId())
            userTemp = userRepo.save(user);
        return userTemp;
    }

    //BM - deleteUserById ---> Returns TRUE if deleted or FALSE if not
    public boolean deleteUserById(int id){
        boolean deleted = false;
        if(userRepo.existsById(id)){
            userRepo.deleteById(id);
            deleted = true;
        }
        return deleted;
    }

    //________________________________________________________________________________________________________________//
    /////////////////////////////////////////////////////////////////////////////
    //                            ADVANCED METHODS                            //
    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

    //AM - deactivateUser ---> Returns TRUE if the user is been Deactivated and FALSE if not.
    public boolean deactivateUser(int userId){
        boolean control = false;
        User tempUser;
        Shop shop;
        Optional<User> foundUser = userRepo.findById(userId);
        if (foundUser.isPresent() && foundUser.get().getStatus() == User.Status.ACTIVE){
            tempUser = foundUser.get();
            if (tempUser.getUserRole() == User.UserRole.OWNER){
                shop = shopService.findShopByOwnerId(tempUser.getId());
                shopService.deactivateShop(shop.getId());
            }
            tempUser.setStatus(User.Status.INACTIVE);
            userRepo.save(tempUser);
            control = true;
        }
        return control;
    }

    //AM - findFavoriteActiveShops --> Returns a list of shops that are user favorites.
    public List<Shop> findFavoriteActiveShops(int userid){
        User user = findUserById(userid);
        List<Shop> shops = user.getFavouriteShops();
        List<Shop> favoriteShops = new ArrayList<>();
        for(Shop shop:shops){
            if (shop.getStatus() == Shop.Status.ACTIVE){
                favoriteShops.add(shop);
            }
        }
        return favoriteShops;
    }

    //AM - addFavoriteShop ---> Returns TRUE if the shop has been added successfully and FALSE if not.
    public boolean addFavoriteShop(User user, Shop shop){
        boolean control = false;
        List<Shop> favoriteShops = user.getFavouriteShops();
        if(!favoriteShops.contains(shop)){
            favoriteShops.add(shop);
            user.setFavouriteShops(favoriteShops);
            userRepo.save(user);
            control = true;
        }
        System.out.println("Pues asi es como ha ido el add" + control);
        return control;
    }

    //AM - deleteFavoriteShop ---> Returns TRUE if the shop has been deleted successfully and FALSE if not.
    public boolean deleteFavoriteShop(User user, Shop shop){
        boolean control = false;
        List<Shop> favoriteShops = user.getFavouriteShops();
        if(favoriteShops.contains(shop)){
            favoriteShops.remove(shop);
            user.setFavouriteShops(favoriteShops);
            userRepo.save(user);
            control = true;
        }
        System.out.println("Pues asi es como ha ido el deleted" + control);
        return control;
    }




    //AM - findUserByEmail ---> Returns user matching the email or null if not
    public User findUserByEmail(String email){
        User tempUser = null;
        Optional<User> foundUser = userRepo.findUserByEmail(email);
        if(foundUser.isPresent()){
            tempUser = foundUser.get();
        }
        return tempUser;
    }

    //AM - loadUserByUsername ---> x
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Optional<User> optionalUser = userRepo.findUserByEmail(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        else {
            throw new UsernameNotFoundException(MessageFormat.format("User with email {0} cannot be found.", email));
        }
    }

    //AM - signUpUser ---> Returns user if signUp or null if not
    public User signUpUser(User user) {
        User savedUser;
        Cart newCart;
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword())); // Encrypt password
        Picture newPicture = new Picture("/images/User.png");
        Picture savedPicture = pictureService.createPicture(newPicture);
        user.setPicture(savedPicture);
        savedUser = createUser(new User(user.getAlias(), user.getName(), user.getLastName(), user.getBirthDay(), user.getEmail(), user.getPassword(), user.getPicture(), "", "", "", "", "", 0, "", "", null));
        if (savedUser!=null){
            newCart = cartService.createCart(new Cart(new ArrayList<>(), savedUser));
            VerificationToken verificationToken = verificationTokenService.createVerificationToken(savedUser);
            emailSender.sendVerification(savedUser.getEmail(), verificationToken);
        }
        return savedUser;
    }

    //AM - ValidateUser ---> Returns user if signUp or null if not
    public boolean validateUser(String token, String code){
        boolean control = false;
        VerificationToken foundVerificationToken = verificationTokenService.findVerificationTokenById(token);

        if (foundVerificationToken!=null){
            if (foundVerificationToken.getToken().equals(token) && foundVerificationToken.getCode().equals(code)){
                control = true;
                User tempUser = foundVerificationToken.getUser();
                tempUser.setEnabled(true);
                userRepo.save(tempUser);
                verificationTokenService.deleteVerificationToken(token);
            }
        }
        return control;
    }

    //AM - getLoggedUser ---> Returns user if Logged or null if not
    public User getLoggedUser(){
        User loggedUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            loggedUser = findUserByEmail(currentUserName);
        }
        return loggedUser;
    }
}
