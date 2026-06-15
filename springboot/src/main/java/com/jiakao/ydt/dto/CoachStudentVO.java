package com.jiakao.ydt.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoachStudentVO {
    private Long studentId;
    private String nickname;
    private String phone;
    private String email;
    private Integer learningStage;
    private long appointmentBooked;
    private long appointmentCheckedIn;
    private long appointmentCompleted;
    private long trainingCount;
    private List<AppointmentBrief> recentAppointments;
    private List<TrainingBrief> recentTrainings;

    @Data
    public static class AppointmentBrief {
        private Long id;
        private String subjectType;
        private String appointmentTime;
        private String status;
    }

    @Data
    public static class TrainingBrief {
        private Long id;
        private String subjectType;
        private String trainingDate;
        private String durationHours;
        private Integer score;
    }
}
