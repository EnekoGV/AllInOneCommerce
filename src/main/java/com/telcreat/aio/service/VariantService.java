package com.telcreat.aio.service;

import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.VariantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VariantService {

    private final VariantRepo variantRepo;

    @Autowired
    public VariantService(VariantRepo variantRepo) {
        this.variantRepo = variantRepo;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //BM - findAllVariants ---> Returns a List of all variants
    public List<Variant> findAllVariants() {
        return variantRepo.findAll();
    }

        //BM - findVariantById ---> Returns de Variant or a null object if not found
    public Variant findVariantById(int id) { //cambiar la funcion para que pueda buscar en cuanto al parametro STaTUS
        Variant variantTemp = null;
        Optional<Variant> foundVariant = variantRepo.findById(id);
        if (foundVariant.isPresent())
            variantTemp = foundVariant.get();
        return variantTemp;
    }

        //BM - createVariant ---> Returns new variant if created or null if not
    public Variant createVariant(Variant newVariant) {
        Variant variantTemp = null;
        if (!variantRepo.existsById(newVariant.getId())) //The id is not Autogenerated??
            variantTemp = variantRepo.save(newVariant);
        return variantTemp;
    }

        //BM - updateVariant --->  Returns updated variant if ok or null if not found
    public Variant updateVariant(Variant variant) {
        Variant variantTemp = null;
        if (variantRepo.existsById(variant.getId()))
            variantTemp = variantRepo.save(variant);
        return variantTemp;
    }

        //BM - deleteVariantById ---> Returns TRUE if deleted or FALSE if not
    public boolean deleteVariantById(int id) {
        boolean deleted = false;
        if (variantRepo.existsById(id)) {
            variantRepo.deleteById(id);
            deleted = true;
        }
        return deleted;
    }

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                            ADVANCED METHODS                            //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //AM - deactivateVariant --->
    public boolean deactivateVariant(int variantId) {
        boolean control = false;
        Variant tempVariant;
        Optional<Variant> foundVariant = variantRepo.findById(variantId);
        if (foundVariant.isPresent() && foundVariant.get().getStatus() == Variant.Status.ACTIVE) {
            tempVariant = foundVariant.get();
            tempVariant.setStatus(Variant.Status.INACTIVE);
            variantRepo.save(tempVariant);
            control = true;
        }
        return control;
    }

        //AM - findVariantsByItemId ---> Returns a List of variants based on ItemId.
    /*public List<Variant> findVariantsByItemId(int itemId){
        return variantRepo.findVariantsByItem_Id(itemId);
    }*/

        // AM - findActiveShopByOwnerId ---> Returns the Shop Object according to the specified UserId if the
        // Shop is Active.
    /*public List<Variant> findActiveVariantByItemId(int itemId){
        List<Variant> variants = findVariantsByItemId(itemId);
        List<Variant> activeVariants = null;
        if(variants != null){
            for (Variant variant:variants){
                if(variant.getStatus() == Variant.Status.ACTIVE){
                    activeVariants.add(variant);
                }
            }
        }
        return activeVariants;
    }*/

        //AM - findVariantByItemIdAndStatus ---> Returns a List of variants based on ItemId and Status.
    /*public List<Variant> findVariantByItemIdAndStatus(int itemId, String status) {
        List<Variant> arrived = variantRepo.findVariantsByItem_Id(itemId);
        List<Variant> finala = new ArrayList<>();
        for (int i = 0; i <= arrived.size(); i++) {
            if (arrived.get(i).getStatus().toString().equals(status))
                finala.add(arrived.get(i));
        }
        return finala;
    }

        //AM - deactivateVariant --->
    public boolean deactivateVariant(Variant variant) {
        boolean control = false;
        Variant tempVariant;
        Optional<Variant> foundVariant = variantRepo.findById(variant.getId());
        if (foundVariant.isPresent() && foundVariant.get().getStatus() == Variant.Status.ACTIVE) {
            tempVariant = foundVariant.get();
            tempVariant.setStatus(Variant.Status.INACTIVE);
            variantRepo.save(tempVariant);
            control = true;
        }
        return control;
    }*/
}



