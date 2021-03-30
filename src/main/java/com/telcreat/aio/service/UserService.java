package com.telcreat.aio.service;

import com.telcreat.aio.model.User;
import com.telcreat.aio.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    //BASIC method findAllUsers, returns a List of all users
    public List<User> getAllUsers(){

        return userRepo.findAll();
    }

    //BASIC method findUserById, returns de user or a null objetct if not found
    public User getUserById(int id){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findById(id);
        if(foundUser.isPresent())
            userTemp = foundUser.get();
        return userTemp;
    }

    //BASIC method createUser, returns new user if created or null if not
    public User createUser(User newUser){
        User userTemp = null;
        Optional<User> foundUser = userRepo.findUserByEmailMatches(newUser.getEmail());
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





}
