package com.telcreat.aio.service;

import com.telcreat.aio.model.Category;
import com.telcreat.aio.repo.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    @Autowired
    public CategoryService(CategoryRepo categoryRepo){
        this.categoryRepo = categoryRepo;
    }

    // Basic Methods (BM)
    // BM - Find All Categories.
    public List<Category> findAllCategories(){
        return categoryRepo.findAll();
    }

    // BM - Find Categories by ID.
    public Category findCategoryById(int categoryId){
        Category tempCategory = null;
        Optional<Category> foundCategory = categoryRepo.findById(categoryId);
        if(foundCategory.isPresent()){
            tempCategory = foundCategory.get();
        }
        return tempCategory;
    }

    // BM - Create a new category
    public Category createCategory(Category newCategory){
        Category tempCategory = null;
        if(!categoryRepo.existsById(newCategory.getId())){
            tempCategory = categoryRepo.save(newCategory);
        }
        return tempCategory;
    }

    // BM - Update a Category
    public Category updateCategory (Category category){
        Category tempCategory = null;
        if(categoryRepo.existsById(category.getId())){
            tempCategory = categoryRepo.save(category);
        }
        return tempCategory;
    }

    // BM - Delete a Category
    public boolean deleteCategoryById(int categoryId){
        boolean control = false;
        if(categoryRepo.existsById(categoryId)){
            categoryRepo.deleteById(categoryId);
            control = true;
        }
        return control;
    }
}
