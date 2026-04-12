$path = 'D:\teacher-internship-platform\backend\src\main\java\com\teacher\internship\modules\application\service\ApplicationService.java'
$text = [System.IO.File]::ReadAllText($path)

$pattern = '(?s)    private void ensurePlanAssignable\(BizInternshipPlan plan\) \{\r?\n.*?\r?\n    \}\r?\n(?=\s*private void ensureStudentInPlanDept)'
$replacement = @'
    private void ensurePlanAssignable(BizInternshipPlan plan) {
        if (!STATUS_PUBLISHED.equals(normalizeCode(plan.getPlanStatus()))) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "\u8BA1\u5212\u5DF2\u7ED3\u675F\uFF0C\u4E0D\u80FD\u518D\u5206\u914D\u6216\u8C03\u6574");
        }
    }
'@

if ($text -notmatch $pattern) {
    throw 'ensurePlanAssignable block not found'
}

$text = [regex]::Replace($text, $pattern, $replacement, 1)
[System.IO.File]::WriteAllText($path, $text)
Write-Host 'updated'
