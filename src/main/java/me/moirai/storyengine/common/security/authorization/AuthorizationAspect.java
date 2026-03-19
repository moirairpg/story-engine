package me.moirai.storyengine.common.security.authorization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

@Aspect
@Component
public class AuthorizationAspect {

    private final AuthorizationService authorizationService;
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuthorizationAspect(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Around("@annotation(authorize)")
    public Object intercept(ProceedingJoinPoint joinPoint, Authorize authorize) throws Throwable {
        var fields = resolveFields(authorize.fields(), joinPoint);
        var principal = (MoiraiPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authorizationService.authorize(authorize.operation(), fields, principal);
        return joinPoint.proceed();
    }

    private Map<String, Object> resolveFields(String[] expressions, ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        Map<String, Object> argsByName = new HashMap<>();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                argsByName.put(paramNames[i], args[i]);
            }
        }

        Map<String, Integer> leafCounts = new HashMap<>();
        for (String expression : expressions) {
            String path = expression.startsWith("#") ? expression.substring(1) : expression;
            leafCounts.merge(leafKey(path), 1, Integer::sum);
        }

        Map<String, Object> resolved = new HashMap<>();
        for (String expression : expressions) {
            String path = expression.startsWith("#") ? expression.substring(1) : expression;
            String[] parts = path.split("\\.", 2);
            Object paramValue = argsByName.get(parts[0]);

            if (parts.length == 1) {
                resolved.put(path, paramValue);
            } else {
                Object value = navigate(paramValue, parts[1]);
                String leaf = leafKey(parts[1]);
                resolved.put(leafCounts.get(leaf) > 1 ? path : leaf, value);
            }
        }

        return resolved;
    }

    private Object navigate(Object root, String dotPath) {
        if (root == null) return null;
        String[] parts = dotPath.split("\\.", 2);
        Object value = extractValue(root, parts[0]);
        return parts.length == 1 ? value : navigate(value, parts[1]);
    }

    private Object extractValue(Object obj, String name) {
        if (obj == null) return null;
        try {
            return obj.getClass().getMethod(name).invoke(obj);
        } catch (NoSuchMethodException e) {
            try {
                Field field = obj.getClass().getDeclaredField(name);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String leafKey(String dotPath) {
        int lastDot = dotPath.lastIndexOf('.');
        return lastDot >= 0 ? dotPath.substring(lastDot + 1) : dotPath;
    }
}
