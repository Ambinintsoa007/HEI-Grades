package com.example.demo.model;

import com.example.demo.endpoint.rest.dto.GradeResponse;

public record SaveGradeResult(GradeResponse grade, boolean created) {}
