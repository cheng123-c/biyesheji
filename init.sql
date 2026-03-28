-- 初始化数据库脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS biyesheji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE biyesheji;

-- ===================== 用户管理表 =====================

-- 用户基本信息表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(100) COMMENT '真实姓名',
    age INT COMMENT '年龄',
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '账户状态',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间（软删除）',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    KEY idx_phone (phone),
    KEY idx_email (email),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名',
    description VARCHAR(200) COMMENT '描述',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限代码',
    permission_name VARCHAR(100) COMMENT '权限名',
    resource_type VARCHAR(50) COMMENT '资源类型',
    operation VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(200) COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关系表
CREATE TABLE IF NOT EXISTS t_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    UNIQUE KEY unique_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关系表';

-- ===================== 数据采集表 =====================

-- 问卷定义表
CREATE TABLE IF NOT EXISTS t_questionnaire (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT COMMENT '问卷描述',
    questionnaire_type VARCHAR(50) COMMENT '问卷类型(LIFESTYLE/SYMPTOM/MENTAL/DIET/EXERCISE)',
    questions LONGTEXT COMMENT '题目列表(JSON数组)',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_type (questionnaire_type),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷定义表';

-- 健康数据表
CREATE TABLE IF NOT EXISTS t_health_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型',
    data_value DECIMAL(10, 2) COMMENT '数据值',
    unit VARCHAR(20) COMMENT '单位',
    data_source VARCHAR(50) COMMENT '数据来源',
    device_id VARCHAR(100) COMMENT '设备ID',
    collected_at TIMESTAMP NOT NULL COMMENT '采集时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_data_type (data_type),
    KEY idx_collected_at (collected_at),
    KEY idx_user_type (user_id, data_type, collected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据表';

-- 问卷数据表
CREATE TABLE IF NOT EXISTS t_questionnaire_response (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    response_data JSON COMMENT '问卷回答数据',
    score DECIMAL(5, 2) COMMENT '问卷得分',
    completed_at TIMESTAMP COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷数据表';

-- 医疗记录表
CREATE TABLE IF NOT EXISTS t_medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_type VARCHAR(50) COMMENT '记录类型',
    record_title VARCHAR(200) COMMENT '记录标题',
    record_content LONGTEXT COMMENT '记录内容',
    record_date DATE COMMENT '记录日期',
    hospital VARCHAR(200) COMMENT '医院名称',
    doctor_name VARCHAR(100) COMMENT '医生名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗记录表';

-- ===================== 数据中台表 =====================

-- 数据字典表
CREATE TABLE IF NOT EXISTS t_data_dictionary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    data_type VARCHAR(50) UNIQUE NOT NULL COMMENT '数据类型',
    display_name VARCHAR(100) COMMENT '显示名称',
    unit VARCHAR(20) COMMENT '单位',
    min_value DECIMAL(10, 2) COMMENT '最小值',
    max_value DECIMAL(10, 2) COMMENT '最大值',
    normal_range VARCHAR(100) COMMENT '正常范围',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_data_type (data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 数据异常记录表
CREATE TABLE IF NOT EXISTS t_data_anomaly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    health_data_id BIGINT COMMENT '健康数据ID',
    anomaly_type VARCHAR(50) COMMENT '异常类型',
    anomaly_value DECIMAL(10, 2) COMMENT '异常值',
    expected_range VARCHAR(100) COMMENT '预期范围',
    severity ENUM('LOW', 'MEDIUM', 'HIGH') COMMENT '严重程度',
    handled_at TIMESTAMP COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据异常记录表';

-- ===================== 评估与报告表 =====================

-- AI 评估结果表
CREATE TABLE IF NOT EXISTS t_ai_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    assessment_date DATE NOT NULL COMMENT '评测日期',
    model_version VARCHAR(50) COMMENT '模型版本',
    physiological_age INT COMMENT '生理年龄',
    disease_risk_score DECIMAL(5, 2) COMMENT '疾病风险评分',
    disease_risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') COMMENT '风险等级',
    predicted_diseases JSON COMMENT '预测疾病',
    health_score DECIMAL(5, 2) COMMENT '健康评分',
    score_breakdown JSON COMMENT '评分明细',
    ai_recommendations JSON COMMENT 'AI推荐',
    confidence_score DECIMAL(5, 2) COMMENT '置信度',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI评估结果表';

-- AI 模型版本控制表
CREATE TABLE IF NOT EXISTS t_ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    model_name VARCHAR(100) COMMENT '模型名称',
    model_version VARCHAR(50) UNIQUE NOT NULL COMMENT '模型版本',
    description TEXT COMMENT '描述',
    status ENUM('ACTIVE', 'INACTIVE', 'DEPRECATED') COMMENT '状态',
    accuracy_rate DECIMAL(5, 2) COMMENT '准确率',
    response_time_ms INT COMMENT '响应时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deployed_at TIMESTAMP COMMENT '部署时间',
    retired_at TIMESTAMP COMMENT '退役时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型版本控制表';

-- 评测报告表
CREATE TABLE IF NOT EXISTS t_assessment_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    assessment_date DATE NOT NULL COMMENT '评测日期',
    overall_score DECIMAL(5, 2) COMMENT '总体评分',
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') COMMENT '风险等级',
    summary TEXT COMMENT '报告摘要',
    recommendation TEXT COMMENT '建议',
    ai_analysis TEXT COMMENT 'AI分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评测报告表';

-- ===================== 知识图谱表 =====================

-- 医学知识节点表
CREATE TABLE IF NOT EXISTS t_medical_concept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    concept_name VARCHAR(100) UNIQUE NOT NULL COMMENT '概念名称',
    concept_type ENUM('DISEASE', 'SYMPTOM', 'INDICATOR', 'TREATMENT') COMMENT '概念类型',
    description TEXT COMMENT '描述',
    icd10_code VARCHAR(20) COMMENT 'ICD10编码',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医学知识节点表';

-- 知识图谱关系表
CREATE TABLE IF NOT EXISTS t_knowledge_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    source_concept_id BIGINT NOT NULL COMMENT '源概念ID',
    target_concept_id BIGINT NOT NULL COMMENT '目标概念ID',
    relation_type VARCHAR(50) COMMENT '关系类型',
    confidence_score DECIMAL(5, 2) COMMENT '置信度',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_source (source_concept_id),
    KEY idx_target (target_concept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识图谱关系表';

-- 推理结果表
CREATE TABLE IF NOT EXISTS t_inference_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID（可为空，匿名推理）',
    input_symptoms JSON COMMENT '输入症状列表(JSON数组)',
    inferred_diseases JSON COMMENT '推理得出的疾病(含分数)',
    ai_analysis TEXT COMMENT 'AI分析建议',
    risk_score DECIMAL(5, 2) COMMENT '综合风险评分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识推理结果表';

-- ===================== 干预与服务表 =====================

-- 干预方案表
CREATE TABLE IF NOT EXISTS t_intervention_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plan_type VARCHAR(50) COMMENT '方案类型',
    target_disease VARCHAR(100) COMMENT '目标疾病',
    plan_detail TEXT COMMENT '方案详情',
    duration_days INT COMMENT '持续天数',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status ENUM('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='干预方案表';

-- 健康建议表
CREATE TABLE IF NOT EXISTS t_health_suggestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    suggestion_type VARCHAR(50) COMMENT '建议类型',
    suggestion_content TEXT COMMENT '建议内容',
    priority ENUM('LOW', 'MEDIUM', 'HIGH') COMMENT '优先级',
    evidence_level VARCHAR(50) COMMENT '循证等级',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at TIMESTAMP COMMENT '阅读时间',
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康建议表';

-- 健康内容库表
CREATE TABLE IF NOT EXISTS t_health_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    content_title VARCHAR(200) COMMENT '内容标题',
    content_type VARCHAR(50) COMMENT '内容类型',
    content_body LONGTEXT COMMENT '内容体',
    related_diseases JSON COMMENT '相关疾病',
    content_source VARCHAR(100) COMMENT '内容来源',
    author VARCHAR(100) COMMENT '作者',
    is_published TINYINT(1) DEFAULT 0 COMMENT '是否发布',
    published_at TIMESTAMP COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_published_at (published_at),
    FULLTEXT INDEX ft_content (content_title, content_body)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康内容库表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS t_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notification_type VARCHAR(50) COMMENT '通知类型',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容',
    read_status TINYINT(1) DEFAULT 0 COMMENT '阅读状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at TIMESTAMP COMMENT '阅读时间',
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS t_user_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    feedback_type VARCHAR(50) COMMENT '反馈类型',
    content TEXT COMMENT '反馈内容',
    related_report_id BIGINT COMMENT '相关报告ID',
    rating INT COMMENT '评分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- ===================== 系统管理表 =====================

-- 数据源表
CREATE TABLE IF NOT EXISTS t_data_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    source_name VARCHAR(100) UNIQUE NOT NULL COMMENT '数据源名称',
    source_type VARCHAR(50) COMMENT '数据源类型',
    vendor VARCHAR(100) COMMENT '供应商',
    api_endpoint VARCHAR(500) COMMENT 'API端点',
    api_key VARCHAR(500) COMMENT 'API密钥',
    status ENUM('ACTIVE', 'INACTIVE', 'ERROR') COMMENT '状态',
    last_sync_time TIMESTAMP COMMENT '最后同步时间',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源表';

-- 报告模板表
CREATE TABLE IF NOT EXISTS t_report_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_name VARCHAR(100) UNIQUE NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型',
    content_config JSON COMMENT '内容配置',
    style_config JSON COMMENT '样式配置',
    language VARCHAR(20) DEFAULT 'zh_CN' COMMENT '语言',
    status ENUM('ACTIVE', 'INACTIVE') COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';

-- ===================== 初始数据插入 =====================

-- 插入默认角色
INSERT INTO t_role (role_name, description, status) VALUES
('ADMIN', '管理员', 'ACTIVE'),
('DOCTOR', '医生', 'ACTIVE'),
('USER', '普通用户', 'ACTIVE')
ON DUPLICATE KEY UPDATE status='ACTIVE';

-- 插入默认权限
INSERT INTO t_permission (permission_code, permission_name, resource_type, operation, description) VALUES
('user:view', '查看用户', 'USER', 'view', '查看用户信息'),
('user:create', '创建用户', 'USER', 'create', '创建新用户'),
('user:update', '更新用户', 'USER', 'update', '更新用户信息'),
('user:delete', '删除用户', 'USER', 'delete', '删除用户'),
('health:view', '查看健康数据', 'HEALTH', 'view', '查看健康数据'),
('health:upload', '上传健康数据', 'HEALTH', 'create', '上传健康数据'),
('report:view', '查看报告', 'REPORT', 'view', '查看评测报告'),
('system:manage', '系统管理', 'SYSTEM', 'update', '系统管理权限')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 插入数据字典
INSERT INTO t_data_dictionary (data_type, display_name, unit, min_value, max_value, normal_range, description) VALUES
('heart_rate', '心率', 'bpm', 40, 200, '60-100', '每分钟心跳次数'),
('blood_pressure_systolic', '收缩压', 'mmHg', 60, 200, '<120', '血压收缩压'),
('blood_pressure_diastolic', '舒张压', 'mmHg', 30, 130, '<80', '血压舒张压'),
('blood_glucose', '血糖', 'mmol/L', 1, 30, '3.9-6.1', '空腹血糖'),
('blood_oxygen', '血氧', '%', 80, 100, '95-100', '血液氧饱和度'),
('body_temperature', '体温', '°C', 35, 42, '36.5-37.5', '体表温度'),
('weight', '体重', 'kg', 20, 200, '根据身高计算', '体重'),
('height', '身高', 'cm', 100, 250, '根据性别年龄', '身高')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name);

-- 插入预置问卷数据
INSERT INTO t_questionnaire (title, description, questionnaire_type, questions, is_active) VALUES
('生活习惯调查', '了解您的日常生活习惯，帮助我们为您提供更精准的健康建议', 'LIFESTYLE',
 '[{"id":"q1","question":"您每天的睡眠时长通常是多少小时？","type":"radio","score":20,"options":[{"label":"少于5小时","value":"A","score":0},{"label":"5-6小时","value":"B","score":10},{"label":"7-8小时","value":"C","score":20},{"label":"超过9小时","value":"D","score":15}]},{"id":"q2","question":"您每周进行有氧运动（快走、跑步、游泳等）的频率？","type":"radio","score":20,"options":[{"label":"几乎不运动","value":"A","score":0},{"label":"1-2次","value":"B","score":10},{"label":"3-4次","value":"C","score":20},{"label":"5次及以上","value":"D","score":18}]},{"id":"q3","question":"您每天饮水量大约是多少？","type":"radio","score":15,"options":[{"label":"少于1000ml","value":"A","score":0},{"label":"1000-1500ml","value":"B","score":10},{"label":"1500-2000ml","value":"C","score":15},{"label":"超过2000ml","value":"D","score":15}]},{"id":"q4","question":"您的饮食习惯如何？","type":"radio","score":20,"options":[{"label":"经常吃高盐高油食品","value":"A","score":0},{"label":"偶尔注意饮食","value":"B","score":10},{"label":"基本均衡，少加工食品","value":"C","score":20},{"label":"非常注重健康饮食","value":"D","score":20}]},{"id":"q5","question":"您是否吸烟？","type":"radio","score":15,"options":[{"label":"每天吸烟","value":"A","score":0},{"label":"偶尔吸烟","value":"B","score":5},{"label":"已戒烟","value":"C","score":10},{"label":"从不吸烟","value":"D","score":15}]},{"id":"q6","question":"您的工作/生活压力如何？","type":"radio","score":10,"options":[{"label":"压力非常大，经常焦虑","value":"A","score":0},{"label":"压力较大","value":"B","score":5},{"label":"压力一般","value":"C","score":8},{"label":"压力较小，心态平和","value":"D","score":10}]}]',
 1),
('心理健康自评', '通过简单问题评估您近期的心理健康状况', 'MENTAL',
 '[{"id":"q1","question":"在过去两周里，您感到情绪低落、沮丧或绝望的频率？","type":"radio","score":25,"options":[{"label":"几乎每天","value":"A","score":0},{"label":"超过一半的天数","value":"B","score":8},{"label":"几天","value":"C","score":18},{"label":"完全没有","value":"D","score":25}]},{"id":"q2","question":"在过去两周里，您对做事情缺乏兴趣或乐趣的频率？","type":"radio","score":25,"options":[{"label":"几乎每天","value":"A","score":0},{"label":"超过一半的天数","value":"B","score":8},{"label":"几天","value":"C","score":18},{"label":"完全没有","value":"D","score":25}]},{"id":"q3","question":"在过去两周里，您睡眠质量如何？","type":"radio","score":25,"options":[{"label":"很差，经常失眠","value":"A","score":0},{"label":"较差，有时失眠","value":"B","score":10},{"label":"一般","value":"C","score":18},{"label":"良好","value":"D","score":25}]},{"id":"q4","question":"在过去两周里，您感到精力充沛吗？","type":"radio","score":25,"options":[{"label":"几乎没有精力","value":"A","score":0},{"label":"有时精力不足","value":"B","score":10},{"label":"大多时候精力还好","value":"C","score":20},{"label":"精力充沛","value":"D","score":25}]}]',
 1),
('症状自评问卷', '描述您近期的身体不适症状，帮助AI分析潜在健康风险', 'SYMPTOM',
 '[{"id":"q1","question":"您近期是否有头痛或头晕的症状？","type":"radio","score":0,"options":[{"label":"无","value":"none","score":0},{"label":"偶尔（每月1-2次）","value":"rare","score":1},{"label":"频繁（每周1-2次）","value":"frequent","score":2},{"label":"几乎每天","value":"daily","score":3}]},{"id":"q2","question":"您近期是否有胸闷、心悸的症状？","type":"radio","score":0,"options":[{"label":"无","value":"none","score":0},{"label":"偶尔","value":"rare","score":1},{"label":"频繁","value":"frequent","score":2},{"label":"几乎每天","value":"daily","score":3}]},{"id":"q3","question":"您近期是否有疲劳乏力的感觉？","type":"radio","score":0,"options":[{"label":"无","value":"none","score":0},{"label":"轻度","value":"mild","score":1},{"label":"中度","value":"moderate","score":2},{"label":"重度","value":"severe","score":3}]},{"id":"q4","question":"您近期是否有消化不良、胃痛或腹泻的情况？","type":"radio","score":0,"options":[{"label":"无","value":"none","score":0},{"label":"偶尔","value":"rare","score":1},{"label":"频繁","value":"frequent","score":2},{"label":"几乎每天","value":"daily","score":3}]},{"id":"q5","question":"请描述您的其他主要症状（可选）","type":"text","score":0}]',
 1)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 插入初始健康内容
INSERT INTO t_health_content (content_title, content_type, content_body, content_source, author, is_published, published_at) VALUES
('高血压的预防与管理', 'article',
 '## 什么是高血压\n\n高血压（Hypertension）是指血压持续升高的一种慢性病，收缩压≥140mmHg 和/或舒张压≥90mmHg 即可诊断为高血压。\n\n## 高血压的危害\n\n长期高血压会损伤心脏、肾脏、脑血管和眼底血管，增加心梗、脑卒中等严重并发症的风险。\n\n## 预防高血压的生活方式建议\n\n1. **减少食盐摄入**：每天食盐摄入量控制在5g以内\n2. **保持健康体重**：BMI控制在18.5-24.9之间\n3. **规律有氧运动**：每周至少150分钟中等强度运动\n4. **戒烟限酒**：吸烟和过量饮酒都会升高血压\n5. **减轻精神压力**：长期压力会导致血压升高\n6. **定期监测血压**：特别是有家族史的人群\n\n## 高血压的药物治疗\n\n如果生活方式干预效果不理想，需要在医生指导下进行药物治疗。常用降压药物包括钙通道阻滞剂、ARB类药物等。',
 '中国高血压防治指南', '健康管理团队', 1, NOW()),
('2型糖尿病饮食管理指南', 'guide',
 '## 糖尿病饮食原则\n\n合理的饮食是控制血糖的基础，也是糖尿病治疗的核心环节。\n\n## 控制总热量\n\n根据体重和活动量计算每日所需热量，一般轻体力劳动者每公斤标准体重需要25-30千卡热量。\n\n## 碳水化合物的选择\n\n- 优先选择低升糖指数（GI）食物：燕麦、糙米、豆类\n- 避免高GI食物：白米饭、白面包、含糖饮料\n- 每餐碳水化合物占总热量45-60%\n\n## 蛋白质摄入\n\n- 优质蛋白：鱼、禽、蛋、低脂奶、豆制品\n- 肾功能正常者每日蛋白质1.0-1.5g/kg体重\n\n## 脂肪控制\n\n- 减少饱和脂肪和反式脂肪摄入\n- 增加单不饱和脂肪酸：橄榄油、牛油果\n- 每日脂肪摄入不超过总热量30%\n\n## 膳食纤维的作用\n\n足量膳食纤维（25-35g/天）可以延缓葡萄糖吸收，改善胰岛素敏感性。多吃蔬菜、水果、全谷物。',
 '中国糖尿病学会', '营养科专家', 1, NOW()),
('科学运动：心血管健康的关键', 'article',
 '## 运动与心血管健康\n\n规律运动是预防和改善心血管疾病的最有效方法之一，可以降低血压、改善血脂、控制体重、增强心肺功能。\n\n## 有氧运动推荐\n\n**频率**：每周至少5天\n**强度**：中等强度（运动时能说话但不能唱歌）\n**时间**：每次30-60分钟\n\n推荐运动类型：\n- 快步走（每分钟100-120步）\n- 慢跑\n- 游泳\n- 骑自行车\n- 跳绳\n\n## 力量训练\n\n每周2-3次力量训练，可以增加肌肉量，提高基础代谢率，改善胰岛素敏感性。\n\n## 运动前注意事项\n\n1. 运动前热身5-10分钟\n2. 心脏病患者运动前咨询医生\n3. 避免空腹剧烈运动\n4. 运动后注意补水\n5. 监测运动时心率（最大心率×60-70%为目标心率）',
 '中国心脏康复指南', '心血管专科医生', 1, NOW()),
('睡眠质量改善指南', 'guide',
 '## 为什么睡眠如此重要\n\n成年人每天需要7-9小时的优质睡眠。长期睡眠不足会增加肥胖、糖尿病、心血管疾病和免疫系统疾病的风险。\n\n## 建立良好的睡眠习惯\n\n**规律作息**：每天固定时间入睡和起床，包括周末。\n\n**创造良好睡眠环境**：\n- 保持卧室黑暗、安静、凉爽（16-20°C）\n- 选择舒适的床垫和枕头\n- 减少卧室中的电子设备\n\n**睡前避免**：\n- 睡前3小时内剧烈运动\n- 睡前2小时内使用手机、平板等蓝光设备\n- 下午3点后饮用咖啡因\n- 睡前大量饮酒（虽然酒精助眠，但影响睡眠质量）\n\n**助眠技巧**：\n- 进行深呼吸或冥想放松\n- 温水泡脚促进血液循环\n- 睡前阅读纸质书\n- 记录明天的待办事项，清空焦虑',
 '中国睡眠研究会', '睡眠医学专家', 1, NOW()),
('BMI与体重管理', 'article',
 '## 什么是BMI\n\nBMI（体质指数）= 体重(kg) ÷ 身高(m)²\n\n**BMI分类标准（亚洲成年人）**：\n- BMI < 18.5：体重过低\n- BMI 18.5-22.9：正常\n- BMI 23-24.9：超重\n- BMI ≥ 25：肥胖\n\n## 健康减重的原则\n\n**能量平衡**：每减少1公斤体重，需要消耗约7700千卡热量。通过饮食减少500-750千卡/天，可以每周减重0.5-0.75kg。\n\n**切勿极端节食**：极端节食不仅伤害身体，还会导致反弹效应。\n\n## 科学减重方法\n\n1. **减少加工食品**：少吃零食、快餐、含糖饮料\n2. **增加蔬菜水果**：每餐蔬菜占餐盘的一半\n3. **控制主食分量**：使用小碗盛饭\n4. **细嚼慢咽**：每口咀嚼20次，让饱腹感充分发挥\n5. **规律运动**：结合有氧运动和力量训练\n6. **充足睡眠**：睡眠不足会增加食欲激素分泌',
 '中国居民膳食指南', '营养与健康专家', 1, NOW())
ON DUPLICATE KEY UPDATE content_title=VALUES(content_title);

-- 插入医学知识图谱初始数据
INSERT INTO t_medical_concept (concept_name, concept_type, description, icd10_code) VALUES
-- 疾病
('高血压', 'DISEASE', '血压持续升高的慢性病，收缩压≥140mmHg或舒张压≥90mmHg', 'I10'),
('2型糖尿病', 'DISEASE', '以胰岛素抵抗为主要特征的慢性代谢性疾病', 'E11'),
('高脂血症', 'DISEASE', '血脂异常升高，包括总胆固醇、甘油三酯或低密度脂蛋白升高', 'E78.5'),
('冠心病', 'DISEASE', '冠状动脉粥样硬化导致心肌缺血的疾病', 'I25'),
('脑卒中', 'DISEASE', '脑血管突然破裂或阻塞引起的脑功能障碍', 'I64'),
('肥胖症', 'DISEASE', 'BMI≥28的体重过高状态，伴随代谢异常风险', 'E66'),
('失眠障碍', 'DISEASE', '难以入睡或维持睡眠的睡眠障碍', 'G47.0'),
('抑郁症', 'DISEASE', '以持续情绪低落、兴趣减退为主要特征的精神障碍', 'F32'),
('焦虑障碍', 'DISEASE', '以过度担忧、紧张为主要特征的精神障碍', 'F41'),
('慢性胃炎', 'DISEASE', '胃黏膜慢性炎症，常见症状为上腹不适、消化不良', 'K29.5'),
-- 症状
('头痛', 'SYMPTOM', '头部疼痛，可为钝痛、跳痛、压迫感等', NULL),
('头晕', 'SYMPTOM', '感觉自身或环境旋转的眩晕感或不稳感', NULL),
('胸闷', 'SYMPTOM', '胸部压迫感或憋闷感，活动后加重', NULL),
('心悸', 'SYMPTOM', '自觉心跳加速、不规律或力度增强', NULL),
('疲劳乏力', 'SYMPTOM', '持续感觉疲惫、精力不足，休息后不缓解', NULL),
('失眠', 'SYMPTOM', '入睡困难、易醒或早醒，影响日间功能', NULL),
('多尿', 'SYMPTOM', '排尿次数或尿量明显增多', NULL),
('口渴', 'SYMPTOM', '持续口干渴感，饮水后仍不缓解', NULL),
('体重减轻', 'SYMPTOM', '短期内体重明显下降', NULL),
('视物模糊', 'SYMPTOM', '视力下降或视野模糊', NULL),
('消化不良', 'SYMPTOM', '上腹部不适、饱胀、嗳气等消化道症状', NULL),
('腹痛', 'SYMPTOM', '腹部疼痛，可为阵发性或持续性', NULL),
('恶心呕吐', 'SYMPTOM', '胃内容物反流并经口排出的症状', NULL),
('情绪低落', 'SYMPTOM', '持续感到悲伤、空虚或绝望', NULL),
('焦虑紧张', 'SYMPTOM', '过度担忧、紧张、坐立不安', NULL),
('记忆力下降', 'SYMPTOM', '近期记忆减退、注意力难以集中', NULL),
-- 指标
('高血压指标', 'INDICATOR', '收缩压≥140mmHg或舒张压≥90mmHg', NULL),
('空腹血糖升高', 'INDICATOR', '空腹血糖≥7.0mmol/L', NULL),
('总胆固醇升高', 'INDICATOR', '总胆固醇≥5.17mmol/L（200mg/dL）', NULL),
('BMI偏高', 'INDICATOR', 'BMI≥24（亚洲人超重标准）', NULL),
('低密度脂蛋白升高', 'INDICATOR', 'LDL-C≥3.37mmol/L（130mg/dL）', NULL)
ON DUPLICATE KEY UPDATE concept_name=concept_name;

-- 插入知识图谱关系数据（疾病-症状关联）
INSERT INTO t_knowledge_relation (source_concept_id, target_concept_id, relation_type, confidence_score)
SELECT s.id, t.id, 'HAS_SYMPTOM', conf
FROM (
  SELECT '高血压' AS src, '头痛' AS tgt, 0.75 AS conf UNION ALL
  SELECT '高血压', '头晕', 0.72 UNION ALL
  SELECT '高血压', '心悸', 0.65 UNION ALL
  SELECT '高血压', '视物模糊', 0.60 UNION ALL
  SELECT '2型糖尿病', '多尿', 0.90 UNION ALL
  SELECT '2型糖尿病', '口渴', 0.88 UNION ALL
  SELECT '2型糖尿病', '疲劳乏力', 0.78 UNION ALL
  SELECT '2型糖尿病', '体重减轻', 0.65 UNION ALL
  SELECT '2型糖尿病', '视物模糊', 0.55 UNION ALL
  SELECT '冠心病', '胸闷', 0.88 UNION ALL
  SELECT '冠心病', '心悸', 0.75 UNION ALL
  SELECT '冠心病', '疲劳乏力', 0.70 UNION ALL
  SELECT '失眠障碍', '失眠', 0.95 UNION ALL
  SELECT '失眠障碍', '疲劳乏力', 0.80 UNION ALL
  SELECT '失眠障碍', '焦虑紧张', 0.65 UNION ALL
  SELECT '抑郁症', '情绪低落', 0.92 UNION ALL
  SELECT '抑郁症', '失眠', 0.75 UNION ALL
  SELECT '抑郁症', '疲劳乏力', 0.70 UNION ALL
  SELECT '抑郁症', '记忆力下降', 0.60 UNION ALL
  SELECT '焦虑障碍', '焦虑紧张', 0.92 UNION ALL
  SELECT '焦虑障碍', '心悸', 0.70 UNION ALL
  SELECT '焦虑障碍', '失眠', 0.68 UNION ALL
  SELECT '慢性胃炎', '消化不良', 0.88 UNION ALL
  SELECT '慢性胃炎', '腹痛', 0.75 UNION ALL
  SELECT '慢性胃炎', '恶心呕吐', 0.65 UNION ALL
  SELECT '脑卒中', '头痛', 0.70 UNION ALL
  SELECT '脑卒中', '头晕', 0.72 UNION ALL
  SELECT '脑卒中', '记忆力下降', 0.60 UNION ALL
  SELECT '高脂血症', '疲劳乏力', 0.45 UNION ALL
  SELECT '肥胖症', '疲劳乏力', 0.60
) AS pairs
JOIN t_medical_concept s ON s.concept_name = pairs.src
JOIN t_medical_concept t ON t.concept_name = pairs.tgt
ON DUPLICATE KEY UPDATE confidence_score=VALUES(confidence_score);

-- 创建索引优化查询性能
-- CREATE INDEX IF NOT EXISTS idx_user_id_created_at ON t_health_data(user_id, created_at DESC);
-- CREATE INDEX IF NOT EXISTS idx_user_id_data_type_date ON t_health_data(user_id, data_type, collected_at DESC);
-- CREATE INDEX IF NOT EXISTS idx_assessment_date_risk ON t_ai_assessment(assessment_date, disease_risk_level);
-- CREATE INDEX IF NOT EXISTS idx_notification_user_read ON t_notification(user_id, read_status, created_at DESC);

-- ===================== 数据迁移（已有数据库执行此部分）=====================

-- 如果 t_user 表中尚未有 role 字段，执行以下语句添加（幂等操作）
-- ALTER TABLE t_user ADD COLUMN IF NOT EXISTS role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色' AFTER status;
-- UPDATE t_user SET role = 'USER' WHERE role IS NULL;

-- ===================== 测试数据 ====================
INSERT INTO t_user (username, phone, email, password_hash, real_name, age, gender, bio, status, role) VALUES
('yangkainan', '18234425930', '602533622@qq.com',
 '$2a$10$fRi9PF.xTNEgXNHFTmqLCedZSzOakbTwgJxbb6FtTpNZ/u/v1rtKi',
 'yangkainan', 25, 'UNKNOWN', null, 'ACTIVE', 'USER')

ON DUPLICATE KEY UPDATE real_name=VALUES(real_name);

INSERT INTO t_health_data (user_id, data_type, data_value, unit, data_source, collected_at) VALUES
-- ========== 用户1 身高体重（只记录一次）==========
(1, 'height',  172.00, 'cm',    'manual', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'weight',   78.50, 'kg',    'manual', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'weight',   78.20, 'kg',    'manual', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'weight',   77.80, 'kg',    'manual', DATE_SUB(NOW(), INTERVAL 1  DAY)),

-- ========== 用户1 心率（每天早晨，正常偏快，运动后略高）==========
(1,'heart_rate', 78, 'bpm','device', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'heart_rate', 82, 'bpm','device', DATE_SUB(NOW(), INTERVAL 29 DAY)),
(1,'heart_rate', 75, 'bpm','device', DATE_SUB(NOW(), INTERVAL 28 DAY)),
(1,'heart_rate', 80, 'bpm','device', DATE_SUB(NOW(), INTERVAL 27 DAY)),
(1,'heart_rate', 77, 'bpm','device', DATE_SUB(NOW(), INTERVAL 26 DAY)),
(1,'heart_rate', 83, 'bpm','device', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1,'heart_rate', 79, 'bpm','device', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(1,'heart_rate', 76, 'bpm','device', DATE_SUB(NOW(), INTERVAL 23 DAY)),
(1,'heart_rate', 81, 'bpm','device', DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1,'heart_rate', 74, 'bpm','device', DATE_SUB(NOW(), INTERVAL 21 DAY)),
(1,'heart_rate', 78, 'bpm','device', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'heart_rate', 80, 'bpm','device', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(1,'heart_rate', 75, 'bpm','device', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(1,'heart_rate', 82, 'bpm','device', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(1,'heart_rate', 77, 'bpm','device', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1,'heart_rate', 79, 'bpm','device', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1,'heart_rate', 76, 'bpm','device', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1,'heart_rate', 84, 'bpm','device', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(1,'heart_rate', 78, 'bpm','device', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1,'heart_rate', 73, 'bpm','device', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1,'heart_rate', 80, 'bpm','device', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'heart_rate', 77, 'bpm','device', DATE_SUB(NOW(), INTERVAL 9  DAY)),
(1,'heart_rate', 75, 'bpm','device', DATE_SUB(NOW(), INTERVAL 8  DAY)),
(1,'heart_rate', 82, 'bpm','device', DATE_SUB(NOW(), INTERVAL 7  DAY)),
(1,'heart_rate', 79, 'bpm','device', DATE_SUB(NOW(), INTERVAL 6  DAY)),
(1,'heart_rate', 76, 'bpm','device', DATE_SUB(NOW(), INTERVAL 5  DAY)),
(1,'heart_rate', 81, 'bpm','device', DATE_SUB(NOW(), INTERVAL 4  DAY)),
(1,'heart_rate', 78, 'bpm','device', DATE_SUB(NOW(), INTERVAL 3  DAY)),
(1,'heart_rate', 74, 'bpm','device', DATE_SUB(NOW(), INTERVAL 2  DAY)),
(1,'heart_rate', 80, 'bpm','device', DATE_SUB(NOW(), INTERVAL 1  DAY)),

-- ========== 用户1 收缩压（偏高，呈轻度上升趋势）==========
(1,'blood_pressure_systolic', 122,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'blood_pressure_systolic', 125,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 28 DAY)),
(1,'blood_pressure_systolic', 121,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 26 DAY)),
(1,'blood_pressure_systolic', 127,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(1,'blood_pressure_systolic', 124,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1,'blood_pressure_systolic', 129,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'blood_pressure_systolic', 126,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(1,'blood_pressure_systolic', 131,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1,'blood_pressure_systolic', 128,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1,'blood_pressure_systolic', 133,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1,'blood_pressure_systolic', 130,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'blood_pressure_systolic', 135,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 8  DAY)),
(1,'blood_pressure_systolic', 132,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 6  DAY)),
(1,'blood_pressure_systolic', 136,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 4  DAY)),
(1,'blood_pressure_systolic', 134,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 2  DAY)),

-- ========== 用户1 舒张压 ==========
(1,'blood_pressure_diastolic', 78,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'blood_pressure_diastolic', 80,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 28 DAY)),
(1,'blood_pressure_diastolic', 77,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 26 DAY)),
(1,'blood_pressure_diastolic', 82,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(1,'blood_pressure_diastolic', 79,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1,'blood_pressure_diastolic', 83,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'blood_pressure_diastolic', 81,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(1,'blood_pressure_diastolic', 84,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1,'blood_pressure_diastolic', 82,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1,'blood_pressure_diastolic', 85,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1,'blood_pressure_diastolic', 83,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'blood_pressure_diastolic', 86,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 8  DAY)),
(1,'blood_pressure_diastolic', 84,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 6  DAY)),
(1,'blood_pressure_diastolic', 87,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 4  DAY)),
(1,'blood_pressure_diastolic', 85,'mmHg','manual', DATE_SUB(NOW(), INTERVAL 2  DAY)),

-- ========== 用户1 血糖（正常范围内，空腹）==========
(1,'blood_glucose', 5.20,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'blood_glucose', 5.40,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1,'blood_glucose', 5.10,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'blood_glucose', 5.30,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1,'blood_glucose', 5.50,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'blood_glucose', 5.20,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 5  DAY)),
(1,'blood_glucose', 5.40,'mmol/L','manual', DATE_SUB(NOW(), INTERVAL 1  DAY)),

-- ========== 用户1 血氧（正常）==========
(1,'blood_oxygen', 98,'%','device', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'blood_oxygen', 97,'%','device', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1,'blood_oxygen', 98,'%','device', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'blood_oxygen', 99,'%','device', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1,'blood_oxygen', 97,'%','device', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'blood_oxygen', 98,'%','device', DATE_SUB(NOW(), INTERVAL 5  DAY)),
(1,'blood_oxygen', 98,'%','device', DATE_SUB(NOW(), INTERVAL 1  DAY)),

-- ========== 用户1 体温（正常）==========
(1,'body_temperature', 36.6,'°C','device', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1,'body_temperature', 36.7,'°C','device', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1,'body_temperature', 36.5,'°C','device', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1,'body_temperature', 36.8,'°C','device', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1,'body_temperature', 36.6,'°C','device', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1,'body_temperature', 37.0,'°C','device', DATE_SUB(NOW(), INTERVAL 5  DAY)),
(1,'body_temperature', 36.7,'°C','device', DATE_SUB(NOW(), INTERVAL 1  DAY));

-- 数据库初始化完成
SELECT '数据库初始化完成！' AS status;

