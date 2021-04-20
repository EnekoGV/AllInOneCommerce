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

    //________________________________________________________________________________________________________________//
                    /////////////////////////////////////////////////////////////////////////////
                    //                             BASIC METHODS                               //
                    ////////////////////////////////////////////////////////////////////////////
    //________________________________________________________________________________________________________________//

        //BM - findAllCategories ---> Returns a List of all Categories
    public List<Category> findAllCategories(){
        return categoryRepo.findAll();
    }

        //BM - findCategoriesById ---> Returns the category or a null object if not found
    public Category findCategoryById(int categoryId){
        Category tempCategory = null;
        Optional<Category> foundCategory = categoryRepo.findById(categoryId);
        if(foundCategory.isPresent()){
            tempCategory = foundCategory.get();
        }
        return tempCategory;
    }

        //BM - createCategory ---> Returns new Category if created or null if not
    public Category createCategory(Category newCategory){
        Category tempCategory = null;
        if(!categoryRepo.existsById(newCategory.getId())){
            tempCategory = categoryRepo.save(newCategory);
        }
        return tempCategory;
    }

        //BM - updateCategory ---> Returns updated user if ok or null if not found
    public Category updateCategory (Category category){
        Category tempCategory = null;
        if(categoryRepo.existsById(category.getId())){
            tempCategory = categoryRepo.save(category);
        }
        return tempCategory;
    }

        //BM - deleteCategoryById ---> Returns TRUE if deleted or FALSE if not
    public boolean deleteCategoryById(int categoryId){
        boolean control = false;
        if(categoryRepo.existsById(categoryId)){
            categoryRepo.deleteById(categoryId);
            control = true;
        }
        return control;
    }
}
