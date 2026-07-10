package com.video.videoserver.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String,Object> error(Exception e){
        Map<String,Object> res = new HashMap<>();
        res.put("code",500);
        res.put("msg",e.getMessage());
        return res;
    }
}