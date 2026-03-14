package me.moirai.storyengine.infrastructure.security.authorization;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class MoiraiMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final Supplier<MoiraiSecurityExpressions> expressionRootSupplier;

    public MoiraiMethodSecurityExpressionHandler(Supplier<MoiraiSecurityExpressions> expressionRootSupplier) {
        this.expressionRootSupplier = expressionRootSupplier;
    }

    @Override
    protected MoiraiSecurityExpressions createSecurityExpressionRoot(
            Authentication authentication, MethodInvocation invocation) {

        return createSecurityExpressionRoot(() -> authentication, invocation);
    }

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {

        MoiraiSecurityExpressions root = createSecurityExpressionRoot(authentication, mi);
        SpringAddonsMethodSecurityEvaluationContext ctx = new SpringAddonsMethodSecurityEvaluationContext(root, mi,
                getParameterNameDiscoverer());

        ctx.setBeanResolver(getBeanResolver());
        return ctx;
    }

    private MoiraiSecurityExpressions createSecurityExpressionRoot(
            Supplier<Authentication> authentication, MethodInvocation invocation) {

        MoiraiSecurityExpressions root = expressionRootSupplier.get();
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(getTrustResolver());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());

        return root;
    }

    static class SpringAddonsMethodSecurityEvaluationContext extends MethodBasedEvaluationContext {

        SpringAddonsMethodSecurityEvaluationContext(
                MethodSecurityExpressionOperations root,
                MethodInvocation mi,
                ParameterNameDiscoverer parameterNameDiscoverer) {

            super(root, getSpecificMethod(mi), mi.getArguments(), parameterNameDiscoverer);
        }

        private static Method getSpecificMethod(MethodInvocation mi) {
            return AopUtils.getMostSpecificMethod(mi.getMethod(), AopProxyUtils.ultimateTargetClass(mi.getThis()));
        }
    }
}
