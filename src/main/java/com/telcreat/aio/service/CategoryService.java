package com.telcreat.aio.service;

import com.telcreat.aio.model.Category;
import com.telcreat.aio.repo.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    @Autowired
    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<Category> findAllCategories(){
        return categoryRepo.findAll();
    }
}
