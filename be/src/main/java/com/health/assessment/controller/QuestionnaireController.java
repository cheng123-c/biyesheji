package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.QuestionnaireSubmitDTO;
import com.health.assessment.entity.Questionnaire;
import com.health.assessment.entity.QuestionnaireResponse;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 问卷控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/questionnaires")
@RequiredArgsConstructor
@Tag(name = "问卷管理", description = "健康问卷相关接口")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    // ==================== 用户端接口 ====================

    /**
     * 获取所有启用的问卷列表
     */
    @GetMapping
    @Operation(summary = "获取问卷列表", description = "获取所有启用的健康问卷")
    public ApiResponse<List<Questionnaire>> getList(
            @RequestParam(required = false) String type) {
        List<Questionnaire> list = type != null
                ? questionnaireService.getByType(type)
                : questionnaireService.getActiveQuestionnaires();
        return ApiResponse.success(list);
    }

    /**
     * 获取问卷详情（含题目）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问卷详情", description = "获取指定问卷的详细信息和题目")
    public ApiResponse<Questionnaire> getById(@PathVariable Long id) {
        return ApiResponse.success(questionnaireService.getById(id));
    }

    /**
     * 提交问卷回答
     */
    @PostMapping("/submit")
    @Operation(summary = "提交问卷", description = "提交健康问卷的回答")
    public ApiResponse<QuestionnaireResponse> submit(
            HttpServletRequest request,
            @Valid @RequestBody QuestionnaireSubmitDTO dto) {
        Long userId = getUserId(request);
        log.info("提交问卷回答: userId={}, questionnaireId={}", userId, dto.getQuestionnaireId());
        QuestionnaireResponse response = questionnaireService.submitResponse(
                userId, dto.getQuestionnaireId(), dto.getAnswers());
        return ApiResponse.success("提交成功", response);
    }

    /**
     * 获取我的问卷回答历史
     */
    @GetMapping("/my-responses")
    @Operation(summary = "我的问卷历史", description = "获取当前用户的问卷回答历史（分页）")
    public ApiResponse<PageInfo<QuestionnaireResponse>> getMyResponses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getUserId(request);
        return ApiResponse.success(questionnaireService.getMyResponses(userId, pageNum, pageSize));
    }

    /**
     * 获取某次回答详情
     */
    @GetMapping("/my-responses/{id}")
    @Operation(summary = "获取回答详情", description = "获取指定问卷回答的详细信息")
    public ApiResponse<QuestionnaireResponse> getResponseById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        return ApiResponse.success(questionnaireService.getResponseById(id, userId));
    }

    /**
     * 获取用户对某问卷的最新回答
     */
    @GetMapping("/{questionnaireId}/my-latest")
    @Operation(summary = "获取最新回答", description = "获取用户对某问卷的最近一次回答")
    public ApiResponse<QuestionnaireResponse> getLatestResponse(
            HttpServletRequest request,
            @PathVariable Long questionnaireId) {
        Long userId = getUserId(request);
        QuestionnaireResponse response = questionnaireService.getLatestResponse(userId, questionnaireId);
        return ApiResponse.success(response);
    }

    /**
     * 统计已完成问卷数量
     */
    @GetMapping("/my-count")
    @Operation(summary = "我的问卷数量", description = "统计当前用户已完成的问卷数量")
    public ApiResponse<Integer> getMyCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(questionnaireService.countMyResponses(userId));
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取全部问卷
     */
    @GetMapping("/admin/all")
    @Operation(summary = "获取所有问卷（管理员）", description = "管理员获取所有问卷，含禁用的")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Questionnaire>> adminGetAll() {
        return ApiResponse.success(questionnaireService.getAll());
    }

    /**
     * 管理员创建问卷
     */
    @PostMapping("/admin")
    @Operation(summary = "创建问卷（管理员）", description = "管理员创建新问卷")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Questionnaire> adminCreate(@RequestBody Questionnaire questionnaire) {
        return ApiResponse.success("创建成功", questionnaireService.create(questionnaire));
    }

    /**
     * 管理员更新问卷
     */
    @PutMapping("/admin/{id}")
    @Operation(summary = "更新问卷（管理员）", description = "管理员更新问卷信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Questionnaire> adminUpdate(
            @PathVariable Long id,
            @RequestBody Questionnaire questionnaire) {
        return ApiResponse.success("更新成功", questionnaireService.update(id, questionnaire));
    }

    /**
     * 管理员删除问卷
     */
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除问卷（管理员）", description = "管理员删除问卷")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminDelete(@PathVariable Long id) {
        questionnaireService.delete(id);
        return ApiResponse.success("删除成功", "");
    }

    /**
     * 管理员切换问卷启用/禁用状态
     */
    @PutMapping("/admin/{id}/toggle-active")
    @Operation(summary = "切换问卷状态（管理员）", description = "管理员启用或禁用问卷")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Questionnaire> adminToggleActive(@PathVariable Long id) {
        return ApiResponse.success("状态已更新", questionnaireService.toggleActive(id));
    }

    /**
     * 管理员分页查看所有用户的问卷回答记录
     */
    @GetMapping("/admin/responses")
    @Operation(summary = "查看所有回答记录（管理员）", description = "管理员分页查看所有用户提交的问卷回答，可按问卷ID过滤")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageInfo<QuestionnaireResponse>> adminGetAllResponses(
            @RequestParam(required = false) Long questionnaireId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize) {
        return ApiResponse.success(questionnaireService.adminGetAllResponses(questionnaireId, pageNum, pageSize));
    }

    // ==================== 私有方法 ====================

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }
        return userId;
    }
}

