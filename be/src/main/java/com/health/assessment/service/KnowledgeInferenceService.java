package com.health.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.health.assessment.entity.InferenceResult;
import com.health.assessment.entity.KnowledgeRelation;
import com.health.assessment.entity.MedicalConcept;
import com.health.assessment.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识图谱推理服务
 * 结合知识图谱和 DeepSeek AI 对用户输入的症状进行智能推理分析
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeInferenceService {

    private final KnowledgeMapper knowledgeMapper;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1}")
    private String apiEndpoint;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    // ===== 公开接口方法 =====

    /**
     * 获取所有症状列表（供前端展示选择）
     */
    @Cacheable(value = "medicalConcepts", key = "'symptoms'")
    public List<MedicalConcept> getAllSymptoms() {
        return knowledgeMapper.selectConceptsByType("SYMPTOM");
    }

    /**
     * 获取所有疾病列表
     */
    @Cacheable(value = "medicalConcepts", key = "'diseases'")
    public List<MedicalConcept> getAllDiseases() {
        return knowledgeMapper.selectConceptsByType("DISEASE");
    }

    /**
     * 搜索医学概念
     */
    public List<MedicalConcept> searchConcepts(String keyword) {
        return knowledgeMapper.searchConcepts(keyword);
    }

    /**
     * 根据概念ID查询其关联关系（知识图谱可视化用）
     */
    @Cacheable(value = "conceptRelations", key = "#conceptId")
    public List<KnowledgeRelation> getRelationsByConceptId(Long conceptId) {
        return knowledgeMapper.selectRelationsByConceptId(conceptId);
    }

    /**
     * 核心推理功能：根据输入的症状名称列表，推断可能的疾病，并生成 AI 分析建议
     *
     * @param userId       用户ID（为null则不保存结果）
     * @param symptomNames 用户描述的症状名称列表
     * @return 推理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public InferenceResult inferFromSymptoms(Long userId, List<String> symptomNames) {
        log.info("开始知识图谱推理: userId={}, symptoms={}", userId, symptomNames);

        // 1. 在知识图谱中匹配症状概念
        List<MedicalConcept> matchedSymptoms = new ArrayList<>();
        for (String name : symptomNames) {
            List<MedicalConcept> results = knowledgeMapper.searchConcepts(name);
            results.stream()
                    .filter(c -> "SYMPTOM".equals(c.getConceptType()))
                    .findFirst()
                    .ifPresent(matchedSymptoms::add);
        }

        // 2. 通过知识图谱关系查找可能的疾病
        Map<String, Double> diseaseScores = new LinkedHashMap<>();
        if (!matchedSymptoms.isEmpty()) {
            List<Long> symptomIds = matchedSymptoms.stream().map(MedicalConcept::getId).collect(Collectors.toList());
            List<KnowledgeRelation> relations = knowledgeMapper.selectDiseasesBySymptomIds(symptomIds);

            // 统计每个疾病被多少症状匹配到（出现次数越多得分越高）
            Map<String, Integer> diseaseHitCount = new LinkedHashMap<>();
            Map<String, Double> diseaseMaxConf = new LinkedHashMap<>();
            for (KnowledgeRelation rel : relations) {
                String diseaseName = rel.getSourceConceptName();
                if (diseaseName != null) {
                    diseaseHitCount.merge(diseaseName, 1, Integer::sum);
                    double conf = rel.getConfidenceScore() != null ? rel.getConfidenceScore().doubleValue() : 0.5;
                    diseaseMaxConf.merge(diseaseName, conf, Math::max);
                }
            }
            // 综合得分 = 命中率 * 置信度
            int totalSymptoms = symptomNames.size();
            diseaseHitCount.forEach((disease, hits) -> {
                double hitRate = (double) hits / totalSymptoms;
                double conf = diseaseMaxConf.getOrDefault(disease, 0.5);
                diseaseScores.put(disease, hitRate * conf * 100);
            });

            // 排序，取前5
            diseaseScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }

        // 3. 调用 DeepSeek AI 生成分析建议
        String aiAnalysis = generateSymptomAnalysis(symptomNames, new ArrayList<>(diseaseScores.keySet()));

        // 4. 计算综合风险评分
        double riskScore = calculateRiskScore(diseaseScores, symptomNames.size());

        // 5. 组装推理结果
        InferenceResult result = buildInferenceResult(userId, symptomNames, diseaseScores, aiAnalysis, riskScore);

        // 6. 保存到数据库（如果有用户ID）
        if (userId != null) {
            knowledgeMapper.insertInferenceResult(result);
            log.info("保存推理结果: userId={}, id={}", userId, result.getId());
        }

        return result;
    }

    /**
     * 获取用户历史推理记录
     */
    public List<InferenceResult> getHistoryResults(Long userId, int limit) {
        return knowledgeMapper.selectInferenceResultsByUserId(userId, limit);
    }

    /**
     * 管理员接口：获取全部概念
     */
    @Cacheable(value = "medicalConcepts", key = "'all'")
    public List<MedicalConcept> getAllConcepts() {
        return knowledgeMapper.selectAllConcepts();
    }

    /**
     * 管理员接口：新增概念
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"medicalConcepts", "conceptRelations"}, allEntries = true)
    public MedicalConcept createConcept(MedicalConcept concept) {
        knowledgeMapper.insertConcept(concept);
        return knowledgeMapper.selectConceptById(concept.getId());
    }

    /**
     * 管理员接口：更新概念
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"medicalConcepts", "conceptRelations"}, allEntries = true)
    public MedicalConcept updateConcept(Long id, MedicalConcept concept) {
        concept.setId(id);
        knowledgeMapper.updateConcept(concept);
        return knowledgeMapper.selectConceptById(id);
    }

    /**
     * 管理员接口：删除概念
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"medicalConcepts", "conceptRelations"}, allEntries = true)
    public void deleteConcept(Long id) {
        knowledgeMapper.deleteConceptById(id);
    }

    /**
     * 管理员接口：新增关系
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "conceptRelations", allEntries = true)
    public void createRelation(KnowledgeRelation relation) {
        knowledgeMapper.insertRelation(relation);
    }

    /**
     * 管理员接口：删除关系
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "conceptRelations", allEntries = true)
    public void deleteRelation(Long id) {
        knowledgeMapper.deleteRelationById(id);
    }

    // ===== 私有方法 =====

    private String generateSymptomAnalysis(List<String> symptoms, List<String> inferredDiseases) {
        if (apiKey == null || apiKey.isEmpty()) {
            return generateLocalSymptomAnalysis(symptoms, inferredDiseases);
        }
        try {
            String prompt = buildSymptomAnalysisPrompt(symptoms, inferredDiseases);
            return callDeepSeekForAnalysis(prompt);
        } catch (Exception e) {
            log.warn("DeepSeek 症状分析失败，使用本地分析: {}", e.getMessage());
            return generateLocalSymptomAnalysis(symptoms, inferredDiseases);
        }
    }

    private String callDeepSeekForAnalysis(String prompt) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode sysMsg = messages.addObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一位专业的健康顾问，擅长根据症状分析可能的健康问题并给出建议。请用中文回答，语言友好、专业。");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 1000);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                apiEndpoint + "/chat/completions", HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(response.getBody());
            return json.path("choices").path(0).path("message").path("content").asText();
        }
        throw new RuntimeException("DeepSeek API 调用失败");
    }

    private String buildSymptomAnalysisPrompt(List<String> symptoms, List<String> inferredDiseases) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户描述了以下症状：").append(String.join("、", symptoms)).append("\n\n");
        if (!inferredDiseases.isEmpty()) {
            sb.append("根据医学知识图谱分析，可能相关的疾病包括：").append(String.join("、", inferredDiseases)).append("\n\n");
        }
        sb.append("请提供：\n");
        sb.append("1. 对这些症状的简要分析（2-3句话）\n");
        sb.append("2. 需要注意的健康风险\n");
        sb.append("3. 建议的行动（是否需要就医，日常注意事项等）\n");
        sb.append("4. 免责声明：本分析仅供参考，不能替代专业医疗诊断\n");
        return sb.toString();
    }

    private String generateLocalSymptomAnalysis(List<String> symptoms, List<String> inferredDiseases) {
        StringBuilder sb = new StringBuilder();
        sb.append("【症状分析报告】\n\n");
        sb.append("您描述的症状：").append(String.join("、", symptoms)).append("\n\n");
        if (!inferredDiseases.isEmpty()) {
            sb.append("根据症状匹配，可能需要关注以下健康问题：\n");
            inferredDiseases.forEach(d -> sb.append("• ").append(d).append("\n"));
            sb.append("\n");
        }
        sb.append("建议：\n");
        sb.append("• 如症状持续或加重，请及时就医\n");
        sb.append("• 保持良好作息，注意饮食均衡\n");
        sb.append("• 可结合AI健康评测进行更全面的健康分析\n\n");
        sb.append("⚠️ 本分析仅供参考，不能替代专业医疗诊断，请遵循医嘱。");
        return sb.toString();
    }

    private double calculateRiskScore(Map<String, Double> diseaseScores, int symptomCount) {
        if (diseaseScores.isEmpty()) return 10.0;
        double maxScore = diseaseScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avgScore = diseaseScores.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        // 综合计算：最高分权重60% + 平均分权重40% + 症状数量加权
        double base = maxScore * 0.6 + avgScore * 0.4;
        double symptomFactor = Math.min(1.0 + (symptomCount - 1) * 0.1, 1.5);
        return Math.min(base * symptomFactor, 100.0);
    }

    private InferenceResult buildInferenceResult(Long userId, List<String> symptoms,
                                                  Map<String, Double> diseaseScores,
                                                  String aiAnalysis, double riskScore) {
        // 序列化症状列表
        String inputSymptomsJson;
        String inferredDiseasesJson;
        try {
            inputSymptomsJson = objectMapper.writeValueAsString(symptoms);
            // 组装疾病数组（含分数）
            List<Map<String, Object>> diseaseList = diseaseScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", e.getKey());
                        m.put("score", Math.round(e.getValue() * 10.0) / 10.0);
                        return m;
                    })
                    .collect(Collectors.toList());
            inferredDiseasesJson = objectMapper.writeValueAsString(diseaseList);
        } catch (Exception e) {
            log.error("序列化推理结果失败", e);
            inputSymptomsJson = "[]";
            inferredDiseasesJson = "[]";
        }

        return InferenceResult.builder()
                .userId(userId)
                .inputSymptoms(inputSymptomsJson)
                .inferredDiseases(inferredDiseasesJson)
                .aiAnalysis(aiAnalysis)
                .riskScore(BigDecimal.valueOf(Math.round(riskScore * 10.0) / 10.0))
                .build();
    }
}

