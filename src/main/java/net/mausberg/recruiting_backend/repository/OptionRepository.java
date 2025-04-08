package net.mausberg.recruiting_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import net.mausberg.recruiting_backend.model.Option;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    Optional<Option> findById(Long id);
}