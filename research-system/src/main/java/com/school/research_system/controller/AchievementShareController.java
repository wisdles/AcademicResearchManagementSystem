package com.school.research_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.research_system.common.Result;
import com.school.research_system.entity.*;
import com.school.research_system.mapper.AchievementShareMapper;
import com.school.research_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多人共同成果机制
 */
@RestController
@RequestMapping("/share")
public class AchievementShareController {

    @Autowired private AchievementShareMapper shareMapper;
    @Autowired private IUserService userService;
    @Autowired private IPaperService paperService;
    @Autowired private IProjectService projectService;
    @Autowired private IPatentService patentService;
    @Autowired private ISoftwareCopyrightService softService;
    @Autowired private IBookService bookService;
    @Autowired private IAwardService awardService;
    @Autowired private ICompetitionService competitionService;
    @Autowired private ICourseService courseService;

    // 添加共同作者
    @PostMapping("/add")
    public Result<String> addShare(@RequestBody Map<String, Object> params) {
        String type = (String) params.get("achievementType");
        Long achievementId = Long.valueOf(params.get("achievementId").toString());
        @SuppressWarnings("unchecked")
        List<Long> sharedIds = ((List<Object>) params.get("sharedUserIds")).stream()
                .map(o -> Long.valueOf(o.toString())).toList();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        for (Long sid : sharedIds) {
            // 跳过自己
            if (sid.equals(me.getId())) continue;
            // 检查是否已存在
            Long exists = shareMapper.selectCount(new LambdaQueryWrapper<AchievementShare>()
                    .eq(AchievementShare::getAchievementType, type)
                    .eq(AchievementShare::getAchievementId, achievementId)
                    .eq(AchievementShare::getSharedUserId, sid));
            if (exists > 0) continue;

            AchievementShare s = new AchievementShare();
            s.setAchievementType(type);
            s.setAchievementId(achievementId);
            s.setOwnerUserId(me.getId());
            s.setSharedUserId(sid);
            s.setRoleType((String) params.getOrDefault("roleType", "CO_AUTHOR"));
            s.setContributionPercent(params.get("contributionPercent") != null
                    ? Integer.valueOf(params.get("contributionPercent").toString()) : null);
            shareMapper.insert(s);
        }
        return Result.success("已关联 " + sharedIds.size() + " 位共同作者");
    }

    // 查看某成果的共同作者列表
    @GetMapping("/list/{type}/{id}")
    public Result<List<Map<String, Object>>> listShares(@PathVariable String type, @PathVariable Long id) {
        List<AchievementShare> shares = shareMapper.selectList(new LambdaQueryWrapper<AchievementShare>()
                .eq(AchievementShare::getAchievementType, type)
                .eq(AchievementShare::getAchievementId, id));
        List<Map<String, Object>> result = new ArrayList<>();
        for (AchievementShare s : shares) {
            User u = userService.getById(s.getSharedUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("userId", u != null ? u.getId() : null);
            m.put("realName", u != null ? u.getRealName() : "未知");
            m.put("roleType", s.getRoleType());
            m.put("contributionPercent", s.getContributionPercent());
            result.add(m);
        }
        return Result.success(result);
    }

    // 删除共同作者
    @DeleteMapping("/remove/{shareId}")
    public Result<String> removeShare(@PathVariable Long shareId) {
        shareMapper.deleteById(shareId);
        return Result.success("已移除");
    }

    // 教师查看"我参与的成果"（别人申报但共享给我的）
    @GetMapping("/my-participation")
    public Result<List<Map<String, Object>>> myParticipation() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<AchievementShare> shares = shareMapper.selectList(new LambdaQueryWrapper<AchievementShare>()
                .eq(AchievementShare::getSharedUserId, me.getId()));
        if (shares.isEmpty()) return Result.success(List.of());

        // 按类型分组
        Map<String, List<AchievementShare>> grouped = shares.stream()
                .collect(Collectors.groupingBy(AchievementShare::getAchievementType));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<AchievementShare>> e : grouped.entrySet()) {
            String type = e.getKey();
            List<Long> ids = e.getValue().stream().map(AchievementShare::getAchievementId).distinct().toList();
            List<?> items = loadByIds(type, ids);
            User owner = userService.getById(e.getValue().get(0).getOwnerUserId());

            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String name = getName(item, type);
                if (name == null) continue;
                Map<String, Object> m = new HashMap<>();
                m.put("type", type);
                m.put("typeLabel", typeLabel(type));
                m.put("name", name);
                m.put("ownerName", owner != null ? owner.getRealName() : "未知");
                m.put("shareId", e.getValue().get(0).getId());
                result.add(m);
            }
        }
        return Result.success(result);
    }

    // === helpers ===
    @SuppressWarnings("unchecked")
    private List<?> loadByIds(String type, List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return switch (type) {
            case "paper" -> paperService.list(new LambdaQueryWrapper<Paper>().in(Paper::getId, ids));
            case "project" -> projectService.list(new LambdaQueryWrapper<Project>().in(Project::getId, ids));
            case "patent" -> patentService.list(new LambdaQueryWrapper<Patent>().in(Patent::getId, ids));
            case "software" -> softService.list(new LambdaQueryWrapper<SoftwareCopyright>().in(SoftwareCopyright::getId, ids));
            case "book" -> bookService.list(new LambdaQueryWrapper<Book>().in(Book::getId, ids));
            case "award" -> awardService.list(new LambdaQueryWrapper<Award>().in(Award::getId, ids));
            case "competition" -> competitionService.list(new LambdaQueryWrapper<Competition>().in(Competition::getId, ids));
            case "course" -> courseService.list(new LambdaQueryWrapper<Course>().in(Course::getId, ids));
            default -> List.of();
        };
    }

    private String getName(Object obj, String type) {
        try {
            return switch (type) {
                case "paper" -> ((Paper) obj).getTitle();
                case "project" -> ((Project) obj).getName();
                case "patent" -> ((Patent) obj).getName();
                case "software" -> ((SoftwareCopyright) obj).getName();
                case "book" -> ((Book) obj).getName();
                case "award" -> ((Award) obj).getAwardName();
                case "competition" -> ((Competition) obj).getName();
                case "course" -> ((Course) obj).getCourseName();
                default -> "未知";
            };
        } catch (Exception e) { return "未知"; }
    }

    private String typeLabel(String type) {
        return Map.of("paper","论文","project","项目","patent","专利","software","软著","book","专著","award","获奖","competition","竞赛","course","课程")
                .getOrDefault(type, type);
    }
}
