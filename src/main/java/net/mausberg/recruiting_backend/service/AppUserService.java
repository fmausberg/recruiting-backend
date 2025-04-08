package net.mausberg.recruiting_backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import net.mausberg.recruiting_backend.config.JwtUtil;
import net.mausberg.recruiting_backend.model.*;
import net.mausberg.recruiting_backend.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService{
    
    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);
    
    private final AppUserRepository appUserRepository;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // ------------ User Creation & Operation ----------- //

    public AppUser createAppUser(AppUser parAppUser, boolean sendMail, String origin) throws MessagingException{
        AppUser appUser = appUserRepository.findByMail(parAppUser.getMail());
        
        //check if username already exists
        if (appUser != null) {
            throw new IllegalArgumentException("Email is already in use");
        }

        appUser = parAppUser;
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setRoles(Set.of("ROLE_USER"));
        
        String verificationToken = UUID.randomUUID().toString(); // This will generate a unique token
        appUser.setVerificationToken(verificationToken);
        appUser.setVerificationTokenCreatedAt(LocalDateTime.now());
        
        if (sendMail) {
            mailService.sendRegistrationEmail(appUser, origin);
        }

        return appUserRepository.save(appUser);

    }
    
    public AppUser resendVerificationMail(AppUser appUser, String origin) throws MessagingException{
        String verificationToken = UUID.randomUUID().toString(); // This will generate a unique token
        appUser.setVerificationToken(verificationToken);
        appUser.setVerificationTokenCreatedAt(LocalDateTime.now());
        
        mailService.sendRegistrationEmail(appUser, origin);

        return appUserRepository.save(appUser);
    }
    
    public AppUser updateUserAttributes(AppUser principal, AppUserDTO updatedAppuserDTO) throws Exception {
        
        AppUser appUserToBeUpdated = appUserRepository.findByUsername(principal.getUsername());
        
        if (!canUpdateAppuser(principal, appUserToBeUpdated)) {
            throw new Exception("Not allowed to change AppUser Data");
        }
        
        if (updatedAppuserDTO.getPublicName() != null) {
            appUserToBeUpdated.setPublicName(updatedAppuserDTO.getPublicName());
        }
        if (updatedAppuserDTO.getFirstName() != null) {
            appUserToBeUpdated.setFirstName(updatedAppuserDTO.getFirstName());
        }
        if (updatedAppuserDTO.getLastName() != null) {
            appUserToBeUpdated.setLastName(updatedAppuserDTO.getLastName());
        }
        if (updatedAppuserDTO.getBirthday() != null) {
            appUserToBeUpdated.setBirthday(updatedAppuserDTO.getBirthday());
        }
        if (updatedAppuserDTO.getPhone() != null) {
            appUserToBeUpdated.setPhone(updatedAppuserDTO.getPhone());
        }
        
        appUserRepository.save(appUserToBeUpdated);
        
        return appUserToBeUpdated;
    }
    
    // --------- Authentication & Authorization --------- //
    
    public boolean canCreateAppUser(Authentication authentication) {
        return true;
    }
    
    private boolean canUpdateAppuser(AppUser subject, AppUser object) {
        if (subject.getId() == object.getId()) {
            return true;
        }
        return false;
    }
    
    public AppUser authenticate(Map<String, String> credentials) {
        String mail = credentials.get("mail");
        String password = credentials.get("password");

        // Prüfen, ob die E-Mail und das Passwort angegeben wurden
        if (mail == null || password == null) {
            throw new IllegalArgumentException("Email and password must not be null.");
        }

        // Benutzer anhand der E-Mail finden
        AppUser appUser = appUserRepository.findByMail(mail);
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + mail);
        }

        // Passwort validieren
        if (!passwordEncoder.matches(password, appUser.getPassword())) {
            throw new IllegalArgumentException("Invalid password.");
        }

        return appUser; // Wenn alles passt, den Benutzer zurückgeben
    }
    
    public AppUser authenticateByPasswordResetToken(String passwordResetToken) {
        AppUser appUser = appUserRepository.findByPasswordResetToken(passwordResetToken);
        if (appUser == null) {
            throw new RuntimeException("Password Reset Token not Valid: " + passwordResetToken);
        }
        return appUser;
    }


    public String generateToken(AppUser appUser) {
        // Implement token generation logic, e.g., using JWT
        return jwtUtil.generateToken(appUser.getMail(), appUser.getRoles());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByMail(username); // Or use another method to load by email
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(appUser.getMail(), appUser.getPassword(), appUser.getAuthorities());
    }
    
    public AppUser verifyMailByToken(String token) {
        
        token = token.trim().replaceAll("^\"|\"$", "");
        
        logger.info("\tFPM: AppUserService.verifyMailByToken(" + token + ") started");		
        
        AppUser appUser = appUserRepository.findByVerificationToken(token);
        
        if (appUser == null) {
            logger.info("\tFPM: AuthController.directLogin().appUser=null");
            
            throw new EntityNotFoundException("Verification token not found.");
        }
            
        // Check if the token is not expired
        if (appUser.getVerificationTokenCreatedAt().plusDays(1).isAfter(LocalDateTime.now())) {
            logger.info("\tFPM: AuthController.directLogin(): token not expired");
            // Token is valid, mark the user as verified
            appUser.setMailVerifiedAt(LocalDateTime.now());
            appUser.setVerificationToken(null); // Remove the token after verification
            appUserRepository.save(appUser);
            
            return appUser;
        } else {
            logger.info("\tFPM: AuthController.directLogin(): token expired");
            throw new IllegalArgumentException("Invalid verification token.");
        }
    }

    public void sendPasswordResetLink(String mail, String origin) throws MessagingException {
        AppUser appUser = appUserRepository.findByMail(mail);

        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + mail);
        }

        String passwordResetToken = UUID.randomUUID().toString();
        appUser.setPasswordResetToken(passwordResetToken);
        appUser.setPasswordResetTokenCreatedAt(LocalDateTime.now());
        appUserRepository.save(appUser);

        mailService.sendPasswordResetTokenEmail(appUser, origin);
    }
    
    public AppUser resetPassword(AppUser appUser, String newPassword) {
        appUser.setPassword(passwordEncoder.encode(newPassword));
        appUser.setPasswordResetToken(null);
        appUser.setPasswordResetTokenCreatedAt(LocalDateTime.now()); //updating the date to the time, when the password was last updated
        appUserRepository.save(appUser);
        return appUser;
    }	
    
    // ------- Uitility Methods ----------//
    
    public long getAppUserCount() {
        return appUserRepository.count();
    }
    
    public AppUser getAppUserByUsername(String mail){
        return this.appUserRepository.findByUsername(mail);
    }

    public AppUser getAppUserByVerificationToken(String verificationToken){
        return this.appUserRepository.findByVerificationToken(verificationToken);
    }
    
    public AppUser getAppUserByMail(String mail){
        return this.appUserRepository.findByMail(mail);
    }

    // ------- Role-based Access Control ----------//
    
    public boolean hasPermissionToUpdate(AppUser appUser, String attribute) {
        Set<String> roles = appUser.getRoles();
        if (roles.contains("ROLE_ADMIN")) {
            return true; // Admins can update any attribute
        }
        if (roles.contains("ROLE_USER")) {
            // Users can update only specific attributes
            return Set.of("publicName", "firstName", "lastName", "birthday", "phone").contains(attribute);
        }
        return false;
    }

    public Map<String, Set<String>> getRolePermissions() {
        return Map.of(
            "ROLE_ADMIN", Set.of("publicName", "firstName", "lastName", "birthday", "phone", "mail", "password"),
            "ROLE_USER", Set.of("publicName", "firstName", "lastName", "birthday", "phone")
        );
    }

    public Object getAppUserById(Long id, AppUser principal) {
        AppUser appUser = appUserRepository.findById(id).orElse(null);

        if (appUser == null) {
            return null; // User not found
        }

        // Check if the principal matches the requested user
        if (id.equals(principal.getId())) {
            // Return full details if principal is the same as the requested user
            return new AppUserDTO(appUser);
        } else {
            // Return limited details for others
            return new AppUserDTOpublic(appUser);
        }
    }
}
