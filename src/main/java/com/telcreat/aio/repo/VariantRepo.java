package com.telcreat.aio.repo;

import com.telcreat.aio.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepo extends JpaRepository<Variant,Integer> {
    List<Variant> findVariantsByItem_Id(int itemId);
    List<Variant> findVariantsByItem_Shop_IdAndStatus(int shopId, Variant.Status variantStatus);
    Optional<Variant> findVariantByIdAndStatus(int variantId, Variant.Status variantStatus);
}
