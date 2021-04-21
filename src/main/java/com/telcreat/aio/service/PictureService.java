package com.telcreat.aio.service;

import com.telcreat.aio.model.Picture;
import com.telcreat.aio.repo.PictureRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PictureService {

    private final PictureRepo pictureRepo;

    @Autowired
    public PictureService(PictureRepo pictureRepo) {
        this.pictureRepo = pictureRepo;
    }

    //________________________________________________________________________________________________________________//
                /////////////////////////////////////////////////////////////////////////////
                //                             BASIC METHODS                               //
                ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //BM - findAllPictures ---> Returns a List of all pictures
    public List<Picture> findAllPictures (){
        return pictureRepo.findAll();
    }

        //BM - findPictureById --->
    public Picture findPictureById (int pictureId){
        Picture tempPicture = null;
        Optional<Picture> foundPicture = pictureRepo.findById(pictureId);
        if (foundPicture.isPresent()){
            tempPicture = foundPicture.get();
        }
        return tempPicture;
    }

        //BM - createPicture ---> Returns new picture if created or null if not
    public Picture createPicture (Picture newPicture){
        Picture tempPicture = null;
        if (!pictureRepo.existsById(newPicture.getId())){
            tempPicture = pictureRepo.save(newPicture);
        }
        return tempPicture;
    }

        //BM - updatePicture ---> Returns updated picture if ok or null if not found
    public Picture updatePicture (Picture updatePicture){
        Picture tempPicture = null;
        if (pictureRepo.existsById(updatePicture.getId())){
            tempPicture = pictureRepo.save(updatePicture);
        }
        return tempPicture;
    }

        //BM - deletePictureById ---> Returns TRUE if deleted or FALSE if not
    public boolean deletePictureById (int pictureId){
        boolean control = false;
        if (pictureRepo.existsById(pictureId)){
            pictureRepo.deleteById(pictureId);
            control = true;
        }
        return control;
    }
}
