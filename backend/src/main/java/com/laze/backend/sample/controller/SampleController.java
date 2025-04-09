package com.laze.backend.sample.controller;

import com.laze.backend.sample.mapper.SampleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class SampleController {

    private static final Logger log = LoggerFactory.getLogger(SampleController.class);

    private final SampleMapper sampleMapper;

    @Autowired
    public SampleController(SampleMapper sampleMapper) {
        this.sampleMapper = sampleMapper;
    }

    @GetMapping
    public String getCurrentTime() {
        return sampleMapper.getCurrentTime();
    }
}
