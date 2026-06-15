package com.jiakao.ydt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiakao.ydt.entity.BizReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface BizReviewMapper extends BaseMapper<BizReview> {

    @Select("SELECT COUNT(*) FROM biz_review WHERE student_user_id = #{uid}")
    long countByStudentUserId(@Param("uid") Long uid);

    @Select("SELECT COALESCE(AVG(school_stars), 0) FROM biz_review WHERE school_id = #{schoolId}")
    BigDecimal avgSchoolStars(@Param("schoolId") Long schoolId);

    @Select("SELECT COUNT(*) FROM biz_review WHERE school_id = #{schoolId}")
    long countSchoolReviews(@Param("schoolId") Long schoolId);
}
