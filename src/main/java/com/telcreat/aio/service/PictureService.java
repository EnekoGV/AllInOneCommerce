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

    // BASIC method
    public List<Picture> findAllPictures (){
        return pictureRepo.findAll();
    }

    // BASIC method
    public Picture findPictureById (int pictureId){
        Picture tempPicture = null;
        Optional<Picture> foundPicture = pictureRepo.findById(pictureId);
        if (foundPicture.isPresent()){
            tempPicture = foundPicture.get();
        }
        return tempPicture;
    }

    // BASIC method
    public Picture createPicture (Picture newPicture){
        Picture tempPicture = null;
        if (!pictureRepo.existsById(newPicture.getId())){
            tempPicture = pictureRepo.save(newPicture);
        }
        return tempPicture;
    }

    // BASIC method
    public boolean deletePictureById (int pictureId){
        boolean control = false;
        if (pictureRepo.existsById(pictureId)){
            pictureRepo.deleteById(pictureId);
            control = true;
        }
        return control;
    }

    // BASIC method
    public Picture updatePicture (Picture updatePicture){
        Picture tempPicture = null;
        if (pictureRepo.existsById(updatePicture.getId())){
            tempPicture = pictureRepo.save(updatePicture);
        }
        return tempPicture;
    }
}
