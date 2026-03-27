package com.health.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.assessment.entity.Assessment;
import com.health.assessment.entity.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * PDF 导出服务
 *
 * 功能：
 * - 将健康评测报告导出为 PDF 文件
 * - 支持中文显示（优先使用 classpath 内嵌 TTF，回退到 itext-asian STSong-Light）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final ObjectMapper objectMapper;

    // 颜色常量
    private static final BaseColor COLOR_PRIMARY   = new BaseColor(102, 126, 234);
    private static final BaseColor COLOR_SUCCESS   = new BaseColor(0, 184, 148);
    private static final BaseColor COLOR_WARNING   = new BaseColor(253, 203, 110);
    private static final BaseColor COLOR_DANGER    = new BaseColor(225, 112, 85);
    private static final BaseColor COLOR_CRITICAL  = new BaseColor(231, 76, 60);
    private static final BaseColor COLOR_LIGHT_BG  = new BaseColor(248, 249, 255);
    private static final BaseColor COLOR_BORDER    = new BaseColor(220, 220, 220);
    private static final BaseColor COLOR_TEXT_GRAY = new BaseColor(102, 102, 102);
    private static final BaseColor COLOR_TEXT_DARK = new BaseColor(51, 51, 51);
    private static final BaseColor COLOR_WHITE     = BaseColor.WHITE;

    /**
     * 加载中文字体（优先 classpath 内嵌 TTF，回退到 itext-asian CJK 字体）
     */
    private BaseFont loadChineseFont() throws Exception {
        // 优先尝试 classpath 下的 NotoSansSC-Regular.ttf
        try {
            ClassPathResource res = new ClassPathResource("fonts/NotoSansSC-Regular.ttf");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    byte[] fontBytes = is.readAllBytes();
                    return BaseFont.createFont("NotoSansSC-Regular.ttf",
                            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
                }
            }
        } catch (Exception e) {
            log.debug("未找到 NotoSansSC 字体，尝试 itext-asian: {}", e.getMessage());
        }

        // 回退：使用 itext-asian 内置 STSong-Light
        try {
            return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            log.debug("itext-asian STSong-Light 不可用: {}", e.getMessage());
        }

        // 最终回退：系统字体路径列表
        String[] systemFontPaths = {
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
            "/System/Library/Fonts/PingFang.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
        };
        for (String path : systemFontPaths) {
            try {
                return BaseFont.createFont(path + ",0", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            } catch (Exception ignored) {
                // 继续尝试下一个
            }
        }

        log.warn("未找到任何中文字体，PDF 中文可能无法正常显示");
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }

    /**
     * 生成健康评测报告 PDF
     *
     * @param assessment 评测报告
     * @param user       用户信息（可为空）
     * @return PDF 字节数组
     */
    public byte[] generateAssessmentPdf(Assessment assessment, User user) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            BaseFont chineseFont = loadChineseFont();

            Font headingFont = new Font(chineseFont, 14, Font.BOLD, COLOR_PRIMARY);
            Font bodyFont    = new Font(chineseFont, 11, Font.NORMAL, COLOR_TEXT_DARK);
            Font smallFont   = new Font(chineseFont, 10, Font.NORMAL, COLOR_TEXT_GRAY);

            // 标题区域
            addTitle(document, chineseFont, assessment, user);

            // 基本信息卡片
            addBasicInfoCard(document, chineseFont, assessment, user);

            // 健康摘要
            addSectionTitle(document, "健康摘要", headingFont, chineseFont);
            addContentBlock(document, assessment.getSummary(), bodyFont);

            // 健康建议
            if (assessment.getRecommendation() != null && !assessment.getRecommendation().isEmpty()) {
                addSectionTitle(document, "健康建议", headingFont, chineseFont);
                addRecommendationBlock(document, assessment.getRecommendation(), chineseFont);
            }

            // AI 分析详情
            if (assessment.getAiAnalysis() != null) {
                addAiAnalysisSection(document, assessment.getAiAnalysis(), chineseFont, headingFont, bodyFont, smallFont);
            }

            // 免责声明
            addDisclaimer(document, chineseFont);

            document.close();
            log.info("PDF 生成成功: assessmentId={}", assessment.getId());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF 生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 生成失败: " + e.getMessage());
        }
    }

    // ============ 私有构建方法 ============

    private void addTitle(Document doc, BaseFont font, Assessment assessment, User user)
            throws DocumentException {
        Font mainTitleFont = new Font(font, 24, Font.BOLD, COLOR_PRIMARY);
        Font subFont       = new Font(font, 12, Font.NORMAL, COLOR_TEXT_GRAY);

        Paragraph title = new Paragraph("个人健康评测报告", mainTitleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        doc.add(title);

        String userName = (user != null && user.getRealName() != null && !user.getRealName().isEmpty())
                ? user.getRealName()
                : (user != null ? user.getUsername() : "用户");
        String dateStr = assessment.getAssessmentDate() != null
                ? assessment.getAssessmentDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
                : "未知日期";
        Paragraph sub = new Paragraph(userName + " · " + dateStr, subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingBefore(6);
        sub.setSpacingAfter(20);
        doc.add(sub);

        LineSeparator ls = new LineSeparator(1, 100, COLOR_BORDER, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);
    }

    private void addBasicInfoCard(Document doc, BaseFont font, Assessment assessment, User user)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(16);
        table.setSpacingAfter(20);

        String scoreStr = assessment.getOverallScore() != null
                ? assessment.getOverallScore().toPlainString() + " 分" : "—";
        addInfoCell(table, font, "健康评分", scoreStr, COLOR_PRIMARY);

        String riskLabel = getRiskLabel(assessment.getRiskLevel());
        BaseColor riskColor = getRiskColor(assessment.getRiskLevel());
        addInfoCell(table, font, "风险等级", riskLabel, riskColor);

        String dateStr = assessment.getAssessmentDate() != null
                ? assessment.getAssessmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "—";
        addInfoCell(table, font, "评测日期", dateStr, COLOR_TEXT_GRAY);

        String userInfo = "—";
        if (user != null) {
            StringBuilder sb = new StringBuilder();
            if (user.getAge() != null) sb.append(user.getAge()).append("岁");
            if (user.getGender() != null) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append("MALE".equals(user.getGender()) ? "男"
                        : "FEMALE".equals(user.getGender()) ? "女" : user.getGender());
            }
            if (sb.length() > 0) userInfo = sb.toString();
        }
        addInfoCell(table, font, "用户信息", userInfo, COLOR_TEXT_GRAY);

        doc.add(table);
    }

    private void addInfoCell(PdfPTable table, BaseFont font, String label, String value, BaseColor valueColor) {
        Font labelF = new Font(font, 10, Font.NORMAL, COLOR_TEXT_GRAY);
        Font valueF = new Font(font, 16, Font.BOLD, valueColor);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setPadding(14);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.5f);

        Paragraph labelPara = new Paragraph(label, labelF);
        labelPara.setSpacingAfter(4);
        cell.addElement(labelPara);
        cell.addElement(new Paragraph(value, valueF));
        table.addCell(cell);
    }

    /**
     * 添加章节标题（使用 PdfPTable 实现左侧色条效果）
     */
    private void addSectionTitle(Document doc, String title, Font headingFont, BaseFont font)
            throws DocumentException {
        doc.add(Chunk.NEWLINE);

        // 用表格实现左侧色条
        PdfPTable titleTable = new PdfPTable(new float[]{0.01f, 0.99f});
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(8);
        titleTable.setSpacingAfter(10);

        PdfPCell barCell = new PdfPCell(new Phrase(""));
        barCell.setBackgroundColor(COLOR_PRIMARY);
        barCell.setBorder(Rectangle.NO_BORDER);
        titleTable.addCell(barCell);

        PdfPCell titleCell = new PdfPCell(new Phrase(" " + title, headingFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingLeft(8);
        titleTable.addCell(titleCell);

        doc.add(titleTable);
    }

    private void addContentBlock(Document doc, String content, Font bodyFont)
            throws DocumentException {
        if (content == null || content.isEmpty()) return;

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new BaseColor(249, 249, 249));
        cell.setPadding(14);
        cell.setBorderWidthLeft(3f);
        cell.setBorderColorLeft(COLOR_PRIMARY);
        cell.setBorderWidthRight(0.5f);
        cell.setBorderWidthTop(0.5f);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColor(COLOR_BORDER);

        Paragraph p = new Paragraph(content, bodyFont);
        p.setLeading(18);
        cell.addElement(p);
        table.addCell(cell);
        doc.add(table);
    }

    private void addRecommendationBlock(Document doc, String recommendation, BaseFont font)
            throws DocumentException {
        Font itemFont = new Font(font, 11, Font.NORMAL, COLOR_TEXT_DARK);
        Font numFont  = new Font(font, 11, Font.BOLD, COLOR_WHITE);

        String[] lines = recommendation.split("\n");
        int index = 1;
        for (String line : lines) {
            String cleaned = line.replaceAll("^\\d+\\.\\s*", "").trim();
            if (cleaned.isEmpty()) continue;

            PdfPTable row = new PdfPTable(new float[]{0.06f, 0.94f});
            row.setWidthPercentage(100);
            row.setSpacingBefore(6);
            row.setSpacingAfter(2);

            PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(index), numFont));
            numCell.setBackgroundColor(COLOR_PRIMARY);
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            numCell.setBorder(Rectangle.NO_BORDER);
            numCell.setPadding(6);
            row.addCell(numCell);

            PdfPCell contentCell = new PdfPCell();
            contentCell.setBackgroundColor(new BaseColor(240, 247, 255));
            contentCell.setBorder(Rectangle.NO_BORDER);
            contentCell.setPaddingLeft(12);
            contentCell.setPaddingTop(8);
            contentCell.setPaddingBottom(8);
            Paragraph p = new Paragraph(cleaned, itemFont);
            p.setLeading(16);
            contentCell.addElement(p);
            row.addCell(contentCell);

            doc.add(row);
            index++;
        }
        doc.add(Chunk.NEWLINE);
    }

    private void addAiAnalysisSection(Document doc, String aiAnalysis, BaseFont font,
                                       Font headingFont, Font bodyFont, Font smallFont)
            throws DocumentException {
        try {
            JsonNode root = objectMapper.readTree(aiAnalysis);

            // 分项评分
            JsonNode scoreBreakdown = root.path("scoreBreakdown");
            if (!scoreBreakdown.isMissingNode() && !scoreBreakdown.isNull()) {
                addSectionTitle(doc, "分项评分详情", headingFont, font);
                PdfPTable scoreTable = new PdfPTable(new float[]{0.35f, 0.65f});
                scoreTable.setWidthPercentage(100);
                scoreTable.setSpacingAfter(16);
                addScoreRow(scoreTable, font, "心血管健康", scoreBreakdown.path("cardiovascular").asDouble(0));
                addScoreRow(scoreTable, font, "代谢健康",   scoreBreakdown.path("metabolic").asDouble(0));
                addScoreRow(scoreTable, font, "生活方式",   scoreBreakdown.path("lifestyle").asDouble(0));
                doc.add(scoreTable);
            }

            // 预测疾病风险
            JsonNode diseases = root.path("predictedDiseases");
            if (diseases.isArray() && diseases.size() > 0) {
                addSectionTitle(doc, "需要关注的健康风险", headingFont, font);
                Font tagFont = new Font(font, 11, Font.NORMAL, new BaseColor(192, 57, 43));
                StringBuilder sb = new StringBuilder();
                for (JsonNode d : diseases) {
                    if (sb.length() > 0) sb.append("   ");
                    sb.append("· ").append(d.asText());
                }
                addContentBlock(doc, sb.toString(), tagFont);
            }

            // 生理年龄
            JsonNode physAge = root.path("physiologicalAge");
            if (!physAge.isMissingNode() && !physAge.isNull()) {
                Font ageFont = new Font(font, 12, Font.BOLD, new BaseColor(230, 126, 34));
                addSectionTitle(doc, "生理年龄评估", headingFont, font);
                addContentBlock(doc,
                    "根据您的健康数据综合分析，您的生理年龄约为 " + physAge.asInt() + " 岁。", ageFont);
            }

            // AI 置信度
            JsonNode confidence = root.path("confidenceScore");
            if (!confidence.isMissingNode() && !confidence.isNull()) {
                Paragraph confPara = new Paragraph("AI 分析置信度：" + confidence.asInt() + "%", smallFont);
                confPara.setAlignment(Element.ALIGN_RIGHT);
                confPara.setSpacingBefore(4);
                doc.add(confPara);
            }
        } catch (Exception e) {
            log.debug("解析 AI 分析详情失败（非致命）: {}", e.getMessage());
        }
    }

    private void addScoreRow(PdfPTable table, BaseFont font, String label, double score) {
        Font labelF = new Font(font, 11, Font.NORMAL, COLOR_TEXT_GRAY);
        Font scoreF = new Font(font, 11, Font.BOLD, COLOR_PRIMARY);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelF));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell progressCell = new PdfPCell();
        progressCell.setBorder(Rectangle.NO_BORDER);
        progressCell.setPaddingTop(8);
        progressCell.setPaddingBottom(8);

        // 确保两列宽度均 > 0，避免 iText 抛出 IllegalArgumentException
        float scoreVal = (float) Math.max(1, Math.min(99, score));
        float emptyVal = 100f - scoreVal;

        PdfPTable progressTable = new PdfPTable(new float[]{scoreVal, emptyVal});
        progressTable.setWidthPercentage(90);

        PdfPCell filled = new PdfPCell(new Phrase(String.format("%.0f", score), scoreF));
        filled.setBackgroundColor(COLOR_PRIMARY);
        filled.setBorder(Rectangle.NO_BORDER);
        filled.setPadding(4);
        filled.setHorizontalAlignment(Element.ALIGN_RIGHT);
        progressTable.addCell(filled);

        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setBackgroundColor(new BaseColor(220, 220, 220));
        empty.setBorder(Rectangle.NO_BORDER);
        progressTable.addCell(empty);

        progressCell.addElement(progressTable);
        table.addCell(progressCell);
    }

    private void addDisclaimer(Document doc, BaseFont font) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        LineSeparator ls = new LineSeparator(0.5f, 100, COLOR_BORDER, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));

        Font disclaimerFont = new Font(font, 9, Font.ITALIC, COLOR_TEXT_GRAY);
        Paragraph disclaimer = new Paragraph(
            "\n免责声明：本报告由 AI 系统自动生成，仅供个人健康参考，不构成医疗诊断建议。" +
            "如有健康问题，请及时咨询专业医疗机构。",
            disclaimerFont
        );
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        disclaimer.setSpacingBefore(8);
        doc.add(disclaimer);
    }

    // ============ 工具方法 ============

    private String getRiskLabel(String riskLevel) {
        if (riskLevel == null) return "未知";
        switch (riskLevel) {
            case "LOW":      return "低风险";
            case "MEDIUM":   return "中风险";
            case "HIGH":     return "高风险";
            case "CRITICAL": return "极高风险";
            default:         return riskLevel;
        }
    }

    private BaseColor getRiskColor(String riskLevel) {
        if (riskLevel == null) return COLOR_TEXT_GRAY;
        switch (riskLevel) {
            case "LOW":      return COLOR_SUCCESS;
            case "MEDIUM":   return COLOR_WARNING;
            case "HIGH":     return COLOR_DANGER;
            case "CRITICAL": return COLOR_CRITICAL;
            default:         return COLOR_TEXT_GRAY;
        }
    }

    // ============ 页眉页脚事件类 ============

    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                BaseFont font;
                try {
                    font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                } catch (Exception e) {
                    font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                }
                PdfContentByte cb = writer.getDirectContent();
                Rectangle pageSize = document.getPageSize();
                cb.setFontAndSize(font, 9);
                cb.setColorFill(new BaseColor(150, 150, 150));
                cb.beginText();
                String footer = "健康评测系统  ·  第 " + writer.getPageNumber() + " 页";
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, footer,
                        pageSize.getWidth() / 2, document.bottomMargin() - 20, 0);
                cb.endText();
            } catch (Exception ignored) {
                // 忽略页脚渲染失败
            }
        }
    }
}

