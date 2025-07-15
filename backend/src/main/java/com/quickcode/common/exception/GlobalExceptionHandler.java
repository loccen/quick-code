package com.quickcode.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import com.quickcode.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 异常统计计数器
     */
    private final Map<String, AtomicLong> exceptionCounters = new ConcurrentHashMap<>();

    /**
     * 记录异常统计
     */
    private void recordExceptionStatistics(String exceptionType, Integer errorCode) {
        String key = exceptionType + "_" + errorCode;
        exceptionCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

        // 每100次异常记录一次统计日志
        long count = exceptionCounters.get(key).get();
        if (count % 100 == 0) {
            log.info("异常统计: {} 已发生 {} 次", key, count);
        }
    }

    /**
     * 获取异常统计信息
     */
    public Map<String, Long> getExceptionStatistics() {
        Map<String, Long> stats = new HashMap<>();
        exceptionCounters.forEach((key, counter) -> stats.put(key, counter.get()));
        return stats;
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", e.getCode(), e.getMessage());

        // 记录异常统计
        recordExceptionStatistics(e.getClass().getSimpleName(), e.getCode());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        if (e.getDetails() != null) {
            response.setData(e.getDetails());
        }

        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("资源不存在: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.notFound(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("验证异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.badRequest(e.getMessage());
        if (e.getDetails() != null) {
            response.setData(e.getDetails());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.badRequest("参数验证失败");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.badRequest("参数绑定失败");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束违反: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        ApiResponse<Object> response = ApiResponse.badRequest("数据验证失败");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败: {}", e.getMessage());

        String message = "认证失败";
        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        }

        ApiResponse<Object> response = ApiResponse.unauthorized(message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问被拒绝: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.forbidden("权限不足");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性违反: {}", e.getMessage());

        String message = "数据操作失败";
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，不能重复添加";
        } else if (e.getMessage().contains("foreign key constraint")) {
            message = "数据关联约束违反，无法删除";
        }

        ApiResponse<Object> response = ApiResponse.error(409, message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(405, "请求方法不支持");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("接口不存在: {}", e.getRequestURL());

        ApiResponse<Object> response = ApiResponse.notFound("接口不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {}", e.getMessage());

        String message = String.format("参数 '%s' 类型不正确", e.getName());
        ApiResponse<Object> response = ApiResponse.badRequest(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数: {}", e.getMessage());

        String message = String.format("缺少必需参数: %s", e.getParameterName());
        ApiResponse<Object> response = ApiResponse.badRequest(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理请求体不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体不可读: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.badRequest("请求体格式错误");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationFailedException(AuthenticationFailedException e, HttpServletRequest request) {
        log.warn("认证失败异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理资源重复异常
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException e, HttpServletRequest request) {
        log.warn("资源重复异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理状态无效异常
     */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidStateException(InvalidStateException e, HttpServletRequest request) {
        log.warn("状态无效异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理资源不足异常
     */
    @ExceptionHandler(InsufficientResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientResourceException(InsufficientResourceException e, HttpServletRequest request) {
        log.warn("资源不足异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理参数无效异常
     */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidParameterException(InvalidParameterException e, HttpServletRequest request) {
        log.warn("参数无效异常: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(response);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常: ", e);

        ApiResponse<Object> response = ApiResponse.error("系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }



    /**
     * 根据错误码获取HTTP状态码
     */
    private HttpStatus getHttpStatus(Integer code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // 根据错误码范围确定HTTP状态码
        if (code >= 1000 && code < 2000) {
            // 认证错误 (1xxx) -> 401 Unauthorized
            return HttpStatus.UNAUTHORIZED;
        } else if (code >= 2000 && code < 3000) {
            // 授权错误 (2xxx) -> 403 Forbidden
            return HttpStatus.FORBIDDEN;
        } else if (code >= 3000 && code < 4000) {
            // 验证错误 (3xxx) -> 400 Bad Request
            return HttpStatus.BAD_REQUEST;
        } else if (code >= 4000 && code < 5000) {
            // 资源错误 (4xxx) -> 404 Not Found (资源不存在) 或 409 Conflict (资源冲突)
            if (code == 4001) {
                return HttpStatus.NOT_FOUND; // 资源不存在
            } else {
                return HttpStatus.CONFLICT; // 资源冲突（如重复）
            }
        } else if (code >= 5000 && code < 6000) {
            // 状态错误 (5xxx) -> 400 Bad Request
            return HttpStatus.BAD_REQUEST;
        } else if (code >= 6000 && code < 7000) {
            // 业务逻辑错误 (6xxx) -> 400 Bad Request
            return HttpStatus.BAD_REQUEST;
        }

        // 传统错误码处理
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
