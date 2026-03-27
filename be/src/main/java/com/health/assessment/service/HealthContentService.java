package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.HealthContent;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.HealthContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康内容库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthContentService {

    private final HealthContentMapper healthContentMapper;

    /**
     * 获取已发布内容列表（分页）
     */
    public PageInfo<HealthContent> getPublished(int pageNum, int pageSize, String contentType) {
        PageHelper.startPage(pageNum, pageSize);
        List<HealthContent> list = contentType != null
                ? healthContentMapper.selectPublishedByType(contentType)
                : healthContentMapper.selectAllPublished();
        return new PageInfo<>(list);
    }

    /**
     * 搜索内容（使用 LIKE，兼容性更好）
     */
    public List<HealthContent> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return healthContentMapper.selectAllPublished();
        }
        return healthContentMapper.searchByKeywordLike(keyword.trim());
    }

    /**
     * 根据ID获取内容详情（仅已发布）
     */
    @Cacheable(value = "healthContent", key = "#id")
    public HealthContent getById(Long id) {
        HealthContent content = healthContentMapper.selectById(id);
        if (content == null) {
            throw new ResourceNotFoundException("内容不存在: " + id);
        }
        return content;
    }

    /**
     * 获取全部内容（管理员）
     */
    public PageInfo<HealthContent> getAll(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<HealthContent> list = healthContentMapper.selectAll();
        return new PageInfo<>(list);
    }

    /**
     * 创建内容（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"healthContent", "healthContents"}, allEntries = true)
    public HealthContent create(HealthContent content) {
        if (content.getIsPublished() == null) {
            content.setIsPublished(0);
        }
        if (content.getIsPublished() == 1 && content.getPublishedAt() == null) {
            content.setPublishedAt(LocalDateTime.now());
        }
        healthContentMapper.insert(content);
        log.info("创建健康内容: id={}, title={}", content.getId(), content.getContentTitle());
        return content;
    }

    /**
     * 更新内容（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"healthContent", "healthContents"}, allEntries = true)
    public HealthContent update(Long id, HealthContent newContent) {
        HealthContent existing = healthContentMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("内容不存在: " + id);
        }
        // 如果从未发布变为发布，更新发布时间
        if (existing.getIsPublished() == 0 && newContent.getIsPublished() != null && newContent.getIsPublished() == 1) {
            newContent.setPublishedAt(LocalDateTime.now());
        }
        newContent.setId(id);
        healthContentMapper.update(newContent);
        log.info("更新健康内容: id={}", id);
        return healthContentMapper.selectById(id);
    }

    /**
     * 删除内容（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"healthContent", "healthContents"}, allEntries = true)
    public void delete(Long id) {
        HealthContent existing = healthContentMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("内容不存在: " + id);
        }
        healthContentMapper.deleteById(id);
        log.info("删除健康内容: id={}", id);
    }

    /**
     * 发布/取消发布（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"healthContent", "healthContents"}, allEntries = true)
    public HealthContent togglePublish(Long id) {
        HealthContent existing = healthContentMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("内容不存在: " + id);
        }
        if (existing.getIsPublished() == 1) {
            existing.setIsPublished(0);
        } else {
            existing.setIsPublished(1);
            existing.setPublishedAt(LocalDateTime.now());
        }
        healthContentMapper.update(existing);
        log.info("切换发布状态: id={}, published={}", id, existing.getIsPublished());
        return healthContentMapper.selectById(id);
    }
}

