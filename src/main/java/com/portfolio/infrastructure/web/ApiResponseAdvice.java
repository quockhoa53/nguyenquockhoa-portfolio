package com.portfolio.infrastructure.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> paramType = returnType.getParameterType();
        if (org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody.class.isAssignableFrom(
                        paramType)
                || org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.class.isAssignableFrom(
                        paramType)
                || byte[].class.isAssignableFrom(paramType)) {
            return false;
        }
        return returnType.getContainingClass().getPackageName().startsWith("com.portfolio");
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
                || body instanceof org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
                || body instanceof byte[]) {
            return body;
        }

        if (selectedContentType != null
                && (selectedContentType.includes(MediaType.TEXT_EVENT_STREAM)
                        || "audio".equalsIgnoreCase(selectedContentType.getType()))) {
            return body;
        }

        if (body instanceof ApiResponse) {
            return body;
        }

        if (body == null) {
            return ApiResponse.success("Thao tác thành công");
        }

        return ApiResponse.success(body);
    }
}
