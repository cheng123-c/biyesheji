package com.health.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * DeepSeek AI 服务
 *
 * 负责与 DeepSeek API 交互，提供 AI 健康分析能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1}")
    private String apiEndpoint;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.api.timeout:30000}")
    private int timeout;

    @Value("${deepseek.api.max-retries:3}")
    private int maxRetries;

    private final ObjectMapper objectMapper;

    /**
     * 分析健康数据并生成评估结果
     *
     * @param healthDataSummary 健康数据摘要（JSON字符串）
     * @param userInfo 用户基本信息
     * @return AI 分析结果（JSON字符串）
     */
    public String analyzeHealthData(String healthDataSummary, Map<String, Object> userInfo) {
        log.info("调用 DeepSeek AI 进行健康分析");

        // 如果没有配置 API Key，使用本地简单评估
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置，使用本地评估模式");
            return generateLocalAssessment(healthDataSummary, userInfo);
        }

        String prompt = buildHealthAnalysisPrompt(healthDataSummary, userInfo);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String result = callDeepSeekAPI(prompt);
                log.info("DeepSeek AI 分析成功");
                return result;
            } catch (Exception e) {
                log.warn("DeepSeek AI 调用失败 (第{}次): {}", attempt, e.getMessage());
                if (attempt == maxRetries) {
                    log.error("DeepSeek AI 多次调用失败，使用本地评估模式");
                    return generateLocalAssessment(healthDataSummary, userInfo);
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return generateLocalAssessment(healthDataSummary, userInfo);
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位专业的健康评估AI助手，擅长分析健康数据并提供个性化的健康建议。请用JSON格式返回评估结果。");

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        requestBody.put("response_format", objectMapper.createObjectNode().put("type", "json_object"));

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiEndpoint + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("choices").path(0).path("message").path("content").asText();
        }

        throw new RuntimeException("DeepSeek API 返回异常: " + response.getStatusCode());
    }

    /**
     * 构建健康分析提示词
     */
    private String buildHealthAnalysisPrompt(String healthDataSummary, Map<String, Object> userInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下用户的健康数据进行综合评估分析：\n\n");

        sb.append("【用户信息】\n");
        if (userInfo != null) {
            userInfo.forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append("\n"));
        }

        sb.append("\n【健康数据摘要】\n");
        sb.append(healthDataSummary);

        sb.append("\n\n请以JSON格式返回以下内容：\n");
        sb.append("{\n");
        sb.append("  \"physiologicalAge\": 生理年龄(整数),\n");
        sb.append("  \"healthScore\": 健康评分(0-100),\n");
        sb.append("  \"diseaseRiskScore\": 疾病风险评分(0-100),\n");
        sb.append("  \"diseaseRiskLevel\": 风险等级(LOW/MEDIUM/HIGH/CRITICAL),\n");
        sb.append("  \"scoreBreakdown\": {\n");
        sb.append("    \"cardiovascular\": 心血管评分(0-100),\n");
        sb.append("    \"metabolic\": 代谢评分(0-100),\n");
        sb.append("    \"lifestyle\": 生活方式评分(0-100)\n");
        sb.append("  },\n");
        sb.append("  \"predictedDiseases\": [\"可能风险疾病列表\"],\n");
        sb.append("  \"recommendations\": [\"具体健康建议列表\"],\n");
        sb.append("  \"summary\": \"健康状况总体描述\",\n");
        sb.append("  \"confidenceScore\": 置信度(0-100)\n");
        sb.append("}\n");
        sb.append("请根据健康数据的实际情况，提供专业、准确的评估。如果某类数据不足，请在评估中说明。");

        return sb.toString();
    }

    /**
     * 本地简单评估（当 AI API 不可用时的降级方案）
     */
    private String generateLocalAssessment(String healthDataSummary, Map<String, Object> userInfo) {
        log.info("使用本地评估模式");

        try {
            // 解析健康数据，进行基础评分
            JsonNode dataNode = objectMapper.readTree(healthDataSummary);
            double healthScore = 75.0;
            String riskLevel = "LOW";
            String summary = "根据您的健康数据进行了基础评估。";

            // 简单的规则评估
            // 注意：extractFeatures 返回的结构为 {"heart_rate": {"latest":75, "avg":77.5, ...}}
            // 使用 path() 避免 NPE，读取 "latest" 最新值进行判断
            if (dataNode.has("heart_rate")) {
                double hr = dataNode.path("heart_rate").path("latest").asDouble(0);
                if (hr > 0 && (hr < 60 || hr > 100)) {
                    healthScore -= 10;
                    riskLevel = "MEDIUM";
                    summary += " 心率异常，需关注。";
                }
            }

            if (dataNode.has("blood_pressure_systolic")) {
                double sbp = dataNode.path("blood_pressure_systolic").path("latest").asDouble(0);
                if (sbp >= 140) {
                    healthScore -= 15;
                    riskLevel = "HIGH";
                    summary += " 收缩压偏高，建议就医检查。";
                } else if (sbp >= 130) {
                    healthScore -= 8;
                    riskLevel = "MEDIUM";
                    summary += " 血压偏高，建议关注。";
                }
            }

            if (dataNode.has("blood_glucose")) {
                double bg = dataNode.path("blood_glucose").path("latest").asDouble(0);
                if (bg >= 126) {
                    healthScore -= 20;
                    riskLevel = "HIGH";
                    summary += " 血糖偏高，建议专业检查。";
                } else if (bg >= 100) {
                    healthScore -= 10;
                    if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM";
                    summary += " 血糖轻度偏高，需要控制饮食。";
                }
            }

            // 检查血氧
            if (dataNode.has("blood_oxygen")) {
                double bo = dataNode.path("blood_oxygen").path("latest").asDouble(0);
                if (bo > 0 && bo < 95) {
                    healthScore -= 15;
                    if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM";
                    summary += " 血氧偏低，建议关注呼吸系统健康。";
                }
            }

            // 检查体温
            if (dataNode.has("body_temperature")) {
                double bt = dataNode.path("body_temperature").path("latest").asDouble(0);
                if (bt > 0 && (bt < 36.0 || bt > 37.5)) {
                    healthScore -= 5;
                    summary += " 体温异常，请注意身体状况。";
                }
            }

            healthScore = Math.max(0, Math.min(100, healthScore));

            // 计算生理年龄：尝试解析用户实际年龄，若失败则默认35
            int physiologicalAge = 35;
            if (userInfo != null && userInfo.containsKey("age")) {
                try {
                    int actualAge = Integer.parseInt(String.valueOf(userInfo.get("age")));
                    // 健康评分低于75则生理年龄偏大，评分越高越接近实际年龄
                    double ageFactor = healthScore > 0 ? (100.0 / healthScore) : 1.0;
                    physiologicalAge = (int) Math.min(120, Math.max(1, actualAge * ageFactor));
                } catch (NumberFormatException ignored) {
                    // age 为"未知"等非数字时使用默认值35
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("physiologicalAge", physiologicalAge);
            result.put("healthScore", healthScore);
            result.put("diseaseRiskScore", 100 - healthScore);
            result.put("diseaseRiskLevel", riskLevel);
            // 各维度评分限制在 [0, 100] 范围内
            result.put("scoreBreakdown", Map.of(
                    "cardiovascular", Math.max(0, Math.min(100, healthScore - 5)),
                    "metabolic", Math.max(0, Math.min(100, healthScore)),
                    "lifestyle", Math.max(0, Math.min(100, healthScore + 5))
            ));
            result.put("predictedDiseases", riskLevel.equals("LOW") ?
                    new String[]{} : new String[]{"高血压风险", "代谢综合征"});
            result.put("recommendations", new String[]{
                    "保持规律作息，每天保证7-8小时睡眠",
                    "坚持适量运动，每周至少150分钟中等强度运动",
                    "注意饮食均衡，减少高糖高脂食物摄入",
                    "定期监测健康指标，及时发现异常"
            });
            result.put("summary", summary);
            result.put("confidenceScore", 70.0);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("本地评估失败: {}", e.getMessage());
            return "{\"healthScore\":70,\"diseaseRiskLevel\":\"LOW\",\"summary\":\"评估数据不足，请上传更多健康数据\",\"confidenceScore\":50}";
        }
    }
}

