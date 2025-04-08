package net.mausberg.recruiting_backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mausberg.recruiting_backend.model.Question;
import net.mausberg.recruiting_backend.repository.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }
}