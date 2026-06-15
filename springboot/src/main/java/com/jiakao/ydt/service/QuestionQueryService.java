package com.jiakao.ydt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.entity.BizQuestion;
import com.jiakao.ydt.mapper.BizQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionQueryService {

    private final BizQuestionMapper questionMapper;

    @Cacheable(cacheNames = RedisCacheConfig.QUESTIONS_SUBJECT_ALL, key = "#subject")
    public List<BizQuestion> listBySubject(Integer subject) {
        return questionMapper.selectList(new LambdaQueryWrapper<BizQuestion>()
                .eq(BizQuestion::getSubject, subject)
                .orderByAsc(BizQuestion::getId));
    }

    @Cacheable(cacheNames = RedisCacheConfig.QUESTIONS_BY_IDS, key = "T(com.jiakao.ydt.service.QuestionQueryService).idsKey(#ids)")
    public Map<Long, BizQuestion> mapByIds(Collection<Long> ids) {
        List<Long> normalized = normalizeIds(ids);
        if (normalized.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectList(new LambdaQueryWrapper<BizQuestion>().in(BizQuestion::getId, normalized))
                .stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q, (a, b) -> a));
    }

    @Cacheable(cacheNames = RedisCacheConfig.QUESTIONS_CHAPTERS, key = "#subject == null ? 'all' : #subject")
    public List<String> chapters(Integer subject) {
        QueryWrapper<BizQuestion> w = new QueryWrapper<>();
        w.select("distinct chapter").isNotNull("chapter").ne("chapter", "");
        if (subject != null) {
            w.eq("subject", subject);
        }
        List<Object> objs = questionMapper.selectObjs(w);
        return objs.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static String idsKey(Collection<Long> ids) {
        return normalizeIds(ids).stream()
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
    }

    private static List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
