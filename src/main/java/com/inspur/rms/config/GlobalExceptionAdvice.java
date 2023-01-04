package com.inspur.rms.config;

import com.inspur.ivideo.common.constant.BaseErrorInfo;
import com.inspur.ivideo.common.entity.Error;
import com.inspur.ivideo.common.entity.ResStatus;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.exception.Res404Exception;
import com.inspur.ivideo.common.exception.ResException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 配置类常量
 *
 * @author : lidongbin
 * @ClassName : GlobalExceptionHandler
 * @Description :
 * @date : 统一异常处理 1:32 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * try catch中返回的异常
     *
     * @param response
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(ResException.class)
    public ResStatus resExceptionHandler(HttpServletResponse response, HttpServletRequest request, ResException e) {
        log.error("has some error in request: [{}],method: [{}], errorCode: [{}], errorMsg: [{}], Exception: [{}]",
                request.getRequestURI(), request.getMethod(), e.getBaseErrorInfo().getStatus(), e.getBaseErrorInfo().getMsg(), e);
        e.printStackTrace();
        BaseErrorInfo resStatusEnum = e.getBaseErrorInfo();
        String msg = e.getMessage();
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code(resStatusEnum.getStatus())
                        .message(msg).build()).build();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return resStatus;
    }

    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    public ResStatus nullPointerExceptionHandle(HttpServletResponse response, HttpServletRequest request, NullPointerException e) {
        log.error("has exception in request: [{}],method: [{}], exception: [{}]", request.getRequestURI(), request.getMethod(), e);
        e.printStackTrace();
        String msg = e.getMessage();
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code("846.011.098")
                        .message("系统忙，请稍后再试!").build()).build();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return resStatus;
    }

    @ResponseBody
    @ExceptionHandler(Res404Exception.class)
    public ResStatus res404ExceptionHandler(HttpServletResponse response, HttpServletRequest request, Res404Exception e) {
        log.error("has some error in request: [{}],method: [{}], errorCode: [{}], errorMsg: [{}], Exception: [{}]",
                request.getRequestURI(), request.getMethod(), e.getCode(), e.getMessage(), e);
        e.printStackTrace();
        BaseErrorInfo baseErrorInfo = e.getBaseErrorInfo();
        String code = e.getCode();
        String message = e.getMessage();
        List<Error> errors = e.getErrors();
        String msg = e.getMessage();

        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code(code)
                        .errors(errors)
                        .message(msg).build()).build();
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return resStatus;
    }

    /**
     * 返回http.status=400错误
     *
     * @param response
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(Res400Exception.class)
    public ResStatus res400ExceptionHandler(HttpServletResponse response, HttpServletRequest request, Res400Exception e) {
        log.error("has some error in request: [{}],method: [{}], errorCode: [{}], errorMsg: [{}], Exception: [{}]",
                request.getRequestURI(), request.getMethod(), e.getCode(), e.getMessage(), e);
        e.printStackTrace();
        BaseErrorInfo baseErrorInfo = e.getBaseErrorInfo();
        String code = e.getCode();
        String message = e.getMessage();
        String msg = e.getMessage();
        List<Error> errors = e.getErrors();
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code(code)
                        .errors(errors)
                        .message(msg).build()).build();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return resStatus;
    }

    //    spring-validation 验证报错
    @ResponseBody
    @ExceptionHandler(BindException.class)
    public ResStatus bindExceptionHandler(HttpServletResponse response, HttpServletRequest request, BindException e) {
        log.error("has exception in request: [{}],method: [{}], exception: [{}]", request.getRequestURI(), request.getMethod(), e);
        e.printStackTrace();
        BindingResult result = e.getBindingResult();
        String res = "";
        if (result.hasErrors()) {
            res = result.getAllErrors().get(0).getDefaultMessage();
        }
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code("846.011.099")
                        .message(res).build()).build();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return resStatus;
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResStatus methodArgumentNotValidExceptionHandler(HttpServletResponse response, HttpServletRequest request, MethodArgumentNotValidException e) {
        log.error("has exception in request: [{}],method: [{}], exception: [{}]", request.getRequestURI(), request.getMethod(), e);
        e.printStackTrace();
        BindingResult result = e.getBindingResult();
        String res = "";
        if (result.hasErrors()) {
            res = result.getAllErrors().get(0).getDefaultMessage();
        }
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code("846.011.099")
                        .message(res).build()).build();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return resStatus;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResStatus exceptionHandler(HttpServletResponse response, HttpServletRequest request, Exception e) {
        log.error("has exception in request: [{}],method: [{}], exception: [{}]", request.getRequestURI(), request.getMethod(), e);
        e.printStackTrace();
        String msg = e.getMessage();
        ResStatus resStatus = ResStatus
                .builder()
                .error(Error
                        .builder()
                        .code("846.011.098")
                        .message("系统忙，请稍后再试!").build()).build();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return resStatus;
    }
}

    