package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiakao.ydt.entity.BizAppointment;
import com.jiakao.ydt.entity.BizExamRegistration;
import com.jiakao.ydt.entity.BizExamVenue;
import com.jiakao.ydt.entity.BizMockExam;
import com.jiakao.ydt.entity.BizTraining;
import com.jiakao.ydt.entity.SysCoach;
import com.jiakao.ydt.entity.SysStudent;
import com.jiakao.ydt.entity.SysUser;
import com.jiakao.ydt.mapper.BizExamVenueMapper;
import com.jiakao.ydt.mapper.SysCoachMapper;
import com.jiakao.ydt.mapper.SysStudentMapper;
import com.jiakao.ydt.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 列表展示用：将学员/教练档案 ID 解析为账号昵称（无昵称则回退用户名）。
 */
@Service
@RequiredArgsConstructor
public class UserDisplayService {

    private final SysStudentMapper studentMapper;
    private final SysCoachMapper coachMapper;
    private final SysUserMapper userMapper;
    private final BizExamVenueMapper venueMapper;

    public void fillExamRegistrationStudents(List<BizExamRegistration> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> ids = rows.stream().map(BizExamRegistration::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nick = studentPkToNickname(ids);
        Set<Long> venueIds = rows.stream().map(BizExamRegistration::getVenueId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> venueNames = venuePkToName(venueIds);
        for (BizExamRegistration r : rows) {
            r.setStudentNickname(nick.getOrDefault(r.getStudentId(), ""));
            r.setVenueName(venueNames.getOrDefault(r.getVenueId(), ""));
        }
    }

    public void fillMockExamStudents(List<BizMockExam> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> userIds = rows.stream().map(BizMockExam::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nick = loadNickByUserIds(userIds);
        for (BizMockExam m : rows) {
            m.setStudentNickname(nick.getOrDefault(m.getUserId(), ""));
        }
    }

    private Map<Long, String> venuePkToName(Set<Long> venueIds) {
        if (venueIds == null || venueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BizExamVenue> venues = venueMapper.selectList(
                new LambdaQueryWrapper<BizExamVenue>().in(BizExamVenue::getId, venueIds));
        Map<Long, String> out = new HashMap<>();
        for (BizExamVenue v : venues) {
            out.put(v.getId(), v.getName() != null ? v.getName() : "");
        }
        return out;
    }

    public void fillAppointmentParties(List<BizAppointment> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> sids = rows.stream().map(BizAppointment::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> cids = rows.stream().map(BizAppointment::getCoachId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> sn = studentPkToNickname(sids);
        Map<Long, String> cn = coachPkToNickname(cids);
        for (BizAppointment a : rows) {
            a.setStudentNickname(sn.getOrDefault(a.getStudentId(), ""));
            a.setCoachNickname(cn.getOrDefault(a.getCoachId(), ""));
        }
    }

    public void fillTrainingParties(List<BizTraining> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> sids = rows.stream().map(BizTraining::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> cids = rows.stream().map(BizTraining::getCoachId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> sn = studentPkToNickname(sids);
        Map<Long, String> cn = coachPkToNickname(cids);
        for (BizTraining t : rows) {
            t.setStudentNickname(sn.getOrDefault(t.getStudentId(), ""));
            t.setCoachNickname(cn.getOrDefault(t.getCoachId(), ""));
        }
    }

    private Map<Long, String> studentPkToNickname(Set<Long> studentPks) {
        if (studentPks == null || studentPks.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysStudent> studs = studentMapper.selectList(
                new LambdaQueryWrapper<SysStudent>().in(SysStudent::getId, studentPks));
        return mapStudentRowsToNick(studs);
    }

    private Map<Long, String> coachPkToNickname(Set<Long> coachPks) {
        if (coachPks == null || coachPks.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysCoach> coaches = coachMapper.selectList(
                new LambdaQueryWrapper<SysCoach>().in(SysCoach::getId, coachPks));
        if (coaches.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = coaches.stream().map(SysCoach::getUserId).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        Map<Long, String> u2n = loadNickByUserIds(userIds);
        Map<Long, String> out = new HashMap<>();
        for (SysCoach c : coaches) {
            if (c.getUserId() != null) {
                out.put(c.getId(), u2n.getOrDefault(c.getUserId(), ""));
            }
        }
        return out;
    }

    private Map<Long, String> mapStudentRowsToNick(List<SysStudent> studs) {
        if (studs == null || studs.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = studs.stream().map(SysStudent::getUserId).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        Map<Long, String> u2n = loadNickByUserIds(userIds);
        Map<Long, String> out = new HashMap<>();
        for (SysStudent st : studs) {
            if (st.getUserId() != null) {
                out.put(st.getId(), u2n.getOrDefault(st.getUserId(), ""));
            }
        }
        return out;
    }

    private Map<Long, String> loadNickByUserIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
        Map<Long, String> out = new HashMap<>();
        for (SysUser u : users) {
            String n = u.getNickname();
            if (n == null || n.isBlank()) {
                n = u.getUsername() != null ? u.getUsername() : "";
            }
            out.put(u.getId(), n);
        }
        return out;
    }
}
