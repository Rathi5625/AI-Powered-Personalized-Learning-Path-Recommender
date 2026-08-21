package com.learningpath.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathChangeHistoryDto {
    private Integer version;
    private Instant timestamp;
    private String reason;
    private String explanation;
    private Double overallProgress;
}
