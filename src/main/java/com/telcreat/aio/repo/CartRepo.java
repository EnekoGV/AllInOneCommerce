package com.telcreat.aio.repo;

import com.telcreat.aio.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {
    Optional<Cart> findCartByUserId(int userId);
}
