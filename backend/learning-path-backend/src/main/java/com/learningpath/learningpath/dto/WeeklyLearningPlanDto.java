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
public class WeeklyLearningPlanDto {
    private Integer weekNumber;
    private Integer weeklyTargetMinutes;
    private Integer scheduledMinutes;
    private String focusTopic;
    private List<WeeklyDayScheduleDto> days;
}
