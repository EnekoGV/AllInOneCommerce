package com.telcreat.aio.repo;

import com.telcreat.aio.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepo extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findVerificationTokenByUser_id(int userId);
}
