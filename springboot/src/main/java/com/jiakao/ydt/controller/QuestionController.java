package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.entity.BizQuestion;
import com.jiakao.ydt.mapper.BizQuestionMapper;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.jiakao.ydt.service.QuestionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 题库维护（管理员）
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final BizQuestionMapper questionMapper;
    private final QuestionQueryService questionQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Page<BizQuestion>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer subject) {
        LambdaQueryWrapper<BizQuestion> q = new LambdaQueryWrapper<>();
        if (subject != null) {
            q.eq(BizQuestion::getSubject, subject);
        }
        q.orderByDesc(BizQuestion::getId);
        return R.ok(questionMapper.selectPage(new Page<>(current, size), q));
    }

    @GetMapping("/chapters")
    @PreAuthorize("hasRole('ADMIN')")
    public R<List<String>> chapters(@RequestParam(required = false) Integer subject) {
        return R.ok(questionQueryService.chapters(subject));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_SUBJECT_ALL, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_BY_IDS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_CHAPTERS, allEntries = true)
    })
    public R<Void> create(@RequestBody BizQuestion question) {
        questionMapper.insert(question);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_SUBJECT_ALL, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_BY_IDS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_CHAPTERS, allEntries = true)
    })
    public R<Void> update(@PathVariable Long id, @RequestBody BizQuestion question) {
        question.setId(id);
        questionMapper.updateById(question);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_SUBJECT_ALL, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_BY_IDS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_CHAPTERS, allEntries = true)
    })
    public R<Void> delete(@PathVariable Long id) {
        questionMapper.deleteById(id);
        return R.ok();
    }

    /**
     * Excel 导入：支持列名为 BizQuestion 字段名或数据库列名
     * 例如：subject / qType / q_type，optionA / option_a 均可。
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_SUBJECT_ALL, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_BY_IDS, allEntries = true),
            @CacheEvict(cacheNames = RedisCacheConfig.QUESTIONS_CHAPTERS, allEntries = true)
    })
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要导入的 Excel 文件");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xls") && !name.endsWith(".xlsx")) {
            throw new BusinessException("仅支持 .xls 或 .xlsx 文件");
        }

        try (InputStream in = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(in);
            List<Map<String, Object>> rows = reader.readAll();
            if (rows == null || rows.isEmpty()) {
                throw new BusinessException("Excel 内容为空");
            }

            List<BizQuestion> questions = new java.util.ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);
                if (row == null || row.isEmpty() || isBlankRow(row)) {
                    continue;
                }
                BizQuestion q = parseQuestionRow(row, i + 2);
                q.setCreateTime(LocalDateTime.now());
                questions.add(q);
            }

            if (questions.isEmpty()) {
                throw new BusinessException("未找到可导入的数据行");
            }

            for (BizQuestion q : questions) {
                questionMapper.insert(q);
            }
            return R.ok();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("导入失败: " + rootMessage(e));
        }
    }

    private static BizQuestion parseQuestionRow(Map<String, Object> row, int excelRowNum) {
        BizQuestion q = new BizQuestion();
        q.setSubject(parseRequiredInteger(getValue(row, excelRowNum, "subject", "subject_type", "科目"), excelRowNum, "subject"));
        q.setQType(parseRequiredInteger(getValue(row, excelRowNum, "qtype", "q_type", "type", "类型"), excelRowNum, "qType"));
        q.setTitle(requireText(getValue(row, excelRowNum, "title", "question", "题干"), excelRowNum, "title"));
        q.setOptionA(textOrNull(getValue(row, excelRowNum, "optiona", "option_a", "a", "选项a")));
        q.setOptionB(textOrNull(getValue(row, excelRowNum, "optionb", "option_b", "b", "选项b")));
        q.setOptionC(textOrNull(getValue(row, excelRowNum, "optionc", "option_c", "c", "选项c")));
        q.setOptionD(textOrNull(getValue(row, excelRowNum, "optiond", "option_d", "d", "选项d")));
        q.setAnswer(requireText(getValue(row, excelRowNum, "answer", "答案"), excelRowNum, "answer").toUpperCase());
        q.setAnalysis(textOrNull(getValue(row, excelRowNum, "analysis", "explain", "解析")));
        q.setImageUrl(optionalImageUrl(row));
        q.setChapter(textOrNull(getValue(row, excelRowNum, "chapter", "章节")));
        q.setId(null);
        validateQuestion(q, excelRowNum);
        return q;
    }

    private static void validateQuestion(BizQuestion q, int excelRowNum) {
        if (q.getSubject() != 1 && q.getSubject() != 4) {
            throw new BusinessException("第 " + excelRowNum + " 行：subject 只能是 1 或 4");
        }
        if (q.getQType() < 1 || q.getQType() > 3) {
            throw new BusinessException("第 " + excelRowNum + " 行：qType 只能是 1、2、3");
        }
        if (q.getTitle() == null || q.getTitle().isBlank()) {
            throw new BusinessException("第 " + excelRowNum + " 行：title 不能为空");
        }
        if (q.getAnswer() == null || q.getAnswer().isBlank()) {
            throw new BusinessException("第 " + excelRowNum + " 行：answer 不能为空");
        }
    }

    private static boolean isBlankRow(Map<String, Object> row) {
        return row.values().stream().allMatch(v -> v == null || v.toString().trim().isEmpty());
    }

    private static String getValue(Map<String, Object> row, int excelRowNum, String... aliases) {
        Map<String, Object> normalized = new HashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            normalized.put(normalizeHeader(entry.getKey()), entry.getValue());
        }
        for (String alias : aliases) {
            Object value = normalized.get(normalizeHeader(alias));
            if (value != null) {
                return value.toString().trim();
            }
        }
        throw new BusinessException("第 " + excelRowNum + " 行缺少必要列：" + String.join("/", aliases));
    }

    private static Integer parseRequiredInteger(String text, int excelRowNum, String field) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("第 " + excelRowNum + " 行：" + field + " 不能为空");
        }
        try {
            return new BigDecimal(text.trim()).intValueExact();
        } catch (Exception e) {
            throw new BusinessException("第 " + excelRowNum + " 行：" + field + " 必须是整数");
        }
    }

    private static String requireText(String text, int excelRowNum, String field) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("第 " + excelRowNum + " 行：" + field + " 不能为空");
        }
        return text.trim();
    }

    private static String textOrNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static String optionalImageUrl(Map<String, Object> row) {
        Map<String, Object> normalized = new HashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            normalized.put(normalizeHeader(entry.getKey()), entry.getValue());
        }
        for (String alias : new String[]{"imageurl", "image_url", "图片", "image"}) {
            Object value = normalized.get(normalizeHeader(alias));
            if (value != null) {
                String text = value.toString().trim();
                return text.isEmpty() ? null : text;
            }
        }
        return null;
    }

    private static String normalizeHeader(String header) {
        if (header == null) return "";
        return header.trim().toLowerCase().replaceAll("[\\s_\\-]+", "");
    }

    private static String rootMessage(Throwable e) {
        Throwable cur = e;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() == null ? e.getMessage() : cur.getMessage();
    }
}
