package com.health.assessment.controller;

import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.InferenceResult;
import com.health.assessment.entity.KnowledgeRelation;
import com.health.assessment.entity.MedicalConcept;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.KnowledgeInferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * 知识图谱与推理引擎控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识图谱推理", description = "症状分析与知识图谱相关接口")
public class KnowledgeController {

    private final KnowledgeInferenceService knowledgeInferenceService;

    // ==================== 用户端接口 ====================

    /**
     * 获取所有可选症状（供前端下拉选择）
     */
    @GetMapping("/symptoms")
    @Operation(summary = "获取症状列表", description = "获取知识图谱中的所有症状概念")
    public ApiResponse<List<MedicalConcept>> getSymptoms() {
        return ApiResponse.success(knowledgeInferenceService.getAllSymptoms());
    }

    /**
     * 获取所有疾病
     */
    @GetMapping("/diseases")
    @Operation(summary = "获取疾病列表", description = "获取知识图谱中的所有疾病概念")
    public ApiResponse<List<MedicalConcept>> getDiseases() {
        return ApiResponse.success(knowledgeInferenceService.getAllDiseases());
    }

    /**
     * 搜索医学概念（支持模糊搜索）
     */
    @GetMapping("/concepts/search")
    @Operation(summary = "搜索医学概念", description = "根据关键词搜索医学概念")
    public ApiResponse<List<MedicalConcept>> searchConcepts(@RequestParam String keyword) {
        return ApiResponse.success(knowledgeInferenceService.searchConcepts(keyword));
    }

    /**
     * 查询概念的知识图谱关系（可视化用）
     */
    @GetMapping("/concepts/{id}/relations")
    @Operation(summary = "查询概念关系", description = "获取指定医学概念的知识图谱关系")
    public ApiResponse<List<KnowledgeRelation>> getRelations(@PathVariable Long id) {
        return ApiResponse.success(knowledgeInferenceService.getRelationsByConceptId(id));
    }

    /**
     * 症状推理分析（核心功能）
     */
    @PostMapping("/infer")
    @Operation(summary = "症状推理分析", description = "根据输入的症状列表，推理可能的疾病并生成AI建议")
    public ApiResponse<InferenceResult> inferFromSymptoms(
            HttpServletRequest request,
            @Valid @RequestBody SymptomInferRequest body) {
        // 可选登录：登录用户保存记录，匿名用户不保存
        Long userId = (Long) request.getAttribute("userId");
        log.info("症状推理请求: userId={}, symptoms={}", userId, body.getSymptoms());
        InferenceResult result = knowledgeInferenceService.inferFromSymptoms(userId, body.getSymptoms());
        return ApiResponse.success(result);
    }

    /**
     * 获取我的历史推理记录
     */
    @GetMapping("/infer/history")
    @Operation(summary = "我的推理历史", description = "获取当前用户的历史症状推理记录")
    public ApiResponse<List<InferenceResult>> getHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getUserId(request);
        return ApiResponse.success(knowledgeInferenceService.getHistoryResults(userId, limit));
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员获取全部概念
     */
    @GetMapping("/admin/concepts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<MedicalConcept>> adminGetAllConcepts() {
        return ApiResponse.success(knowledgeInferenceService.getAllConcepts());
    }

    /**
     * 管理员新增概念
     */
    @PostMapping("/admin/concepts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MedicalConcept> adminCreateConcept(@RequestBody MedicalConcept concept) {
        return ApiResponse.success("创建成功", knowledgeInferenceService.createConcept(concept));
    }

    /**
     * 管理员更新概念
     */
    @PutMapping("/admin/concepts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MedicalConcept> adminUpdateConcept(
            @PathVariable Long id, @RequestBody MedicalConcept concept) {
        return ApiResponse.success("更新成功", knowledgeInferenceService.updateConcept(id, concept));
    }

    /**
     * 管理员删除概念
     */
    @DeleteMapping("/admin/concepts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminDeleteConcept(@PathVariable Long id) {
        knowledgeInferenceService.deleteConcept(id);
        return ApiResponse.success("删除成功", "");
    }

    /**
     * 管理员添加关系
     */
    @PostMapping("/admin/relations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminCreateRelation(@RequestBody KnowledgeRelation relation) {
        knowledgeInferenceService.createRelation(relation);
        return ApiResponse.success("创建成功", "");
    }

    /**
     * 管理员删除关系
     */
    @DeleteMapping("/admin/relations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminDeleteRelation(@PathVariable Long id) {
        knowledgeInferenceService.deleteRelation(id);
        return ApiResponse.success("删除成功", "");
    }

    // ==================== 内部类 ====================

    @Data
    static class SymptomInferRequest {
        @NotEmpty(message = "症状列表不能为空")
        @Size(max = 10, message = "症状数量不超过10个")
        private List<String> symptoms;
    }

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new AuthenticationException("未能获取用户ID，请重新登录");
        return userId;
    }
}

