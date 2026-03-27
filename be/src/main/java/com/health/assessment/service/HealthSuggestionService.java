package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.HealthSuggestion;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.HealthSuggestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 健康建议服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthSuggestionService {

    /** 建议优先级白名单 */
    private static final Set<String> VALID_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");

    /** 建议类型白名单 */
    private static final Set<String> VALID_SUGGESTION_TYPES = Set.of("DIET", "EXERCISE", "MEDICATION", "LIFESTYLE", "OTHER");

    private final HealthSuggestionMapper healthSuggestionMapper;

    /**
     * 分页获取用户所有健康建议
     */
    public PageInfo<HealthSuggestion> getSuggestions(Long userId, int pageNum, int pageSize, String priority) {
        // 白名单校验：priority（查询参数，非法值直接忽略）
        if (priority != null && !priority.isEmpty() && !VALID_PRIORITIES.contains(priority)) {
            throw new BusinessException("非法的优先级参数: " + priority);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<HealthSuggestion> list = (priority != null && !priority.isEmpty()) ? healthSuggestionMapper.selectByUserIdAndPriority(userId, priority) : healthSuggestionMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 获取用户未读健康建议
     */
    public List<HealthSuggestion> getUnreadSuggestions(Long userId) {
        return healthSuggestionMapper.selectUnreadByUserId(userId);
    }

    /**
     * 按优先级获取健康建议
     */
    public List<HealthSuggestion> getSuggestionsByPriority(Long userId, String priority) {
        return healthSuggestionMapper.selectByUserIdAndPriority(userId, priority);
    }

    /**
     * 根据ID获取健康建议
     */
    public HealthSuggestion getById(Long id, Long userId) {
        HealthSuggestion suggestion = healthSuggestionMapper.selectById(id);
        if (suggestion == null) {
            throw new ResourceNotFoundException("健康建议不存在: " + id);
        }
        if (!suggestion.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该建议");
        }
        return suggestion;
    }

    /**
     * 创建健康建议（通常由AI评测后自动生成，也允许手动创建）
     */
    @Transactional(rollbackFor = Exception.class)
    public HealthSuggestion create(Long userId, HealthSuggestion suggestion) {
        suggestion.setUserId(userId);
        if (suggestion.getCreatedAt() == null) { suggestion.setCreatedAt(java.time.LocalDateTime.now()); }
        if (suggestion.getPriority() == null) {
            suggestion.setPriority("MEDIUM");
        }
        // 白名单校验：priority
        if (!VALID_PRIORITIES.contains(suggestion.getPriority())) {
            throw new BusinessException("非法的优先级: " + suggestion.getPriority());
        }
        // 白名单校验：suggestionType
        if (suggestion.getSuggestionType() != null && !VALID_SUGGESTION_TYPES.contains(suggestion.getSuggestionType())) {
            throw new BusinessException("非法的建议类型: " + suggestion.getSuggestionType());
        }
        healthSuggestionMapper.insert(suggestion);
        log.info("创建健康建议成功: userId={}, id={}", userId, suggestion.getId());
        return suggestion;
    }

    /**
     * 标记建议为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long id) {
        HealthSuggestion suggestion = healthSuggestionMapper.selectById(id);
        if (suggestion == null) {
            throw new ResourceNotFoundException("健康建议不存在: " + id);
        }
        if (!suggestion.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该建议");
        }
        // 若已读则直接返回（幂等操作，无需报错）
        if (suggestion.getReadAt() != null) {
            return;
        }
        int affected = healthSuggestionMapper.markAsRead(id, userId);
        if (affected == 0) {
            throw new ResourceNotFoundException("健康建议不存在或已被删除: " + id);
        }
        log.info("标记建议为已读: userId={}, id={}", userId, id);
    }

    /**
     * 标记所有建议为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        int count = healthSuggestionMapper.markAllAsRead(userId);
        log.info("标记所有建议为已读: userId={}, count={}", userId, count);
    }

    /**
     * 删除健康建议
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        HealthSuggestion suggestion = healthSuggestionMapper.selectById(id);
        if (suggestion == null) {
            throw new ResourceNotFoundException("健康建议不存在: " + id);
        }
        if (!suggestion.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该建议");
        }
        int affected = healthSuggestionMapper.deleteByIdAndUserId(id, userId);
        if (affected == 0) {
            throw new ResourceNotFoundException("健康建议不存在或已被删除: " + id);
        }
        log.info("删除健康建议成功: userId={}, id={}", userId, id);
    }

    /**
     * 获取未读建议数量
     */
    public Integer countUnread(Long userId) {
        return healthSuggestionMapper.countUnreadByUserId(userId);
    }

    /**
     * 获取建议总数
     */
    public Integer countTotal(Long userId) {
        return healthSuggestionMapper.countByUserId(userId);
    }
}

