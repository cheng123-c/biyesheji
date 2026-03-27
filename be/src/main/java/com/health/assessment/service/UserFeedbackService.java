package com.health.assessment.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.UserFeedback;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.UserFeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户反馈服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackService {

    private final UserFeedbackMapper userFeedbackMapper;

    /**
     * 用户提交反馈
     */
    @Transactional(rollbackFor = Exception.class)
    public UserFeedback submit(Long userId, UserFeedback feedback) {
        feedback.setUserId(userId);
        feedback.setStatus("PENDING");
        userFeedbackMapper.insert(feedback);
        log.info("用户提交反馈: userId={}, type={}", userId, feedback.getFeedbackType());
        return userFeedbackMapper.selectById(feedback.getId());
    }

    /**
     * 获取我的反馈列表
     */
    public List<UserFeedback> getMyFeedbacks(Long userId) {
        return userFeedbackMapper.selectByUserId(userId);
    }

    /**
     * 获取反馈详情（用户只能查自己的）
     */
    public UserFeedback getById(Long id, Long userId) {
        UserFeedback feedback = userFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new ResourceNotFoundException("反馈不存在: " + id);
        }
        if (userId != null && !feedback.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看该反馈");
        }
        return feedback;
    }

    /**
     * 管理员获取全部反馈（分页）
     */
    public PageInfo<UserFeedback> adminGetAll(int pageNum, int pageSize, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserFeedback> list = status != null
                ? userFeedbackMapper.selectByStatus(status)
                : userFeedbackMapper.selectAll();
        return new PageInfo<>(list);
    }

    /**
     * 管理员回复反馈
     */
    @Transactional(rollbackFor = Exception.class)
    public UserFeedback adminReply(Long id, String reply, String status) {
        UserFeedback existing = userFeedbackMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("反馈不存在: " + id);
        }
        existing.setAdminReply(reply);
        existing.setStatus(status != null ? status : "RESOLVED");
        userFeedbackMapper.updateReply(existing);
        log.info("管理员回复反馈: id={}", id);
        return userFeedbackMapper.selectById(id);
    }

    /**
     * 管理员更改状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(Long id, String status) {
        UserFeedback existing = userFeedbackMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("反馈不存在: " + id);
        }
        userFeedbackMapper.updateStatus(id, status);
        log.info("管理员更改反馈状态: id={}, status={}", id, status);
    }

    /**
     * 统计待处理反馈数量
     */
    public Integer countPending() {
        return userFeedbackMapper.countPending();
    }
}

