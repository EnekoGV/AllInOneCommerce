package com.telcreat.aio.repo;

import com.telcreat.aio.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepo extends JpaRepository<Picture, Integer> {
}
