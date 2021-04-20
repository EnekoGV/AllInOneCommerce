package com.telcreat.aio.service;

import com.telcreat.aio.model.Picture;
import com.telcreat.aio.model.User;
import com.telcreat.aio.model.VerificationToken;
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
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final VerificationTokenRepo verificationTokenRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final SendEmail emailSender;
    private final PictureService pictureService;


    @Autowired
    public UserService(UserRepo userRepo, VerificationTokenService verificationTokenService, VerificationTokenRepo verificationTokenRepo, PictureService pictureService){
        this.userRepo = userRepo;
        bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.verificationTokenService = verificationTokenService;
        emailSender = new SendEmail();
        this.verificationTokenRepo = verificationTokenRepo;
        this.pictureService = pictureService;
    }

    //BASIC method findAllUsers, returns a List of all users
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    //BASIC method findUserById, returns de user or a null object if not found
    public User findUserById(int id){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findById(id);
        if(foundUser.isPresent())
            userTemp = foundUser.get();
        return userTemp;
    }

    //BASIC method createUser, returns new user if created or null if not
    public User createUser(User newUser){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findUserByEmail(newUser.getEmail());
        if(!userRepo.existsById(newUser.getId()) && !foundUser.isPresent())
            userTemp = userRepo.save(newUser);
        return userTemp;
    }

    //BASIC method updateUser, returns updated user if ok or null if not found
    public User updateUser(User user){
        User userTemp = null;
        if(userRepo.existsById(user.getId()))
            userTemp = userRepo.save(user);
        return userTemp;
    }

    //BASIC method deleteUser, returns TRUE if deleted or FALSE if not
    public boolean deleteUserById(int id){
        boolean deleted = false;
        if(userRepo.existsById(id)){
            userRepo.deleteById(id);
            deleted = true;
        }
        return deleted;
    }

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

    public User signUpUser(User user) {
        User savedUser;

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword())); // Encrypt password
        Picture newPicture = new Picture("");
        Picture savedPicture = pictureService.createPicture(newPicture);
        user.setPicture(savedPicture);
        savedUser = createUser(new User(user.getAlias(), user.getName(), user.getLastName(), user.getBirthDay(), user.getEmail(), user.getPassword(), user.getPicture(), "", "", "", "", "", 0, "", "", null));
        if (savedUser!=null){
            VerificationToken verificationToken = verificationTokenService.createVerificationToken(savedUser);
            emailSender.send(savedUser.getEmail(), verificationToken);
        }
        return savedUser;
    }

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

    public User getLoggedUser(){
        User loggedUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            loggedUser = findUserByEmail(currentUserName);
        }
        return loggedUser;
    }

    public User findUserByEmail(String email){
        User tempUser = null;
        Optional<User> foundUser = userRepo.findUserByEmail(email);
        if(foundUser.isPresent()){
            tempUser = foundUser.get();
        }
        return tempUser;
    }

}
