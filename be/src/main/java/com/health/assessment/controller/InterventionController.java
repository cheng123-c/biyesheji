package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.InterventionPlanDTO;
import com.health.assessment.entity.InterventionPlan;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.InterventionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 干预方案控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/interventions")
@RequiredArgsConstructor
@Tag(name = "干预方案管理", description = "干预方案相关接口")
public class InterventionController {

    private final InterventionPlanService interventionPlanService;

    /**
     * 获取干预方案列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取干预方案列表", description = "分页获取用户的干预方案")
    public ApiResponse<PageInfo<InterventionPlan>> getList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = getUserId(request);
        PageInfo<InterventionPlan> pageInfo = interventionPlanService.getPlans(userId, pageNum, pageSize, status);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取进行中的干预方案
     */
    @GetMapping("/active")
    @Operation(summary = "获取进行中方案", description = "获取用户当前进行中的干预方案")
    public ApiResponse<List<InterventionPlan>> getActivePlans(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<InterventionPlan> list = interventionPlanService.getActivePlans(userId);
        return ApiResponse.success(list);
    }

    /**
     * 获取进行中方案数量
     */
    @GetMapping("/active/count")
    @Operation(summary = "获取进行中方案数量", description = "获取用户进行中的干预方案数量")
    public ApiResponse<Integer> getActiveCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer count = interventionPlanService.countActive(userId);
        return ApiResponse.success(count);
    }

    /**
     * 根据ID获取干预方案
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取方案详情", description = "根据ID获取干预方案详情")
    public ApiResponse<InterventionPlan> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        InterventionPlan plan = interventionPlanService.getById(id, userId);
        return ApiResponse.success(plan);
    }

    /**
     * 创建干预方案
     */
    @PostMapping
    @Operation(summary = "创建干预方案", description = "创建新的干预方案")
    public ApiResponse<InterventionPlan> create(
            HttpServletRequest request,
            @Valid @RequestBody InterventionPlanDTO dto) {
        Long userId = getUserId(request);
        log.info("创建干预方案: userId={}, type={}", userId, dto.getPlanType());

        InterventionPlan plan = buildPlan(dto);
        InterventionPlan saved = interventionPlanService.create(userId, plan);
        return ApiResponse.success("创建成功", saved);
    }

    /**
     * 更新干预方案
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新干预方案", description = "更新指定的干预方案")
    public ApiResponse<InterventionPlan> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody InterventionPlanDTO dto) {
        Long userId = getUserId(request);
        InterventionPlan plan = buildPlan(dto);
        InterventionPlan updated = interventionPlanService.update(userId, id, plan);
        return ApiResponse.success("更新成功", updated);
    }

    /**
     * 更新干预方案状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新方案状态", description = "更新干预方案的执行状态")
    public ApiResponse<String> updateStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = getUserId(request);
        String status = body.get("status");
        if (status == null || status.isEmpty()) {
            return ApiResponse.error(400, "状态不能为空");
        }
        interventionPlanService.updateStatus(userId, id, status);
        return ApiResponse.success("状态更新成功", "");
    }

    /**
     * 删除干预方案
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除干预方案", description = "删除指定的干预方案")
    public ApiResponse<String> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        interventionPlanService.delete(userId, id);
        return ApiResponse.success("删除成功", "");
    }

    // ============ 私有方法 ============

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }
        return userId;
    }

    private InterventionPlan buildPlan(InterventionPlanDTO dto) {
        return InterventionPlan.builder()
                .planType(dto.getPlanType())
                .targetDisease(dto.getTargetDisease())
                .planDetail(dto.getPlanDetail())
                .durationDays(dto.getDurationDays())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();
    }
}

