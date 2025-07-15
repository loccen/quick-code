package com.quickcode.unit.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.quickcode.common.exception.AuthenticationFailedException;
import com.quickcode.common.exception.DuplicateResourceException;
import com.quickcode.common.exception.ErrorCode;
import com.quickcode.common.exception.GlobalExceptionHandler;
import com.quickcode.common.exception.InsufficientResourceException;
import com.quickcode.common.exception.InvalidStateException;
import com.quickcode.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionHandler 单元测试
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("AuthenticationFailedException 处理测试")
    class AuthenticationFailedExceptionTests {

        @Test
        @DisplayName("应该正确处理密码不一致异常")
        void shouldHandlePasswordMismatchException() {
            // Arrange
            AuthenticationFailedException exception = AuthenticationFailedException.passwordMismatch();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.PASSWORD_MISMATCH));
            assertThat(response.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("应该正确处理邮箱验证码错误异常")
        void shouldHandleInvalidEmailCodeException() {
            // Arrange
            AuthenticationFailedException exception = AuthenticationFailedException.invalidEmailCode();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.EMAIL_CODE_INVALID);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.EMAIL_CODE_INVALID));
        }

        @Test
        @DisplayName("应该正确处理用户协议未同意异常")
        void shouldHandleTermsNotAgreedException() {
            // Arrange
            AuthenticationFailedException exception = AuthenticationFailedException.termsNotAgreed();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.TERMS_NOT_AGREED);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.TERMS_NOT_AGREED));
        }
    }

    @Nested
    @DisplayName("DuplicateResourceException 处理测试")
    class DuplicateResourceExceptionTests {

        @Test
        @DisplayName("应该正确处理用户名已存在异常")
        void shouldHandleUsernameExistsException() {
            // Arrange
            DuplicateResourceException exception = DuplicateResourceException.usernameExists("testuser");

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleDuplicateResourceException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.USERNAME_EXISTS);
            assertThat(response.getBody().getMessage()).isEqualTo("用户名已存在: testuser");
            assertThat(response.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("应该正确处理邮箱已存在异常")
        void shouldHandleEmailExistsException() {
            // Arrange
            DuplicateResourceException exception = DuplicateResourceException.emailExists("test@example.com");

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleDuplicateResourceException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.EMAIL_EXISTS);
            assertThat(response.getBody().getMessage()).isEqualTo("邮箱已存在: test@example.com");
        }
    }

    @Nested
    @DisplayName("InvalidStateException 处理测试")
    class InvalidStateExceptionTests {

        @Test
        @DisplayName("应该正确处理用户已被禁用异常")
        void shouldHandleUserDisabledException() {
            // Arrange
            InvalidStateException exception = InvalidStateException.userDisabled();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleInvalidStateException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.USER_DISABLED);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.USER_DISABLED));
        }

        @Test
        @DisplayName("应该正确处理用户已被锁定异常")
        void shouldHandleUserLockedException() {
            // Arrange
            InvalidStateException exception = InvalidStateException.userLocked();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleInvalidStateException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.USER_LOCKED);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.USER_LOCKED));
        }

        @Test
        @DisplayName("应该正确处理邮箱已验证异常")
        void shouldHandleEmailAlreadyVerifiedException() {
            // Arrange
            InvalidStateException exception = InvalidStateException.emailAlreadyVerified();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleInvalidStateException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_VERIFIED);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.EMAIL_ALREADY_VERIFIED));
        }
    }

    @Nested
    @DisplayName("InsufficientResourceException 处理测试")
    class InsufficientResourceExceptionTests {

        @Test
        @DisplayName("应该正确处理积分不足异常")
        void shouldHandleInsufficientPointsException() {
            // Arrange
            InsufficientResourceException exception = InsufficientResourceException.insufficientPoints();

            // Act
            ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleInsufficientResourceException(exception, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INSUFFICIENT_POINTS);
            assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.getDefaultMessage(ErrorCode.INSUFFICIENT_POINTS));
        }
    }
}
