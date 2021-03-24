package com.telcreat.aio.service;

import com.telcreat.aio.repo.VariantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VariantService {

    private final VariantRepo variantRepo;

    @Autowired
    public VariantService(VariantRepo variantRepo){
        this.variantRepo=variantRepo;
    }
}
