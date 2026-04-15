package com.teacher.internship.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.auth.dto.LoginRequest;
import com.teacher.internship.modules.auth.vo.AuthContextVO;
import com.teacher.internship.modules.auth.vo.LoginUserInfo;
import com.teacher.internship.modules.auth.vo.RoleOptionVO;
import com.teacher.internship.modules.base.entity.BaseDepartment;
import com.teacher.internship.modules.base.entity.BaseGrade;
import com.teacher.internship.modules.base.entity.BaseMajor;
import com.teacher.internship.modules.base.mapper.BaseDepartmentMapper;
import com.teacher.internship.modules.base.mapper.BaseGradeMapper;
import com.teacher.internship.modules.base.mapper.BaseMajorMapper;
import com.teacher.internship.modules.system.entity.SysLoginLog;
import com.teacher.internship.modules.system.entity.SysRole;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysLoginLogMapper;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import com.teacher.internship.modules.system.service.RbacService;
import com.teacher.internship.modules.system.service.SystemParamService;
import com.teacher.internship.security.jwt.JwtProperties;
import com.teacher.internship.security.jwt.JwtTokenProvider;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String PARAM_LOGIN_REMEMBER_DAYS = "SYSTEM_LOGIN_REMEMBER_DAYS";
    private static final long DEFAULT_REMEMBER_ME_DAYS = 7L;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final BaseDepartmentMapper baseDepartmentMapper;
    private final BaseMajorMapper baseMajorMapper;
    private final BaseGradeMapper baseGradeMapper;
    private final RbacService rbacService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SystemParamService paramService;

    public AuthService(SysUserMapper sysUserMapper,
                       SysLoginLogMapper sysLoginLogMapper,
                       BaseDepartmentMapper baseDepartmentMapper,
                       BaseMajorMapper baseMajorMapper,
                       BaseGradeMapper baseGradeMapper,
                       RbacService rbacService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       SystemParamService paramService) {
        this.sysUserMapper = sysUserMapper;
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.baseDepartmentMapper = baseDepartmentMapper;
        this.baseMajorMapper = baseMajorMapper;
        this.baseGradeMapper = baseGradeMapper;
        this.rbacService = rbacService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.paramService = paramService;
    }

    public AuthContextVO login(LoginRequest request, HttpServletRequest httpRequest) {
        String accountNo = request.getAccount() == null ? null : request.getAccount().trim();
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .and(wrapper -> wrapper.eq(SysUser::getStudentNo, accountNo)
                        .or().eq(SysUser::getTeacherNo, accountNo)
                        .or().eq(SysUser::getLoginName, accountNo))
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (user == null) {
            saveLoginLog(accountNo, null, "FAIL", "账号不存在", httpRequest);
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学号/工号或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveLoginLog(accountNo, user.getId(), "FAIL", "密码不匹配", httpRequest);
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "学号/工号或密码错误");
        }

        validateLoginStatus(user);

        String currentRoleCode = chooseCurrentRoleCode(user.getId(), request.getRoleCode());
        long expiresIn = Boolean.TRUE.equals(request.getRememberMe())
                ? resolveRememberMeSeconds()
                : jwtProperties.getExpirationSeconds();
        String token = jwtTokenProvider.createToken(user.getId(), resolveAccountNo(user), currentRoleCode, expiresIn);

        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdatedBy(user.getId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        saveLoginLog(accountNo, user.getId(), "SUCCESS", null, httpRequest);
        return buildAuthContext(user, currentRoleCode, token, expiresIn);
    }

    public AuthContextVO me(JwtUserPrincipal principal) {
        SysUser user = getCurrentUser(principal);
        String currentRoleCode = chooseCurrentRoleCode(user.getId(), principal.getRoleCode());
        return buildAuthContext(user, currentRoleCode, null, null);
    }

    public AuthContextVO switchRole(JwtUserPrincipal principal, String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色编码不能为空");
        }
        SysUser user = getCurrentUser(principal);
        String currentRoleCode = chooseCurrentRoleCode(user.getId(), roleCode);
        String token = jwtTokenProvider.createToken(
                user.getId(),
                resolveAccountNo(user),
                currentRoleCode,
                jwtProperties.getExpirationSeconds()
        );
        return buildAuthContext(user, currentRoleCode, token, jwtProperties.getExpirationSeconds());
    }

    private SysUser getCurrentUser(JwtUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, principal.getUserId())
                .eq(SysUser::getDeleted, 0L)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), "当前用户不存在");
        }
        validateLoginStatus(user);
        return user;
    }

    private void validateLoginStatus(SysUser user) {
        if ("PENDING".equals(user.getStatus())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前账号待审核，暂不可登录");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前账号已被禁用");
        }
        if ("LOCKED".equals(user.getStatus())) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前账号已被锁定");
        }
    }

    private String chooseCurrentRoleCode(Long userId, String expectedRoleCode) {
        List<SysRole> roles = rbacService.getEnabledRolesByUserId(userId);
        if (CollectionUtils.isEmpty(roles)) {
            throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前账号未分配可用角色");
        }

        if (StringUtils.hasText(expectedRoleCode)) {
            boolean matched = roles.stream().anyMatch(role -> expectedRoleCode.equals(role.getRoleCode()));
            if (!matched) {
                throw new BusinessException(ApiCode.FORBIDDEN.getCode(), "当前角色不属于该账号");
            }
            return expectedRoleCode;
        }

        return roles.get(0).getRoleCode();
    }

    private AuthContextVO buildAuthContext(SysUser user, String currentRoleCode, String token, Long expiresIn) {
        AuthContextVO context = new AuthContextVO();
        context.setToken(token);
        context.setTokenType(token == null ? null : "Bearer");
        context.setExpiresIn(expiresIn);
        context.setCurrentRoleCode(currentRoleCode);

        LoginUserInfo userInfo = new LoginUserInfo(
                user.getId(),
                resolveAccountNo(user),
                user.getRealName(),
                user.getIdentityType(),
                user.getStudentNo(),
                user.getTeacherNo(),
                user.getPhone(),
                user.getEmail(),
                user.getDeptId(),
                resolveDepartmentName(user.getDeptId()),
                resolveMajorName(user.getMajorId()),
                resolveGradeName(user.getGradeId()),
                currentRoleCode,
                user.getMustChangePassword()
        );
        context.setUser(userInfo);

        List<RoleOptionVO> roleOptions = rbacService.getRoleOptionsByUserId(user.getId());
        context.setRoles(roleOptions);
        context.setMenuTree(rbacService.getMenuTreeByRoleCode(currentRoleCode));

        Set<String> permissionCodeSet = rbacService.getPermissionCodesByRoleCode(currentRoleCode);
        context.setPermissionCodes(permissionCodeSet.stream().sorted().collect(Collectors.toList()));
        return context;
    }

    private String resolveAccountNo(SysUser user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getStudentNo())) {
            return user.getStudentNo().trim();
        }
        if (StringUtils.hasText(user.getTeacherNo())) {
            return user.getTeacherNo().trim();
        }
        return StringUtils.hasText(user.getLoginName()) ? user.getLoginName().trim() : null;
    }

    private String resolveDepartmentName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        BaseDepartment department = baseDepartmentMapper.selectOne(new LambdaQueryWrapper<BaseDepartment>()
                .eq(BaseDepartment::getId, deptId)
                .eq(BaseDepartment::getDeleted, 0L)
                .last("LIMIT 1"));
        return department == null ? null : department.getDeptName();
    }

    private String resolveMajorName(Long majorId) {
        if (majorId == null) {
            return null;
        }
        BaseMajor major = baseMajorMapper.selectOne(new LambdaQueryWrapper<BaseMajor>()
                .eq(BaseMajor::getId, majorId)
                .eq(BaseMajor::getDeleted, 0L)
                .last("LIMIT 1"));
        return major == null ? null : major.getMajorName();
    }

    private String resolveGradeName(Long gradeId) {
        if (gradeId == null) {
            return null;
        }
        BaseGrade grade = baseGradeMapper.selectOne(new LambdaQueryWrapper<BaseGrade>()
                .eq(BaseGrade::getId, gradeId)
                .eq(BaseGrade::getDeleted, 0L)
                .last("LIMIT 1"));
        return grade == null ? null : grade.getGradeName();
    }

    private void saveLoginLog(String loginName,
                              Long userId,
                              String result,
                              String reason,
                              HttpServletRequest request) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setId(null);
            loginLog.setLoginName(loginName);
            loginLog.setUserId(userId);
            loginLog.setLoginIp(extractIp(request));
            loginLog.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
            loginLog.setLoginResult(result);
            loginLog.setFailReason(reason);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setCreatedBy(0L);
            loginLog.setUpdatedBy(0L);
            loginLog.setDeleted(0L);
            sysLoginLogMapper.insert(loginLog);
        } catch (Exception ex) {
            log.warn("Save login log failed, accountNo={}, userId={}, result={}, reason={}",
                    loginName, userId, result, reason, ex);
        }
    }

    private long resolveRememberMeSeconds() {
        int configuredDays = paramService.getIntValue(PARAM_LOGIN_REMEMBER_DAYS, (int) DEFAULT_REMEMBER_ME_DAYS);
        long safeDays = Math.max(configuredDays, 1);
        return safeDays * 24L * 60L * 60L;
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
