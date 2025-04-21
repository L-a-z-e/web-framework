package com.laze.backend.common.aop;

import com.laze.backend.common.entity.Auditable;
import com.laze.backend.security.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.time.LocalDateTime;
import java.util.Collection;

@Aspect
@Component
@Slf4j
public class AuditingAspect {

    // --- Pointcut 정의 ---
    // 등록 관련 메서드 이름 패턴 (save*, create*, insert*, add* 등 포함 고려)
    @Pointcut("execution(* com.laze.backend..service.*Service.save*(..)) || " +
        "execution(* com.laze.backend..service.*Service.create*(..)) || " +
        "execution(* com.laze.backend..service.*Service.insert*(..)) || " +
        "execution(* com.laze.backend..service.*Service.add*(..))")
    public void registrationMethods() {}

    // 수정 관련 메서드 이름 패턴 (update*, modify*, merge*, set* 등 포함 고려)
    @Pointcut("execution(* com.laze.backend..service.*Service.update*(..)) || " +
        "execution(* com.laze.backend..service.*Service.modify*(..)) || " +
        "execution(* com.laze.backend..service.*Service.merge*(..)) || " +
        "execution(* com.laze.backend..service.*Service.set*(..))")
    public void modificationMethods() {}

    // --- Advice 구현 ---

    // 등록 메서드용 Advice (@Before 사용)
    @Before("registrationMethods()")
    public void setRegistrationAuditFields(JoinPoint joinPoint) {
        log.debug("Applying registration audit for method: {}", joinPoint.getSignature().toShortString());
        setAuditFields(joinPoint, true); // isNew = true 전달
    }

    // 수정 메서드용 Advice (@Before 사용)
    @Before("modificationMethods()")
    public void setModificationAuditFields(JoinPoint joinPoint) {
        log.debug("Applying modification audit for method: {}", joinPoint.getSignature().toShortString());
        setAuditFields(joinPoint, false); // isNew = false 전달
    }

    // 공통 필드 설정 로직
    private void setAuditFields(JoinPoint joinPoint, boolean isNew) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String empIdFromAuth = "SYSTEM";
        String empNmFromAuth = "SYSTEM";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                empIdFromAuth = userDetails.getUsername(); // empId
                empNmFromAuth = userDetails.getEmpNm();
            } else {
                empIdFromAuth = authentication.getName(); // ☑️ 해당하지 않는 케이스에 empId가 존재할지 알 수 없음
                empNmFromAuth = authentication.getName();
            }
        }

        log.debug("Current User for Auditing - ID: {}, Name: {}", empIdFromAuth, empNmFromAuth);

        final String currentEmpId = empIdFromAuth;
        final String currentEmpNm = empNmFromAuth;

        for (Object arg : args) {
            if (arg instanceof Auditable) { // 단일 객체 처리
                processAuditable((Auditable) arg, now, currentEmpId, currentEmpNm, isNew);
            } else if (arg instanceof Collection) { // 컬렉션(List 등) 처리
                ((Collection<?>) arg).stream()
                    .filter(item -> item instanceof Auditable)
                    .forEach(item -> processAuditable((Auditable) item, now, currentEmpId, currentEmpNm, isNew));
            }
            // 필요시 Map 등 다른 타입 처리 추가
        }
    }

    // Auditable 객체 필드 설정 로직 분리
    private void processAuditable(Auditable auditableEntity, LocalDateTime now, String userId, String userName, boolean isNew) {
        if (isNew) {
            log.debug("Setting registration audit fields for: {}", auditableEntity.getClass().getSimpleName());
            auditableEntity.setRegDtm(now);
            auditableEntity.setRegEmpId(userId);
            auditableEntity.setRegEmpNm(userName);
            auditableEntity.setChgDtm(now);
            auditableEntity.setChgEmpId(userId);
            auditableEntity.setChgEmpNm(userName);
        } else {
            log.debug("Setting modification audit fields for: {}", auditableEntity.getClass().getSimpleName());
            auditableEntity.setChgDtm(now);
            auditableEntity.setChgEmpId(userId);
            auditableEntity.setChgEmpNm(userName);
        }
    }
}
