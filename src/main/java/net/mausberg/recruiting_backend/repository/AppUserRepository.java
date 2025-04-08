package net.mausberg.recruiting_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.mausberg.recruiting_backend.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	
	AppUser findByUsername(String username);

	AppUser findByMail(String mail);

	AppUser findByVerificationToken(String token);

	AppUser findByPasswordResetToken(String verificationLink);
}
