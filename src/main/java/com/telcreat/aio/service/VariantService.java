package com.telcreat.aio.service;

import com.telcreat.aio.model.User;
import com.telcreat.aio.model.Variant;
import com.telcreat.aio.repo.VariantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VariantService {

    private final VariantRepo variantRepo;

    @Autowired
    public VariantService(VariantRepo variantRepo) {
        this.variantRepo = variantRepo;
    }

    //BASIC method findAllVariants, returns a List of all variants
    public List<Variant> findAllVariants() {
        return variantRepo.findAll();
    }

    public List<Variant> findVariantsByItemId(int itemId){
        return variantRepo.findVariantsByItem_Id(itemId);
    }


    public List<Variant> findVariantByItemIdAndStatus(int itemId, String status) {
        List<Variant> arrived = variantRepo.findVariantsByItem_Id(itemId);
        List<Variant> finala = new ArrayList<>();
        for (int i = 0; i <= arrived.size(); i++) {
            if (arrived.get(i).getStatus().toString().equals(status))
                finala.add(arrived.get(i));
        }
        return finala;
    }

    //BASIC method findVariantById, returns de Variant or a null object if not found
    public Variant findVariantById(int id) { //cambiar la funcion para que pueda buscar en cuanto al parametro STaTUS
        Variant variantTemp = null;
        Optional<Variant> foundVariant = variantRepo.findById(id);
        if (foundVariant.isPresent())
            variantTemp = foundVariant.get();
        return variantTemp;
    }

    //BASIC method createUser, returns new variant if created or null if not
    public Variant createVariant(Variant newVariant) {
        Variant variantTemp = null;
        if (!variantRepo.existsById(newVariant.getId())) //The id is not Autogenerated??
            variantTemp = variantRepo.save(newVariant);
        return variantTemp;
    }

    //BASIC method updateVariant, returns updated variant if ok or null if not found
    public Variant updateVariant(Variant variant) {
        Variant variantTemp = null;
        if (variantRepo.existsById(variant.getId()))
            variantTemp = variantRepo.save(variant);
        return variantTemp;
    }

    //BASIC method deleteVariantById, returns TRUE if deleted or FALSE if not
    public boolean deleteVariantById(int id) {
        boolean deleted = false;
        if (variantRepo.existsById(id)) {
            variantRepo.deleteById(id);
            deleted = true;
        }
        return deleted;
    }


    public Variant deactivateVariant(Variant variant) {
        Variant variantTemp = null;
        if (variantRepo.existsById(variant.getId()) && variant.getStatus().toString().equals("ACTIVE")) {
            variantTemp = variant;
            variantTemp.setStatus(Variant.Status.valueOf("INACTIVE"));
            variantRepo.save(variantTemp);
        }
        return variantTemp;
    }
}



