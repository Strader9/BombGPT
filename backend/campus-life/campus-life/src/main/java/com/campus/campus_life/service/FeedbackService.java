package com.campus.campus_life.service;

import com.campus.campus_life.entity.Feedback;

import java.util.List;

public interface FeedbackService {

    void submit(Feedback feedback);

    List<Feedback> listAll();

    List<Feedback> listByUsername(String username);

    void reply(Long id, String reply);
}