package org.example.be17pickcook.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.review.service.ReviewService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
}
