package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.Notification;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 获取用户所有通知（分页）
     */
    public PageInfo<Notification> getNotifications(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notification> list = notificationMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 获取用户未读通知
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadByUserId(userId);
    }

    /**
     * 获取未读通知数量
     */
    public Integer countUnread(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记单条通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ResourceNotFoundException("通知不存在: " + notificationId);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该通知");
        }
        notificationMapper.markAsRead(notificationId);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        log.info("用户所有通知已标记已读: userId={}", userId);
    }

    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ResourceNotFoundException("通知不存在: " + notificationId);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该通知");
        }
        notificationMapper.delete(notificationId);
    }
}

