package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.MedicalRecord;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.MedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 医疗记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * 获取用户所有医疗记录（分页）
     */
    public PageInfo<MedicalRecord> getRecords(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalRecord> list = medicalRecordMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 按类型获取用户医疗记录
     */
    public List<MedicalRecord> getRecordsByType(Long userId, String recordType) {
        return medicalRecordMapper.selectByUserIdAndType(userId, recordType);
    }

    /**
     * 根据ID获取医疗记录
     */
    public MedicalRecord getById(Long id, Long userId) {
        MedicalRecord record = medicalRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("医疗记录不存在: " + id);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该记录");
        }
        return record;
    }

    /**
     * 新增医疗记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MedicalRecord create(Long userId, MedicalRecord record) {
        record.setUserId(userId);
        medicalRecordMapper.insert(record);
        log.info("创建医疗记录成功: userId={}, id={}", userId, record.getId());
        return record;
    }

    /**
     * 更新医疗记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MedicalRecord update(Long userId, Long id, MedicalRecord newRecord) {
        MedicalRecord existing = medicalRecordMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("医疗记录不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该记录");
        }
        newRecord.setId(id);
        newRecord.setUserId(userId);
        int affected = medicalRecordMapper.update(newRecord);
        if (affected == 0) {
            throw new ResourceNotFoundException("医疗记录不存在或已被删除: " + id);
        }
        log.info("更新医疗记录成功: userId={}, id={}", userId, id);
        return medicalRecordMapper.selectById(id);
    }

    /**
     * 删除医疗记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        MedicalRecord existing = medicalRecordMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("医疗记录不存在: " + id);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该记录");
        }
        int affected = medicalRecordMapper.deleteByIdAndUserId(id, userId);
        if (affected == 0) {
            throw new ResourceNotFoundException("医疗记录不存在或已被删除: " + id);
        }
        log.info("删除医疗记录成功: userId={}, id={}", userId, id);
    }

    /**
     * 统计用户医疗记录数量
     */
    public Integer countByUserId(Long userId) {
        return medicalRecordMapper.countByUserId(userId);
    }
}

