package com.telcreat.aio.service;

import com.telcreat.aio.model.User;
import com.telcreat.aio.model.VerificationToken;
import com.telcreat.aio.repo.VerificationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepo verificationTokenRepo;

    @Autowired
    public VerificationTokenService(VerificationTokenRepo verificationTokenRepo) {
        this.verificationTokenRepo = verificationTokenRepo;
    }

        //deleteVerificationToken --->
    public boolean deleteVerificationToken(String verificationToken){
        boolean control = false;
        if(verificationTokenRepo.existsById(verificationToken)){
            verificationTokenRepo.deleteById(verificationToken);
            control = true;
        }
        return control;
    }

        //createVerificationToken --->
    public VerificationToken createVerificationToken(User user){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 15;
        Random random = new Random();

        String token = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String code = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        System.out.println(token);
        System.out.println(code);
        VerificationToken tempVerificationToken = null;
        VerificationToken newVerificationToken = new VerificationToken(token, code, user);

        if (!verificationTokenRepo.existsById(newVerificationToken.getToken())){
            tempVerificationToken = verificationTokenRepo.save(newVerificationToken);
        }

        return tempVerificationToken;
    }

        //findTokenByUserId --->
    public String findTokenByUserId(int userId){
        String token = null;
        Optional<VerificationToken> foundVerificationToken = verificationTokenRepo.findVerificationTokenByUser_id(userId);
        if (foundVerificationToken.isPresent()){
            token = foundVerificationToken.get().getToken();
        }
        return token;
    }

        //findVerificationTokenById --->
    public VerificationToken findVerificationTokenById(String token){
        VerificationToken tempVerificationToken = null;
        Optional<VerificationToken> foundVerificationToken = verificationTokenRepo.findById(token);
        if (foundVerificationToken.isPresent()){
            tempVerificationToken = foundVerificationToken.get();
        }
        return tempVerificationToken;
    }
}
