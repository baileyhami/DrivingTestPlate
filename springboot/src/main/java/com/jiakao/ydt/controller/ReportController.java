package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.entity.*;
import com.jiakao.ydt.mapper.*;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据报表与导出
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final BizEnrollmentMapper enrollmentMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizMockExamMapper mockExamMapper;
    private final BizExamRegistrationMapper examRegistrationMapper;
    private final BizTrainingMapper trainingMapper;
    private final BizQuestionMapper questionMapper;
    private final SysUserMapper userMapper;
    private final SysStudentMapper studentMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public R<Map<String, Object>> dashboard() {
        SecurityUser u = SecurityUtils.requireUser();
        Long schoolId = Roles.SCHOOL.equals(u.getRoleCode()) ? u.getSchoolId() : null;

        Map<String, Object> m = new HashMap<>();
        m.put("enrollmentByStatus", countEnrollmentByStatus(schoolId));
        m.put("appointmentByStatus", countAppointmentByStatus(schoolId));
        m.put("examRegByStatus", countExamRegByStatus(schoolId));
        m.put("studentTotal", countStudents(schoolId));
        m.put("mockAvgScore", avgMockScore(schoolId));
        return R.ok(m);
    }

    private Map<String, Long> countEnrollmentByStatus(Long schoolId) {
        LambdaQueryWrapper<BizEnrollment> q = new LambdaQueryWrapper<>();
        if (schoolId != null) {
            q.eq(BizEnrollment::getSchoolId, schoolId);
        }
        List<BizEnrollment> list = enrollmentMapper.selectList(q);
        return list.stream().collect(Collectors.groupingBy(BizEnrollment::getStatus, Collectors.counting()));
    }

    private Map<String, Long> countAppointmentByStatus(Long schoolId) {
        LambdaQueryWrapper<BizAppointment> q = new LambdaQueryWrapper<>();
        if (schoolId != null) {
            q.eq(BizAppointment::getSchoolId, schoolId);
        }
        List<BizAppointment> list = appointmentMapper.selectList(q);
        return list.stream().collect(Collectors.groupingBy(BizAppointment::getStatus, Collectors.counting()));
    }

    private Map<String, Long> countExamRegByStatus(Long schoolId) {
        LambdaQueryWrapper<BizExamRegistration> q = new LambdaQueryWrapper<>();
        if (schoolId != null) {
            q.eq(BizExamRegistration::getSchoolId, schoolId);
        }
        List<BizExamRegistration> list = examRegistrationMapper.selectList(q);
        return list.stream().collect(Collectors.groupingBy(BizExamRegistration::getStatus, Collectors.counting()));
    }

    private long countStudents(Long schoolId) {
        LambdaQueryWrapper<SysStudent> q = new LambdaQueryWrapper<>();
        if (schoolId != null) {
            q.eq(SysStudent::getSchoolId, schoolId);
        }
        return studentMapper.selectCount(q);
    }

    private double avgMockScore(Long schoolId) {
        if (schoolId == null) {
            List<BizMockExam> list = mockExamMapper.selectList(new LambdaQueryWrapper<>());
            return list.stream().mapToInt(BizMockExam::getScore).average().orElse(0);
        }
        List<SysStudent> sts = studentMapper.selectList(new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getSchoolId, schoolId));
        if (sts.isEmpty()) {
            return 0;
        }
        List<Long> uids = sts.stream().map(SysStudent::getUserId).collect(Collectors.toList());
        List<BizMockExam> exams = mockExamMapper.selectList(new LambdaQueryWrapper<BizMockExam>().in(BizMockExam::getUserId, uids));
        return exams.stream().mapToInt(BizMockExam::getScore).average().orElse(0);
    }

    @GetMapping("/export/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public ResponseEntity<byte[]> exportEnrollments() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizEnrollment> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizEnrollment::getSchoolId, u.getSchoolId());
        }
        List<BizEnrollment> list = enrollmentMapper.selectList(q);
        StringBuilder sb = new StringBuilder();
        sb.append("id,schoolId,applicantName,phone,status,source,createTime\n");
        for (BizEnrollment e : list) {
            sb.append(e.getId()).append(',')
                    .append(e.getSchoolId()).append(',')
                    .append(csv(e.getApplicantName())).append(',')
                    .append(csv(e.getPhone())).append(',')
                    .append(e.getStatus()).append(',')
                    .append(csv(e.getSource())).append(',')
                    .append(e.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=enrollments.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
    @GetMapping("/export/appointments")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public ResponseEntity<byte[]> exportAppointments() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizAppointment> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizAppointment::getSchoolId, u.getSchoolId());
        }
        List<BizAppointment> list = appointmentMapper.selectList(q);
        StringBuilder sb = new StringBuilder();
        sb.append("id,schoolId,studentId,coachId,subjectType,appointmentTime,status,remark,createTime\n");
        for (BizAppointment a : list) {
            sb.append(a.getId()).append(',')
                    .append(a.getSchoolId()).append(',')
                    .append(a.getStudentId()).append(',')
                    .append(a.getCoachId()).append(',')
                    .append(a.getSubjectType()).append(',')
                    .append(a.getAppointmentTime()).append(',')
                    .append(a.getStatus()).append(',')
                    .append(csv(a.getRemark())).append(',')
                    .append(a.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=appointments.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/export/trainings")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public ResponseEntity<byte[]> exportTrainings() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizTraining> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizTraining::getSchoolId, u.getSchoolId());
        }
        List<BizTraining> list = trainingMapper.selectList(q);
        StringBuilder sb = new StringBuilder();
        sb.append("id,appointmentId,schoolId,studentId,coachId,subjectType,trainingDate,checkInTime,durationHours,score,content,createTime\n");
        for (BizTraining t : list) {
            sb.append(t.getId()).append(',')
                    .append(t.getAppointmentId()).append(',')
                    .append(t.getSchoolId()).append(',')
                    .append(t.getStudentId()).append(',')
                    .append(t.getCoachId()).append(',')
                    .append(t.getSubjectType()).append(',')
                    .append(t.getTrainingDate()).append(',')
                    .append(t.getCheckInTime()).append(',')
                    .append(t.getDurationHours()).append(',')
                    .append(t.getScore()).append(',')
                    .append(csv(t.getContent())).append(',')
                    .append(t.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trainings.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/export/exam-registrations")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public ResponseEntity<byte[]> exportExamRegistrations() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<BizExamRegistration> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(BizExamRegistration::getSchoolId, u.getSchoolId());
        }
        List<BizExamRegistration> list = examRegistrationMapper.selectList(q);
        StringBuilder sb = new StringBuilder();
        sb.append("id,schoolId,studentId,venueId,examSubject,examDate,status,remark,createTime\n");
        for (BizExamRegistration r : list) {
            sb.append(r.getId()).append(',')
                    .append(r.getSchoolId()).append(',')
                    .append(r.getStudentId()).append(',')
                    .append(r.getVenueId()).append(',')
                    .append(r.getExamSubject()).append(',')
                    .append(r.getExamDate()).append(',')
                    .append(r.getStatus()).append(',')
                    .append(csv(r.getRemark())).append(',')
                    .append(r.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exam_registrations.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/export/questions")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<byte[]> exportQuestions() {
        // only global ADMIN can export question bank
        List<BizQuestion> list = questionMapper.selectList(new LambdaQueryWrapper<>());
        StringBuilder sb = new StringBuilder();
        sb.append("id,subject,qType,title,optionA,optionB,optionC,optionD,answer,analysis,imageUrl,chapter,createTime\n");
        for (BizQuestion q : list) {
            sb.append(q.getId()).append(',')
                    .append(q.getSubject()).append(',')
                    .append(q.getQType()).append(',')
                    .append(csv(q.getTitle())).append(',')
                    .append(csv(q.getOptionA())).append(',')
                    .append(csv(q.getOptionB())).append(',')
                    .append(csv(q.getOptionC())).append(',')
                    .append(csv(q.getOptionD())).append(',')
                    .append(q.getAnswer()).append(',')
                    .append(csv(q.getAnalysis())).append(',')
                    .append(csv(q.getImageUrl())).append(',')
                    .append(csv(q.getChapter())).append(',')
                    .append(q.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questions.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/export/users")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL')")
    public ResponseEntity<byte[]> exportUsers() {
        SecurityUser u = SecurityUtils.requireUser();
        LambdaQueryWrapper<com.jiakao.ydt.entity.SysUser> q = new LambdaQueryWrapper<>();
        if (Roles.SCHOOL.equals(u.getRoleCode()) && u.getSchoolId() != null) {
            q.eq(com.jiakao.ydt.entity.SysUser::getSchoolId, u.getSchoolId());
        }
        List<com.jiakao.ydt.entity.SysUser> list = userMapper.selectList(q);
        StringBuilder sb = new StringBuilder();
        sb.append("id,username,nickname,phone,email,roleCode,schoolId,status,createTime\n");
        for (com.jiakao.ydt.entity.SysUser s : list) {
            sb.append(s.getId()).append(',')
                    .append(csv(s.getUsername())).append(',')
                    .append(csv(s.getNickname())).append(',')
                    .append(csv(s.getPhone())).append(',')
                    .append(csv(s.getEmail())).append(',')
                    .append(s.getRoleCode()).append(',')
                    .append(s.getSchoolId()).append(',')
                    .append(s.getStatus()).append(',')
                    .append(s.getCreateTime()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private String csv(String s) {
        if (s == null) {
            return "";
        }
        String x = s.replace("\"", "\"\"");
        if (x.contains(",") || x.contains("\"") || x.contains("\n")) {
            return "\"" + x + "\"";
        }
        return x;
    }
}
