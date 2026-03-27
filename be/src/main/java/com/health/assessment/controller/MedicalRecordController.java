package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.MedicalRecordDTO;
import com.health.assessment.entity.MedicalRecord;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 医疗记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/medical-records")
@RequiredArgsConstructor
@Tag(name = "医疗记录管理", description = "医疗记录相关接口")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * 获取医疗记录列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取医疗记录列表", description = "分页获取用户的医疗记录")
    public ApiResponse<PageInfo<MedicalRecord>> getList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getUserId(request);
        PageInfo<MedicalRecord> pageInfo = medicalRecordService.getRecords(userId, pageNum, pageSize);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 根据ID获取医疗记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取医疗记录详情", description = "根据ID获取医疗记录")
    public ApiResponse<MedicalRecord> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        MedicalRecord record = medicalRecordService.getById(id, userId);
        return ApiResponse.success(record);
    }

    /**
     * 新增医疗记录
     */
    @PostMapping
    @Operation(summary = "新增医疗记录", description = "创建新的医疗记录")
    public ApiResponse<MedicalRecord> create(
            HttpServletRequest request,
            @Valid @RequestBody MedicalRecordDTO dto) {
        Long userId = getUserId(request);
        log.info("创建医疗记录: userId={}, type={}", userId, dto.getRecordType());

        MedicalRecord record = buildRecord(dto);
        MedicalRecord saved = medicalRecordService.create(userId, record);
        return ApiResponse.success("创建成功", saved);
    }

    /**
     * 更新医疗记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新医疗记录", description = "更新指定的医疗记录")
    public ApiResponse<MedicalRecord> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordDTO dto) {
        Long userId = getUserId(request);
        MedicalRecord record = buildRecord(dto);
        MedicalRecord updated = medicalRecordService.update(userId, id, record);
        return ApiResponse.success("更新成功", updated);
    }

    /**
     * 删除医疗记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除医疗记录", description = "删除指定的医疗记录")
    public ApiResponse<String> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        medicalRecordService.delete(userId, id);
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

    private MedicalRecord buildRecord(MedicalRecordDTO dto) {
        return MedicalRecord.builder()
                .recordType(dto.getRecordType())
                .recordTitle(dto.getRecordTitle())
                .recordContent(dto.getRecordContent())
                .recordDate(dto.getRecordDate())
                .hospital(dto.getHospital())
                .doctorName(dto.getDoctorName())
                .build();
    }
}

