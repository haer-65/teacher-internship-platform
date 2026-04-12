-- =============================================
-- 师范生教育实习全过程管理平台
-- 扩展基础数据（院系/专业/年级/基地）
-- 说明：
-- 1. 采用幂等写法，避免重复插入
-- 2. 兼容当前运行库的补录，也兼容新库初始化
-- =============================================

USE `teacher_internship_platform`;

-- ------------------------------
-- 院系扩展
-- ------------------------------
INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3003, 'FLC', '外国语学院', 0, '周老师', '0773-5551001', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'FLC' OR `dept_name` = '外国语学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3004, 'MTH', '数学与统计学院', 0, '黄老师', '0773-5551002', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'MTH' OR `dept_name` = '数学与统计学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3005, 'PHY', '物理与电子工程学院', 0, '蒋老师', '0773-5551003', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'PHY' OR `dept_name` = '物理与电子工程学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3006, 'CHE', '化学与材料学院', 0, '彭老师', '0773-5551004', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'CHE' OR `dept_name` = '化学与材料学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3007, 'BIO', '生物与食品工程学院', 0, '何老师', '0773-5551005', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'BIO' OR `dept_name` = '生物与食品工程学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3008, 'HIS', '历史文化与旅游学院', 0, '赵老师', '0773-5551006', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'HIS' OR `dept_name` = '历史文化与旅游学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3009, 'GEO', '地理与环境科学学院', 0, '罗老师', '0773-5551007', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'GEO' OR `dept_name` = '地理与环境科学学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3010, 'MUS', '音乐学院', 0, '唐老师', '0773-5551008', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'MUS' OR `dept_name` = '音乐学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3011, 'ART', '美术学院', 0, '谢老师', '0773-5551009', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'ART' OR `dept_name` = '美术学院')
);

INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3012, 'PE', '体育与健康学院', 0, '梁老师', '0773-5551010', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_department`
    WHERE `deleted` = 0
      AND (`dept_code` = 'PE' OR `dept_name` = '体育与健康学院')
);

-- ------------------------------
-- 专业扩展
-- ------------------------------
INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3103,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'EDU' OR `dept_name` = '教育学院') ORDER BY `id` LIMIT 1),
       'XQ001', '学前教育', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'EDU' OR `dept_name` = '教育学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'XQ001' OR `major_name` = '学前教育')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3104,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'EDU' OR `dept_name` = '教育学院') ORDER BY `id` LIMIT 1),
       'XX001', '小学教育', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'EDU' OR `dept_name` = '教育学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'XX001' OR `major_name` = '小学教育')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3105,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'FLC' OR `dept_name` = '外国语学院') ORDER BY `id` LIMIT 1),
       'YY001', '英语', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'FLC' OR `dept_name` = '外国语学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'YY001' OR `major_name` = '英语')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3106,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'MTH' OR `dept_name` = '数学与统计学院') ORDER BY `id` LIMIT 1),
       'SX001', '数学与应用数学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'MTH' OR `dept_name` = '数学与统计学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'SX001' OR `major_name` = '数学与应用数学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3107,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'PHY' OR `dept_name` = '物理与电子工程学院') ORDER BY `id` LIMIT 1),
       'WL001', '物理学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'PHY' OR `dept_name` = '物理与电子工程学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'WL001' OR `major_name` = '物理学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3108,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'CHE' OR `dept_name` = '化学与材料学院') ORDER BY `id` LIMIT 1),
       'HX001', '化学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'CHE' OR `dept_name` = '化学与材料学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'HX001' OR `major_name` = '化学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3109,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'BIO' OR `dept_name` = '生物与食品工程学院') ORDER BY `id` LIMIT 1),
       'SW001', '生物科学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'BIO' OR `dept_name` = '生物与食品工程学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'SW001' OR `major_name` = '生物科学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3110,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'HIS' OR `dept_name` = '历史文化与旅游学院') ORDER BY `id` LIMIT 1),
       'LS001', '历史学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'HIS' OR `dept_name` = '历史文化与旅游学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'LS001' OR `major_name` = '历史学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3111,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'GEO' OR `dept_name` = '地理与环境科学学院') ORDER BY `id` LIMIT 1),
       'DL001', '地理科学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'GEO' OR `dept_name` = '地理与环境科学学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'DL001' OR `major_name` = '地理科学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3112,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'MUS' OR `dept_name` = '音乐学院') ORDER BY `id` LIMIT 1),
       'YYX001', '音乐学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'MUS' OR `dept_name` = '音乐学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'YYX001' OR `major_name` = '音乐学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3113,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'ART' OR `dept_name` = '美术学院') ORDER BY `id` LIMIT 1),
       'MS001', '美术学', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'ART' OR `dept_name` = '美术学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'MS001' OR `major_name` = '美术学')
);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
SELECT 3114,
       (SELECT `id` FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'PE' OR `dept_name` = '体育与健康学院') ORDER BY `id` LIMIT 1),
       'TY001', '体育教育', 'ENABLED', 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM `base_department` WHERE `deleted` = 0 AND (`dept_code` = 'PE' OR `dept_name` = '体育与健康学院')
)
  AND NOT EXISTS (
    SELECT 1 FROM `base_major` WHERE `deleted` = 0 AND (`major_code` = 'TY001' OR `major_name` = '体育教育')
);

-- ------------------------------
-- 年级扩展
-- ------------------------------
INSERT INTO `base_grade` (`id`, `grade_code`, `grade_name`, `status`, `created_by`, `updated_by`)
SELECT 3203, 'G2022', '2022级', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_grade`
    WHERE `deleted` = 0
      AND (`grade_code` = 'G2022' OR `grade_name` = '2022级')
);

