package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.HealthData;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.HealthDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据服务
 *
 * 功能：
 * - 上传/查询/更新/删除健康数据
 * - 数据统计分析
 * - 趋势数据获取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDataService {

    private final HealthDataMapper healthDataMapper;

    /**
     * 上传单条健康数据
     */
    @Transactional(rollbackFor = Exception.class)
    public HealthData uploadHealthData(Long userId, HealthData healthData) {
        log.info("上传健康数据: userId={}, type={}", userId, healthData.getDataType());

        healthData.setUserId(userId);
        if (healthData.getCollectedAt() == null) {
            healthData.setCollectedAt(LocalDateTime.now());
        }
        healthData.setCreatedAt(LocalDateTime.now());

        int result = healthDataMapper.insert(healthData);
        if (result > 0) {
            log.info("健康数据上传成功: id={}", healthData.getId());
            return healthData;
        } else {
            throw new BusinessException("健康数据上传失败");
        }
    }

    /**
     * 批量上传健康数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpload(Long userId, List<HealthData> healthDataList) {
        log.info("批量上传健康数据: userId={}, count={}", userId, healthDataList.size());

        LocalDateTime now = LocalDateTime.now();
        healthDataList.forEach(hd -> {
            hd.setUserId(userId);
            if (hd.getCollectedAt() == null) {
                hd.setCollectedAt(now);
            }
            hd.setCreatedAt(now);
        });

        int result = healthDataMapper.batchInsert(healthDataList);
        log.info("批量上传成功: count={}", result);
        return result;
    }

    /**
     * 根据ID获取健康数据
     */
    public HealthData getById(Long id) {
        HealthData data = healthDataMapper.selectById(id);
        if (data == null) {
            throw new ResourceNotFoundException("健康数据不存在: " + id);
        }
        return data;
    }

    /**
     * 获取用户最新健康数据（各类型最新一条）
     */
    public List<HealthData> getLatestAllTypes(Long userId) {
        log.debug("获取用户最新健康数据: userId={}", userId);
        return healthDataMapper.selectLatestAllTypes(userId);
    }

    /**
     * 获取用户最近N条健康数据
     */
    public List<HealthData> getLatest(Long userId, int limit) {
        log.debug("获取最近{}条健康数据: userId={}", limit, userId);
        return healthDataMapper.selectLatestByUserId(userId, limit);
    }

    /**
     * 获取用户指定类型、时间段的历史数据
     */
    public List<HealthData> getHistory(Long userId, String dataType,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("获取历史数据: userId={}, type={}, start={}, end={}", userId, dataType, startTime, endTime);
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        return healthDataMapper.selectByUserIdAndType(userId, dataType, startTime, endTime);
    }

    /**
     * 获取用户指定时间段的所有健康数据（分页）
     */
    public PageInfo<HealthData> getPagedData(Long userId, LocalDateTime startTime,
                                             LocalDateTime endTime, int pageNum, int pageSize) {
        log.debug("分页查询健康数据: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        PageHelper.startPage(pageNum, pageSize);
        List<HealthData> list = healthDataMapper.selectByUserIdAndTimeRange(userId, startTime, endTime);
        return new PageInfo<>(list);
    }

    /**
     * 更新健康数据
     */
    @Transactional(rollbackFor = Exception.class)
    public HealthData updateHealthData(Long userId, Long id, HealthData newData) {
        HealthData existing = healthDataMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("健康数据不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该数据");
        }

        newData.setId(id);
        healthDataMapper.update(newData);
        log.info("健康数据更新成功: id={}", id);
        return healthDataMapper.selectById(id);
    }

    /**
     * 删除健康数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteHealthData(Long userId, Long id) {
        HealthData existing = healthDataMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("健康数据不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该数据");
        }

        healthDataMapper.delete(id);
        log.info("健康数据删除成功: id={}", id);
    }

    /**
     * 获取用户健康数据统计
     */
    public List<HealthDataMapper.HealthDataStatistics> getStatistics(Long userId) {
        log.debug("获取健康数据统计: userId={}", userId);
        return healthDataMapper.getStatistics(userId);
    }

    /**
     * 获取用户拥有的数据类型数量
     */
    public Integer countDataTypes(Long userId) {
        return healthDataMapper.countDataTypes(userId);
    }
}

