package com.telcreat.aio.repo;

import com.telcreat.aio.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepo  extends JpaRepository<Item, Integer> {
}