INSERT INTO `base_grade` (`id`, `grade_code`, `grade_name`, `status`, `created_by`, `updated_by`)
SELECT 3204, 'G2025', '2025级', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_grade`
    WHERE `deleted` = 0
      AND (`grade_code` = 'G2025' OR `grade_name` = '2025级')
);

INSERT INTO `base_grade` (`id`, `grade_code`, `grade_name`, `status`, `created_by`, `updated_by`)
SELECT 3205, 'G2026', '2026级', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_grade`
    WHERE `deleted` = 0
      AND (`grade_code` = 'G2026' OR `grade_name` = '2026级')
);

-- ------------------------------
-- 实习基地扩展
-- ------------------------------
INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3303, 'BASE003', '桂林市第三中学', '广西壮族自治区', '桂林市', '象山区', '环城西一路18号', '陈主任', '0773-5552001', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE003' OR `base_name` = '桂林市第三中学')
);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3304, 'BASE004', '桂林市实验中学', '广西壮族自治区', '桂林市', '七星区', '建干路12号', '莫主任', '0773-5552002', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE004' OR `base_name` = '桂林市实验中学')
);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3305, 'BASE005', '桂林师范附属小学', '广西壮族自治区', '桂林市', '秀峰区', '榕湖北路6号', '苏校长', '0773-5552003', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE005' OR `base_name` = '桂林师范附属小学')
);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3306, 'BASE006', '桂林市田家炳中学', '广西壮族自治区', '桂林市', '叠彩区', '中山北路86号', '邓主任', '0773-5552004', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE006' OR `base_name` = '桂林市田家炳中学')
);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3307, 'BASE007', '临桂区第一中学', '广西壮族自治区', '桂林市', '临桂区', '临政路28号', '冯主任', '0773-5552005', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE007' OR `base_name` = '临桂区第一中学')
);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
SELECT 3308, 'BASE008', '雁山区实验学校', '广西壮族自治区', '桂林市', '雁山区', '雁山街12号', '潘主任', '0773-5552006', 'ENABLED', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `base_internship_base`
    WHERE `deleted` = 0
      AND (`base_code` = 'BASE008' OR `base_name` = '雁山区实验学校')
);
