package com.jiakao.ydt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiakao.ydt.dto.SchoolPublicCardVO;
import com.jiakao.ydt.entity.SysSchool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysSchoolMapper extends BaseMapper<SysSchool> {

    @Select("SELECT s.id, s.name, s.short_name AS shortName, s.city, s.address, s.contact_phone AS contactPhone, "
            + "s.contact_email AS contactEmail, s.work_hours AS workHours, s.price_c1 AS priceC1, s.price_c2 AS priceC2, "
            + "(SELECT COALESCE(AVG(r.school_stars), 0) FROM biz_review r WHERE r.school_id = s.id) AS ratingAvg, "
            + "(SELECT COUNT(r.id) FROM biz_review r WHERE r.school_id = s.id) AS ratingCount "
            + "FROM sys_school s WHERE s.status = 1 ORDER BY ratingAvg DESC, s.id ASC")
    List<SchoolPublicCardVO> selectSchoolPublicRankings();
}
