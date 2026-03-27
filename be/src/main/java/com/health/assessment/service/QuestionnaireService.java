package com.health.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.Questionnaire;
import com.health.assessment.entity.QuestionnaireResponse;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.QuestionnaireMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问卷服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final QuestionnaireMapper questionnaireMapper;
    private final ObjectMapper objectMapper;

    // ==================== 问卷定义相关 ====================

    /**
     * 获取所有启用的问卷列表
     */
    @Cacheable(value = "questionnaires", key = "'active'")
    public List<Questionnaire> getActiveQuestionnaires() {
        return questionnaireMapper.selectAllActive();
    }

    /**
     * 按类型获取问卷
     */
    @Cacheable(value = "questionnaires", key = "'type:' + #type")
    public List<Questionnaire> getByType(String type) {
        return questionnaireMapper.selectByType(type);
    }

    /**
     * 根据ID获取问卷详情
     */
    @Cacheable(value = "questionnaires", key = "#id")
    public Questionnaire getById(Long id) {
        Questionnaire q = questionnaireMapper.selectById(id);
        if (q == null) {
            throw new ResourceNotFoundException("问卷不存在: " + id);
        }
        return q;
    }

    /**
     * 获取所有问卷（管理员用）
     */
    public List<Questionnaire> getAll() {
        return questionnaireMapper.selectAll();
    }

    /**
     * 创建问卷（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questionnaires", allEntries = true)
    public Questionnaire create(Questionnaire questionnaire) {
        if (questionnaire.getIsActive() == null) {
            questionnaire.setIsActive(1);
        }
        questionnaireMapper.insert(questionnaire);
        log.info("创建问卷: id={}, title={}", questionnaire.getId(), questionnaire.getTitle());
        return questionnaire;
    }

    /**
     * 更新问卷（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questionnaires", allEntries = true)
    public Questionnaire update(Long id, Questionnaire questionnaire) {
        Questionnaire existing = questionnaireMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("问卷不存在: " + id);
        }
        questionnaire.setId(id);
        questionnaireMapper.update(questionnaire);
        log.info("更新问卷: id={}", id);
        return questionnaireMapper.selectById(id);
    }

    /**
     * 删除问卷（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "questionnaires", allEntries = true)
    public void delete(Long id) {
        Questionnaire existing = questionnaireMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("问卷不存在: " + id);
        }
        questionnaireMapper.deleteById(id);
        log.info("删除问卷: id={}", id);
    }

    // ==================== 问卷回答相关 ====================

    /**
     * 提交问卷回答
     *
     * @param userId          用户ID
     * @param questionnaireId 问卷ID
     * @param answers         用户的回答（key=题目ID，value=答案）
     * @return 保存的回答记录
     */
    @Transactional(rollbackFor = Exception.class)
    public QuestionnaireResponse submitResponse(Long userId, Long questionnaireId, Map<String, Object> answers) {
        // 验证问卷存在
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new ResourceNotFoundException("问卷不存在: " + questionnaireId);
        }
        if (questionnaire.getIsActive() == null || questionnaire.getIsActive() == 0) {
            throw new BusinessException("该问卷已停用");
        }

        // 计算得分
        BigDecimal score = calculateScore(questionnaire, answers);

        // 序列化回答数据
        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            log.error("序列化回答数据失败", e);
            responseJson = "{}";
        }

        QuestionnaireResponse response = QuestionnaireResponse.builder()
                .userId(userId)
                .questionnaireId(questionnaireId)
                .responseData(responseJson)
                .score(score)
                .completedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        questionnaireMapper.insertResponse(response);
        log.info("提交问卷回答成功: userId={}, questionnaireId={}, score={}", userId, questionnaireId, score);

        // 填充关联字段
        response.setQuestionnaireTitle(questionnaire.getTitle());
        response.setQuestionnaireType(questionnaire.getQuestionnaireType());
        return response;
    }

    /**
     * 获取用户问卷回答历史（分页）
     */
    public PageInfo<QuestionnaireResponse> getMyResponses(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<QuestionnaireResponse> list = questionnaireMapper.selectResponsesByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 获取某次回答详情
     */
    public QuestionnaireResponse getResponseById(Long id, Long userId) {
        QuestionnaireResponse response = questionnaireMapper.selectResponseById(id);
        if (response == null) {
            throw new ResourceNotFoundException("回答记录不存在: " + id);
        }
        if (!response.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该记录");
        }
        return response;
    }

    /**
     * 获取用户对某问卷的最近一次回答
     */
    public QuestionnaireResponse getLatestResponse(Long userId, Long questionnaireId) {
        return questionnaireMapper.selectLatestResponseByUserAndQuestionnaire(userId, questionnaireId);
    }

    /**
     * 统计用户已完成的问卷数量
     */
    public Integer countMyResponses(Long userId) {
        return questionnaireMapper.countResponsesByUserId(userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据问卷题目定义和用户回答计算总分
     * 如果问卷没有定义分值，则返回100分（满分）
     */
    private BigDecimal calculateScore(Questionnaire questionnaire, Map<String, Object> answers) {
        try {
            String questionsJson = questionnaire.getQuestions();
            if (questionsJson == null || questionsJson.isEmpty()) {
                return BigDecimal.valueOf(100);
            }

            JsonNode questions = objectMapper.readTree(questionsJson);
            if (!questions.isArray() || questions.size() == 0) {
                return BigDecimal.valueOf(100);
            }

            double totalScore = 0;
            double maxScore = 0;

            for (JsonNode question : questions) {
                String qId = question.path("id").asText();
                double qMaxScore = question.path("score").asDouble(0);
                maxScore += qMaxScore;

                if (answers.containsKey(qId) && question.has("options")) {
                    Object userAnswer = answers.get(qId);
                    JsonNode options = question.get("options");

                    for (JsonNode option : options) {
                        String optionValue = option.path("value").asText();
                        double optionScore = option.path("score").asDouble(0);
                        if (userAnswer != null && userAnswer.toString().contains(optionValue)) {
                            totalScore += optionScore;
                            break;
                        }
                    }
                }
            }

            if (maxScore == 0) {
                return BigDecimal.valueOf(100);
            }

            double percentage = (totalScore / maxScore) * 100;
            return BigDecimal.valueOf(Math.round(percentage * 100.0) / 100.0);

        } catch (Exception e) {
            log.warn("计算问卷得分失败: {}", e.getMessage());
            return BigDecimal.valueOf(100);
        }
    }
}

