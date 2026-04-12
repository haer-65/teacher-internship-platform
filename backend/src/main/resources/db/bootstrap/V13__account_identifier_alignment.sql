UPDATE `sys_user`
SET `login_name` = CASE
    WHEN `student_no` IS NOT NULL AND TRIM(`student_no`) <> '' THEN TRIM(`student_no`)
    WHEN `teacher_no` IS NOT NULL AND TRIM(`teacher_no`) <> '' THEN TRIM(`teacher_no`)
    ELSE `login_name`
END
WHERE `deleted` = 0;
