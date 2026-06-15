package com.jiakao.ydt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.entity.BizQuestion;
import com.jiakao.ydt.entity.BizWrongBook;
import com.jiakao.ydt.mapper.BizQuestionMapper;
import com.jiakao.ydt.mapper.BizWrongBookMapper;
import com.jiakao.ydt.security.SecurityUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错题本
 */
@RestController
@RequestMapping("/wrong-book")
@RequiredArgsConstructor
public class WrongBookController {

    private final BizWrongBookMapper wrongBookMapper;
    private final BizQuestionMapper questionMapper;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public R<Page<WrongItemVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Long uid = SecurityUtils.requireUser().getId();
        Page<BizWrongBook> p = wrongBookMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<BizWrongBook>().eq(BizWrongBook::getUserId, uid).orderByDesc(BizWrongBook::getLastWrongAt));
        List<Long> qids = p.getRecords().stream().map(BizWrongBook::getQuestionId).collect(Collectors.toList());
        Map<Long, BizQuestion> qmap = qids.isEmpty() ? Map.of() :
                questionMapper.selectList(new LambdaQueryWrapper<BizQuestion>().in(BizQuestion::getId, qids))
                        .stream().collect(Collectors.toMap(BizQuestion::getId, x -> x));
        Page<WrongItemVO> out = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        out.setRecords(p.getRecords().stream().map(w -> {
            WrongItemVO vo = new WrongItemVO();
            vo.setWrong(w);
            vo.setQuestion(qmap.get(w.getQuestionId()));
            return vo;
        }).collect(Collectors.toList()));
        return R.ok(out);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public R<Void> delete(@PathVariable Long id) {
        BizWrongBook w = wrongBookMapper.selectById(id);
        if (w == null || !w.getUserId().equals(SecurityUtils.requireUser().getId())) {
            throw new BusinessException("记录不存在");
        }
        wrongBookMapper.deleteById(id);
        return R.ok();
    }

    @Data
    public static class WrongItemVO {
        private BizWrongBook wrong;
        private BizQuestion question;
    }
}
