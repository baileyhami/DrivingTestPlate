package com.jiakao.ydt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiakao.ydt.dto.CoachInSchoolVO;
import com.jiakao.ydt.dto.CoachRankCardVO;
import com.jiakao.ydt.entity.SysCoach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysCoachMapper extends BaseMapper<SysCoach> {

    @Select("SELECT c.id AS coachId, u.nickname AS coachName, sch.name AS schoolName, sch.short_name AS schoolShortName, "
            + "(SELECT COALESCE(AVG(r.coach_stars), 0) FROM biz_review r WHERE r.coach_id = c.id) AS ratingAvg, "
            + "(SELECT COUNT(r.id) FROM biz_review r WHERE r.coach_id = c.id) AS ratingCount "
            + "FROM sys_coach c JOIN sys_user u ON u.id = c.user_id JOIN sys_school sch ON sch.id = c.school_id "
            + "WHERE sch.status = 1 ORDER BY ratingAvg DESC, c.id ASC")
    List<CoachRankCardVO> selectCoachPublicRankings();

    @Select("SELECT c.id AS coachId, u.nickname, u.username, c.specialty, "
            + "(SELECT COALESCE(AVG(r.coach_stars), 0) FROM biz_review r WHERE r.coach_id = c.id) AS ratingAvg, "
            + "(SELECT COUNT(r.id) FROM biz_review r WHERE r.coach_id = c.id) AS ratingCount "
            + "FROM sys_coach c JOIN sys_user u ON u.id = c.user_id WHERE c.school_id = #{schoolId}")
    List<CoachInSchoolVO> selectCoachesBySchool(@Param("schoolId") Long schoolId);
}
