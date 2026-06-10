package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.common.Roles;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.dto.CoachStudentVO;
import com.jiakao.ydt.entity.BizAppointment;
import com.jiakao.ydt.entity.BizTraining;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizAppointmentMapper;
import com.jiakao.ydt.mapper.BizTrainingMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import com.jiakao.ydt.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachStudentService {

    private final SysCoachMapper coachMapper;
    private final SysStudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final BizTrainingMapper trainingMapper;

    public List<CoachStudentVO> listMyStudents(SecurityUser u) {
        if (!Roles.COACH.equals(u.getRoleCode())) {
            throw new BusinessException("仅教练可查看");
        }
        SysCoach coach = coachMapper.selectOne(new LambdaQueryWrapper<SysCoach>().eq(SysCoach::getUserId, u.getId()));
        if (coach == null) {
            return List.of();
        }
        List<SysStudent> studs = studentMapper.selectList(new LambdaQueryWrapper<SysStudent>()
                .eq(SysStudent::getCoachId, coach.getId())
                .orderByDesc(SysStudent::getId));
        if (studs.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = studs.stream().map(SysStudent::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream().collect(Collectors.toMap(SysUser::getId, x -> x));
        List<Long> studentPks = studs.stream().map(SysStudent::getId).collect(Collectors.toList());
        List<BizAppointment> appts = appointmentMapper.selectList(new LambdaQueryWrapper<BizAppointment>()
                .in(BizAppointment::getStudentId, studentPks)
                .orderByDesc(BizAppointment::getAppointmentTime));
        List<BizTraining> trains = trainingMapper.selectList(new LambdaQueryWrapper<BizTraining>()
                .in(BizTraining::getStudentId, studentPks)
                .orderByDesc(BizTraining::getTrainingDate));
        Map<Long, List<BizAppointment>> apptByStudent = appts.stream().collect(Collectors.groupingBy(BizAppointment::getStudentId));
        Map<Long, List<BizTraining>> trainByStudent = trains.stream().collect(Collectors.groupingBy(BizTraining::getStudentId));

        List<CoachStudentVO> out = new ArrayList<>();
        for (SysStudent st : studs) {
            CoachStudentVO vo = new CoachStudentVO();
            vo.setStudentId(st.getId());
            SysUser usr = userMap.get(st.getUserId());
            if (usr != null) {
                String n = usr.getNickname();
                if (n == null || n.isBlank()) {
                    n = usr.getUsername();
                }
                vo.setNickname(n);
                vo.setPhone(usr.getPhone());
                vo.setEmail(usr.getEmail());
            }
            vo.setLearningStage(st.getLearningStage());
            List<BizAppointment> sa = apptByStudent.getOrDefault(st.getId(), List.of());
            vo.setAppointmentBooked(sa.stream().filter(a -> "BOOKED".equalsIgnoreCase(a.getStatus())).count());
            vo.setAppointmentCheckedIn(sa.stream().filter(a -> "CHECKED_IN".equalsIgnoreCase(a.getStatus())).count());
            vo.setAppointmentCompleted(sa.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count());
            List<BizTraining> stt = trainByStudent.getOrDefault(st.getId(), List.of());
            vo.setTrainingCount(stt.size());
            vo.setRecentAppointments(sa.stream().limit(5).map(a -> {
                CoachStudentVO.AppointmentBrief b = new CoachStudentVO.AppointmentBrief();
                b.setId(a.getId());
                b.setSubjectType(a.getSubjectType());
                b.setAppointmentTime(a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : null);
                b.setStatus(a.getStatus());
                return b;
            }).collect(Collectors.toList()));
            vo.setRecentTrainings(stt.stream().limit(5).map(t -> {
                CoachStudentVO.TrainingBrief b = new CoachStudentVO.TrainingBrief();
                b.setId(t.getId());
                b.setSubjectType(t.getSubjectType());
                b.setTrainingDate(t.getTrainingDate() != null ? t.getTrainingDate().toString() : null);
                b.setDurationHours(t.getDurationHours() != null ? t.getDurationHours().toPlainString() : null);
                b.setScore(t.getScore());
                return b;
            }).collect(Collectors.toList()));
            out.add(vo);
        }
        return out;
    }
}
