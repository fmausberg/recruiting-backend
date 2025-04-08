package net.mausberg.recruiting_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mausberg.recruiting_backend.model.Answer;
import net.mausberg.recruiting_backend.model.AppUser;
import net.mausberg.recruiting_backend.repository.AnswerRepository;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    public List<Answer> getAnswersByAppUserId(Long id, AppUser principal) {
        return answerRepository.findByAppUserId(id);
    }
}