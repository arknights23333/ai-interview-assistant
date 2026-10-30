package com.assistant.aiinterviewassistant.common.exception;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.MailErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.PasswordErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.RegisterErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.exception.handler.GlobalExceotionHandler;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.MailSendDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("统一业务异常的响应兼容性")
class BusinessExceptionContractTests {

    // 固定迁移前的协议，防止仅使用枚举自身作为期望值而漏掉意外改码或改文案。
    static Stream<ExpectedError> originalErrors() {
        return Stream.of(
                new ExpectedError(LoginErrorCode.BAD_CREDENTIAL, 40101, HttpStatus.UNAUTHORIZED, "账号或密码错误"),
                new ExpectedError(LoginErrorCode.LOGIN_FAILED, 50003, HttpStatus.INTERNAL_SERVER_ERROR, "登录失败，请稍后重试"),
                new ExpectedError(RegisterErrorCode.USERNAME_ALREADY_EXISTS, 40901, HttpStatus.CONFLICT, "用户名已存在"),
                new ExpectedError(RegisterErrorCode.EMAIL_ALREADY_BOUND, 40902, HttpStatus.CONFLICT, "该邮箱已绑定账号"),
                new ExpectedError(RegisterErrorCode.REGISTER_FAILED, 50004, HttpStatus.INTERNAL_SERVER_ERROR, "注册失败，请稍后重试"),
                new ExpectedError(MailErrorCode.INVALID_EMAIL, 40001, HttpStatus.BAD_REQUEST, "收件邮箱格式错误"),
                new ExpectedError(MailErrorCode.CODE_MISMATCH, 40002, HttpStatus.BAD_REQUEST, "验证码不正确"),
                new ExpectedError(MailErrorCode.CODE_EXPIRED, 40003, HttpStatus.BAD_REQUEST, "验证码已过期，请重新获取"),
                new ExpectedError(MailErrorCode.SEND_TOO_FREQUENT, 42901, HttpStatus.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后重试"),
                new ExpectedError(MailErrorCode.SEND_FAILED, 50001, HttpStatus.INTERNAL_SERVER_ERROR, "邮件发送失败，请稍后重试"),
                new ExpectedError(MailErrorCode.INVALID_GENERATED_CODE, 50002, HttpStatus.INTERNAL_SERVER_ERROR, "验证码生成异常，请稍后重试"),
                new ExpectedError(PasswordErrorCode.OLD_PASSWORD_MIS_MATCH, 40004, HttpStatus.BAD_REQUEST, "旧密码不匹配"),
                new ExpectedError(PasswordErrorCode.NEW_PASSWORD_SAME_AS_OLD, 40005, HttpStatus.BAD_REQUEST, "新密码与旧密码相同"),
                new ExpectedError(PasswordErrorCode.VERIFICATION_REQUIRED, 40006, HttpStatus.BAD_REQUEST, "请提供旧密码或邮箱验证码"),
                new ExpectedError(PasswordErrorCode.PASSWORD_RESET_FAILED, 50005, HttpStatus.INTERNAL_SERVER_ERROR, "密码重置失败，请稍后重试"),
                new ExpectedError(UserErrorCode.USER_NOT_FOUND, 40401, HttpStatus.NOT_FOUND, "用户不存在"),
                new ExpectedError(UserErrorCode.USER_DISABLED, 40102, HttpStatus.UNAUTHORIZED, "账号已禁用")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("originalErrors")
    @DisplayName("各业务枚举保留原错误码、状态、提示和异常原因")
    void preservesExceptionContract(ExpectedError expected) {
        RuntimeException cause = new IllegalStateException("仅供后端排查的原始异常");
        BusinessException exception = new BusinessException(expected.error(), cause);
        assertAll(
                () -> assertEquals(expected.code(), exception.getCode()),
                () -> assertEquals(expected.status(), exception.getHttpStatus()),
                () -> assertEquals(expected.message(), exception.getMessage()),
                () -> assertSame(cause, exception.getCause()),
                () -> assertEquals(expected.message(), new BusinessException(expected.error()).getMessage())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("originalErrors")
    @DisplayName("所有业务错误经同一处理器返回原 HTTP 状态和统一响应体")
    void preservesHttpContract(ExpectedError expected) throws Exception {
        mvc(new BusinessErrorController(expected.error())).perform(get("/test/business"))
                .andExpect(status().is(expected.status().value()))
                .andExpect(jsonPath("$.code").value(expected.code()))
                .andExpect(jsonPath("$.msg").value(expected.message()))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.cause").doesNotExist());
    }

    @Test
    @DisplayName("所有业务错误码全局唯一且已被兼容性用例覆盖")
    void errorCodesAreUniqueAndCovered() {
        BusinessErrorCode[] errors = Stream.of(LoginErrorCode.values(), RegisterErrorCode.values(),
                        MailErrorCode.values(), PasswordErrorCode.values(), UserErrorCode.values())
                .flatMap(Arrays::stream).toArray(BusinessErrorCode[]::new);
        assertEquals(errors.length, Arrays.stream(errors).map(BusinessErrorCode::getCode).distinct().count());
        assertEquals(errors.length, originalErrors().count());
    }

    @Test
    @DisplayName("参数校验仍返回具体字段提示")
    void preservesValidationMessage() throws Exception {
        mvc(new InputController()).perform(post("/test/input").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.msg").value("字段不能为空"));
    }

    @Test
    @DisplayName("缺少验证码用途仍由参数校验处理")
    void preservesMissingSceneResponse() throws Exception {
        mvc(new InputController()).perform(post("/test/mail").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.msg").value("请选择验证码用途"));
    }

    @Test
    @DisplayName("非法用途枚举仍返回请求体解析错误")
    void preservesInvalidSceneResponse() throws Exception {
        mvc(new InputController()).perform(post("/test/mail").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"scene\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.msg").value("请求体格式或字段值不正确"));
    }

    @Test
    @DisplayName("未预期异常仍返回安全的服务器错误提示")
    void unexpectedFailureDoesNotExposeInternalMessage() throws Exception {
        mvc(new InputController()).perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("服务器内部错误，请稍后重试"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    private MockMvc mvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceotionHandler()).build();
    }

    record ExpectedError(BusinessErrorCode error, int code, HttpStatus status, String message) {
        @Override
        public String toString() {
            return error.toString();
        }
    }

    /**
     * 仅供业务异常响应回归测试使用，不同步到 Apifox。
     *
     * @ignore
     */
    @RestController
    static class BusinessErrorController {
        private final BusinessErrorCode error;

        BusinessErrorController(BusinessErrorCode error) {
            this.error = error;
        }

        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @GetMapping("/test/business")
        public void fail() {
            throw new BusinessException(error, new IllegalStateException("内部连接信息不得返回"));
        }
    }

    /**
     * 仅供参数校验和异常兜底测试使用，不同步到 Apifox。
     *
     * @ignore
     */
    @RestController
    static class InputController {
        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @PostMapping("/test/input")
        public void input(@Valid @RequestBody Input input) {
        }

        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @PostMapping("/test/mail")
        public void mail(@Valid @RequestBody MailSendDto dto) {
        }

        /**
         * 仅用于自动化测试，不导出为 Apifox 接口。
         *
         * @ignore
         */
        @GetMapping("/test/unexpected")
        public void unexpected() {
            throw new IllegalStateException("内部数据库连接信息不得返回");
        }
    }

    record Input(@NotBlank(message = "字段不能为空") String value) {
    }
}
