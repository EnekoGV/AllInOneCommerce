package com.telcreat.aio.repo;

import com.telcreat.aio.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantRepo extends JpaRepository<Variant,Integer> {
}
