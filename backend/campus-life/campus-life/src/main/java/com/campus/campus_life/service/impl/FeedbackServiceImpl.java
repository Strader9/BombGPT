package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Feedback;
import com.campus.campus_life.mapper.FeedbackMapper;
import com.campus.campus_life.service.FeedbackService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;

    @Override
    public void submit(Feedback feedback) {
        feedbackMapper.insert(feedback);
    }

    @Override
    public List<Feedback> listAll() {
        return feedbackMapper.listAll();
    }

    @Override
    public List<Feedback> listByUsername(String username) {
        return feedbackMapper.listByUsername(username);
    }

    @Override
    public void reply(Long id, String reply) {
        feedbackMapper.reply(id, reply);
    }
}