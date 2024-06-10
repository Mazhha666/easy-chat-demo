package com.atmiao.wechatdemo.exp_handler;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author miao
 * @version 1.0
 */
//@ControllerAdvice
public class PageErrorHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleArithmeticException(Exception ex, Model model){
        model.addAttribute("ex", ex);
        return ex.getMessage();
    }
}
