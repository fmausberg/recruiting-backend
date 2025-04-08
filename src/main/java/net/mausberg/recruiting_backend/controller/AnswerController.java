package net.mausberg.recruiting_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.mausberg.recruiting_backend.model.Answer;
import net.mausberg.recruiting_backend.model.AppUser;
import net.mausberg.recruiting_backend.service.AnswerService;
import net.mausberg.recruiting_backend.service.AppUserService;

@RestController
@RequestMapping("/api/v0/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AppUserService appUserService;

    @GetMapping("/by-loggedin-user")
    public ResponseEntity<List<Answer>> getAnswersByLoggedInUser(Authentication authentication) {
        // Get the logged-in user
        AppUser principal = appUserService.getAppUserByMail(authentication.getName());

        // Fetch all answers by the user
        List<Answer> answers = answerService.getAnswersByAppUserId(principal.getId(), principal);

        return ResponseEntity.ok(answers);
    }

    @GetMapping("/by-user/{id}")
    public ResponseEntity<List<Answer>> getAnswersByUserId(@PathVariable Long id, Authentication authentication) {
        AppUser principal = appUserService.getAppUserByMail(authentication.getName());

        // Fetch all answers by the specified user
        List<Answer> answers = answerService.getAnswersByAppUserId(id, principal);

        return ResponseEntity.ok(answers);
    }
}