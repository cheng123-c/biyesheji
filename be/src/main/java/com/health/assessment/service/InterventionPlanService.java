package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.InterventionPlan;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.InterventionPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 干预方案服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterventionPlanService {

    /** 干预方案状态白名单 */
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "PAUSED", "COMPLETED", "CANCELLED");

    /** 干预方案类型白名单 */
    private static final Set<String> VALID_PLAN_TYPES = Set.of("DIET", "EXERCISE", "MEDICATION", "REHABILITATION", "OTHER");

    private final InterventionPlanMapper interventionPlanMapper;

    /**
     * 分页获取用户所有干预方案
     */
    public PageInfo<InterventionPlan> getPlans(Long userId, int pageNum, int pageSize, String status) {
        // 白名单校验：status（查询参数）
        if (status != null && !status.isEmpty() && !VALID_STATUSES.contains(status)) {
            throw new BusinessException("非法的方案状态参数: " + status);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<InterventionPlan> list = (status != null && !status.isEmpty()) ? interventionPlanMapper.selectByUserIdAndStatus(userId, status) : interventionPlanMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 获取用户进行中的干预方案
     */
    public List<InterventionPlan> getActivePlans(Long userId) {
        return interventionPlanMapper.selectActiveByUserId(userId);
    }

    /**
     * 按状态获取干预方案
     */
    public List<InterventionPlan> getPlansByStatus(Long userId, String status) {
        return interventionPlanMapper.selectByUserIdAndStatus(userId, status);
    }

    /**
     * 根据ID获取干预方案
     */
    public InterventionPlan getById(Long id, Long userId) {
        InterventionPlan plan = interventionPlanMapper.selectById(id);
        if (plan == null) {
            throw new ResourceNotFoundException("干预方案不存在: " + id);
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该方案");
        }
        return plan;
    }

    /**
     * 创建干预方案
     */
    @Transactional(rollbackFor = Exception.class)
    public InterventionPlan create(Long userId, InterventionPlan plan) {
        plan.setUserId(userId);

        // 设置默认值
        if (plan.getStatus() == null) {
            plan.setStatus("ACTIVE");
        }
        // 白名单校验：status
        if (!VALID_STATUSES.contains(plan.getStatus())) {
            throw new BusinessException("非法的方案状态: " + plan.getStatus());
        }
        // 白名单校验：planType
        if (plan.getPlanType() != null && !VALID_PLAN_TYPES.contains(plan.getPlanType())) {
            throw new BusinessException("非法的方案类型: " + plan.getPlanType());
        }
        if (plan.getStartDate() == null) {
            plan.setStartDate(LocalDate.now());
        }
        if (plan.getDurationDays() != null && plan.getEndDate() == null) {
            plan.setEndDate(plan.getStartDate().plusDays(plan.getDurationDays()));
        }

        interventionPlanMapper.insert(plan);
        log.info("创建干预方案成功: userId={}, id={}, type={}", userId, plan.getId(), plan.getPlanType());
        return plan;
    }

    /**
     * 更新干预方案
     */
    @Transactional(rollbackFor = Exception.class)
    public InterventionPlan update(Long userId, Long id, InterventionPlan newPlan) {
        InterventionPlan existing = interventionPlanMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("干预方案不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该方案");
        }
        // 白名单校验：status
        if (newPlan.getStatus() != null && !VALID_STATUSES.contains(newPlan.getStatus())) {
            throw new BusinessException("非法的方案状态: " + newPlan.getStatus());
        }
        // 白名单校验：planType
        if (newPlan.getPlanType() != null && !VALID_PLAN_TYPES.contains(newPlan.getPlanType())) {
            throw new BusinessException("非法的方案类型: " + newPlan.getPlanType());
        }
        newPlan.setId(id);
        newPlan.setUserId(userId);
        // 若设置了 durationDays 且未手动设置 endDate，则自动计算 endDate
        if (newPlan.getDurationDays() != null && newPlan.getEndDate() == null
                && newPlan.getStartDate() != null) {
            newPlan.setEndDate(newPlan.getStartDate().plusDays(newPlan.getDurationDays()));
        }
        int affected = interventionPlanMapper.update(newPlan);
        if (affected == 0) {
            throw new ResourceNotFoundException("干预方案不存在或已被删除: " + id);
        }
        log.info("更新干预方案成功: userId={}, id={}", userId, id);
        return interventionPlanMapper.selectById(id);
    }

    /**
     * 更新干预方案状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Long id, String status) {
        // 白名单校验：status
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new BusinessException("非法的方案状态: " + status);
        }
        InterventionPlan existing = interventionPlanMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("干预方案不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该方案");
        }
        int affected = interventionPlanMapper.updateStatus(id, userId, status);
        if (affected == 0) {
            throw new ResourceNotFoundException("干预方案不存在或已被删除: " + id);
        }
        log.info("更新干预方案状态成功: userId={}, id={}, status={}", userId, id, status);
    }

    /**
     * 删除干预方案
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        InterventionPlan existing = interventionPlanMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("干预方案不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该方案");
        }
        int affected = interventionPlanMapper.deleteByIdAndUserId(id, userId);
        if (affected == 0) {
            throw new ResourceNotFoundException("干预方案不存在或已被删除: " + id);
        }
        log.info("删除干预方案成功: userId={}, id={}", userId, id);
    }

    /**
     * 获取进行中方案数量
     */
    public Integer countActive(Long userId) {
        return interventionPlanMapper.countActiveByUserId(userId);
    }

    /**
     * 获取方案总数
     */
    public Integer countTotal(Long userId) {
        return interventionPlanMapper.countByUserId(userId);
    }
}

