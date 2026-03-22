package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.HealthDataBatchUploadDTO;
import com.health.assessment.dto.HealthDataUploadDTO;
import com.health.assessment.entity.HealthData;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.mapper.HealthDataMapper;
import com.health.assessment.service.HealthDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 健康数据控制器
 *
 * 处理健康数据的上传、查询、统计等请求
 */
@Slf4j
@RestController
@RequestMapping("/v1/health-data")
@RequiredArgsConstructor
@Tag(name = "健康数据管理", description = "健康数据相关接口")
public class HealthDataController {

    private final HealthDataService healthDataService;

    /**
     * 上传单条健康数据
     */
    @PostMapping
    @Operation(summary = "上传健康数据", description = "上传单条健康数据")
    public ApiResponse<HealthData> upload(
            HttpServletRequest request,
            @Valid @RequestBody HealthDataUploadDTO dto) {
        Long userId = getUserId(request);
        log.info("上传健康数据: userId={}, type={}", userId, dto.getDataType());

        HealthData healthData = buildHealthData(dto, userId);
        HealthData saved = healthDataService.uploadHealthData(userId, healthData);
        return ApiResponse.success("数据上传成功", saved);
    }

    /**
     * 批量上传健康数据
     */
    @PostMapping("/batch")
    @Operation(summary = "批量上传健康数据", description = "批量上传多条健康数据")
    public ApiResponse<Integer> batchUpload(
            HttpServletRequest request,
            @Valid @RequestBody HealthDataBatchUploadDTO dto) {
        Long userId = getUserId(request);
        log.info("批量上传健康数据: userId={}, count={}", userId, dto.getItems().size());

        List<HealthData> list = dto.getItems().stream()
                .map(item -> buildHealthData(item, userId))
                .collect(Collectors.toList());

        int count = healthDataService.batchUpload(userId, list);
        return ApiResponse.success("批量上传成功", count);
    }

    /**
     * 获取当前用户各类型最新健康数据
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新健康数据", description = "获取各类型最新一条健康数据")
    public ApiResponse<List<HealthData>> getLatestAllTypes(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<HealthData> dataList = healthDataService.getLatestAllTypes(userId);
        return ApiResponse.success(dataList);
    }

    /**
     * 获取健康数据列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取健康数据列表", description = "分页获取健康数据")
    public ApiResponse<PageInfo<HealthData>> getList(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getUserId(request);
        PageInfo<HealthData> pageInfo = healthDataService.getPagedData(userId, startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取单条健康数据
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取单条健康数据", description = "根据ID获取健康数据")
    public ApiResponse<HealthData> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        HealthData data = healthDataService.getById(id);
        // 校验数据归属权，防止越权访问
        if (!data.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该数据");
        }
        return ApiResponse.success(data);
    }

    /**
     * 获取健康数据趋势（指定类型+时间段）
     */
    @GetMapping("/trend")
    @Operation(summary = "获取数据趋势", description = "获取指定类型在时间段内的历史数据")
    public ApiResponse<List<HealthData>> getTrend(
            HttpServletRequest request,
            @RequestParam String dataType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long userId = getUserId(request);
        List<HealthData> list = healthDataService.getHistory(userId, dataType, startTime, endTime);
        return ApiResponse.success(list);
    }

    /**
     * 获取健康数据统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "获取用户各类型健康数据的统计信息")
    public ApiResponse<List<HealthDataMapper.HealthDataStatistics>> getStatistics(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<HealthDataMapper.HealthDataStatistics> stats = healthDataService.getStatistics(userId);
        return ApiResponse.success(stats);
    }

    /**
     * 更新健康数据
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新健康数据", description = "更新指定的健康数据")
    public ApiResponse<HealthData> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody HealthDataUploadDTO dto) {
        Long userId = getUserId(request);
        HealthData newData = buildHealthData(dto, userId);
        HealthData updated = healthDataService.updateHealthData(userId, id, newData);
        return ApiResponse.success("更新成功", updated);
    }

    /**
     * 删除健康数据
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除健康数据", description = "删除指定的健康数据")
    public ApiResponse<String> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        healthDataService.deleteHealthData(userId, id);
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

    private HealthData buildHealthData(HealthDataUploadDTO dto, Long userId) {
        return HealthData.builder()
                .userId(userId)
                .dataType(dto.getDataType())
                .dataValue(dto.getDataValue())
                .unit(dto.getUnit())
                .dataSource(dto.getDataSource() != null ? dto.getDataSource() : "manual")
                .deviceId(dto.getDeviceId())
                .collectedAt(dto.getCollectedAt())
                .build();
    }
}

