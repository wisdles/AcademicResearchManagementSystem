package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.MessagePreference;
import com.school.research_system.entity.ScoreRule;
import com.school.research_system.entity.User;
import com.school.research_system.mapper.MessagePreferenceMapper;
import com.school.research_system.mapper.ScoreRuleMapper;
import com.school.research_system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preference")
public class PreferencesController {

    @Autowired private MessagePreferenceMapper messagePreferenceMapper;
    @Autowired private ScoreRuleMapper scoreRuleMapper;
    @Autowired private IUserService userService;

    // === 消息偏好 ===
    @GetMapping("/message")
    public Result<MessagePreference> getPreference() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        MessagePreference p = messagePreferenceMapper.selectOne(
                new LambdaQueryWrapper<MessagePreference>().eq(MessagePreference::getUserId, user.getId()));
        if (p == null) {
            p = new MessagePreference();
            p.setUserId(user.getId());
            p.setAuditResultEnabled(1);
            p.setUrgeEnabled(1);
            p.setNoticeEnabled(1);
            p.setSystemEnabled(1);
            messagePreferenceMapper.insert(p);
        }
        return Result.success(p);
    }

    @PostMapping("/message")
    public Result<String> savePreference(@RequestBody MessagePreference pref) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        pref.setUserId(user.getId());
        messagePreferenceMapper.update(pref, new LambdaQueryWrapper<MessagePreference>()
                .eq(MessagePreference::getUserId, user.getId()));
        return Result.success("保存成功");
    }

    // === 评分规则（管理员） ===
    @GetMapping("/rules")
    public Result<List<ScoreRule>> getRules() { return Result.success(scoreRuleMapper.selectList(null)); }

    @PostMapping("/rules/save")
    public Result<String> saveRules(@RequestBody List<ScoreRule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            ScoreRule r = rules.get(i);
            ScoreRule exist = scoreRuleMapper.selectOne(
                    new LambdaQueryWrapper<ScoreRule>().eq(ScoreRule::getRuleType, r.getRuleType()).eq(ScoreRule::getRuleKey, r.getRuleKey()));
            if (exist != null) { r.setId(exist.getId()); scoreRuleMapper.updateById(r); }
            else scoreRuleMapper.insert(r);
        }
        return Result.success("保存成功");
    }

    // 初始化默认评分规则
    @PostMapping("/rules/init")
    public Result<String> initRules() {
        if (scoreRuleMapper.selectCount(null) > 0) return Result.success("已存在");
        String[][] data = {
            {"project", "NATIONAL", "国家级项目", "5"}, {"project", "PROVINCIAL", "省部级项目", "3"}, {"project", "CITY", "市厅级项目", "2"}, {"project", "SCHOOL", "校级项目", "1"},
            {"paper", "SCI_Q1", "SCI一区", "5"}, {"paper", "SCI_Q2", "SCI二区", "4"}, {"paper", "SCI_Q3", "SCI三区", "3"}, {"paper", "SCI_Q4", "SCI四区", "2"},
            {"paper", "EI", "EI收录", "2"}, {"paper", "CORE", "核心期刊", "2"}, {"paper", "GENERAL", "普通期刊", "1"},
            {"patent", "INVENTION", "发明专利", "3"}, {"patent", "UTILITY", "实用新型", "2"}, {"patent", "DESIGN", "外观设计", "1"},
            {"software", "NORMAL", "软件著作权", "2"},
            {"book", "NATIONAL", "国家级出版社", "3"}, {"book", "LOCAL", "地方出版社", "2"},
            {"award", "NATIONAL", "国家级获奖", "5"}, {"award", "PROVINCIAL", "省部级获奖", "3"}, {"award", "CITY", "市厅级获奖", "2"},
            {"competition", "NATIONAL", "国家级竞赛", "3"}, {"competition", "PROVINCIAL", "省部级竞赛", "2"}, {"competition", "SCHOOL", "校级竞赛", "1"},
            {"course", "NATIONAL", "国家级课程", "4"}, {"course", "PROVINCIAL", "省级课程", "3"}, {"course", "SCHOOL", "校级课程", "2"}
        };
        for (String[] d : data) {
            ScoreRule r = new ScoreRule();
            r.setRuleType(d[0]); r.setRuleKey(d[1]); r.setRuleLabel(d[2]); r.setScore(Integer.parseInt(d[3])); r.setEnabled(1);
            scoreRuleMapper.insert(r);
        }
        return Result.success("初始化完成");
    }
}