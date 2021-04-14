package com.telcreat.aio.repo;

import com.telcreat.aio.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface categoryRepo extends JpaRepository<Category, Integer> {
}
