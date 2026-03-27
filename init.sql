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

-- 用户设备绑定表
CREATE TABLE IF NOT EXISTS t_user_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id VARCHAR(100) UNIQUE NOT NULL COMMENT '设备ID',
    device_type VARCHAR(50) COMMENT '设备类型',
    device_name VARCHAR(100) COMMENT '设备名称',
    bind_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    last_sync_time TIMESTAMP COMMENT '最后同步时间',
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备绑定表';

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
    KEY idx_assessment_date (assessment_date),
    UNIQUE KEY unique_user_date (user_id, assessment_date)
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
    KEY idx_assessment_date (assessment_date),
    UNIQUE KEY unique_user_date (user_id, assessment_date)
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

-- 推理结果缓存表
CREATE TABLE IF NOT EXISTS t_inference_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    input_symptom VARCHAR(200) COMMENT '输入症状',
    inferred_conditions JSON COMMENT '推理得出的疾病',
    confidence DECIMAL(5, 2) COMMENT '置信度',
    explanation TEXT COMMENT '解释',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expired_at TIMESTAMP COMMENT '过期时间',
    KEY idx_user_id (user_id),
    KEY idx_expired_at (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推理结果缓存表';

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
('blood_glucose', '血糖', 'mg/dL', 50, 400, '70-100', '空腹血糖'),
('blood_oxygen', '血氧', '%', 80, 100, '95-100', '血液氧饱和度'),
('body_temperature', '体温', '°C', 35, 42, '36.5-37.5', '体表温度'),
('weight', '体重', 'kg', 20, 200, '根据身高计算', '体重'),
('height', '身高', 'cm', 100, 250, '根据性别年龄', '身高')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name);

-- 创建索引优化查询性能
-- CREATE INDEX IF NOT EXISTS idx_user_id_created_at ON t_health_data(user_id, created_at DESC);
-- CREATE INDEX IF NOT EXISTS idx_user_id_data_type_date ON t_health_data(user_id, data_type, collected_at DESC);
-- CREATE INDEX IF NOT EXISTS idx_assessment_date_risk ON t_ai_assessment(assessment_date, disease_risk_level);
-- CREATE INDEX IF NOT EXISTS idx_notification_user_read ON t_notification(user_id, read_status, created_at DESC);

-- ===================== 数据迁移（已有数据库执行此部分）=====================

-- 如果 t_user 表中尚未有 role 字段，执行以下语句添加（幂等操作）
-- ALTER TABLE t_user ADD COLUMN IF NOT EXISTS role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色' AFTER status;
-- UPDATE t_user SET role = 'USER' WHERE role IS NULL;

-- 数据库初始化完成
SELECT '数据库初始化完成！' AS status;

