package net.mausberg.recruiting_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.mausberg.recruiting_backend.model.Answer;
import net.mausberg.recruiting_backend.model.AppUser;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findById(Long id);
    List<Answer> findByAppUser(AppUser appUser);
    List<Answer> findByAppUserId(Long appUserId); // Fetch answers by AppUser ID
}