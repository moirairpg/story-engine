package me.moirai.storyengine.infrastructure.security.authorization;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.moirai.storyengine.common.exception.UnauthorizedException;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final OperationAuthorizerFactory authorizerFactory;
    private final JsonMapper jsonMapper;

    public AuthorizationInterceptor(OperationAuthorizerFactory authorizerFactory, JsonMapper jsonMapper) {
        this.authorizerFactory = authorizerFactory;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        var annotation = handlerMethod.getMethodAnnotation(Authorize.class);
        if (annotation == null) {
            return true;
        }

        var fields = resolveFields(annotation.fields(), request);
        var principal = (MoiraiPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var context = new AuthorizationContext(principal, fields);
        var authorized = authorizerFactory.getAuthorizer(annotation.operation()).authorize(context);

        if (!authorized) {
            throw new UnauthorizedException("Access denied for operation: " + annotation.operation());
        }

        return true;
    }

    private Map<String, Object> resolveFields(String[] fieldExpressions, HttpServletRequest request) throws Exception {
        Map<String, Object> resolved = new HashMap<>();
        JsonNode bodyNode = null;

        for (String expression : fieldExpressions) {
            int colonIndex = expression.indexOf(':');
            String source = expression.substring(0, colonIndex);
            String name = expression.substring(colonIndex + 1);

            switch (source) {
                case "path" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> pathVariables = (Map<String, String>) request
                            .getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
                    if (pathVariables != null) {
                        resolved.put(name, pathVariables.get(name));
                    }
                }
                case "param" -> resolved.put(name, request.getParameter(name));
                case "body" -> {
                    if (bodyNode == null) {
                        bodyNode = parseBody(request);
                    }
                    if (bodyNode != null) {
                        resolved.put(leafKey(name), navigatePath(bodyNode, name));
                    }
                }
            }
        }

        return resolved;
    }

    private JsonNode parseBody(HttpServletRequest request) throws Exception {
        byte[] body;
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            body = wrapper.getContentAsByteArray();
            if (body.length == 0) {
                body = request.getInputStream().readAllBytes();
            }
        } else {
            body = request.getInputStream().readAllBytes();
        }

        if (body.length == 0) {
            return null;
        }

        return jsonMapper.readTree(body);
    }

    private Object navigatePath(JsonNode root, String dotPath) {
        String[] parts = dotPath.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            current = current.get(part);
        }
        if (current == null || current.isNull() || current.isMissingNode()) {
            return null;
        }
        if (current.isString()) {
            return current.asString();
        }
        return current.toString();
    }

    private String leafKey(String dotPath) {
        int lastDot = dotPath.lastIndexOf('.');
        return lastDot >= 0 ? dotPath.substring(lastDot + 1) : dotPath;
    }
}
