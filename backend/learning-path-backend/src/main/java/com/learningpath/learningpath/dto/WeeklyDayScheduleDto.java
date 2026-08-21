package com.learningpath.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyDayScheduleDto {
    private String dayName;
    private Integer dayIndex;
    private Integer allocatedMinutes;
    private List<LearningPathNodeDto> activities;
}
