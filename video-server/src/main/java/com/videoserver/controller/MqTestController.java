package com.videoserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
public class MqTestController {

    // 已删除MqProducerService相关导入、注入、调用代码，无找不到符号报错

    @GetMapping("/send")
    public String sendTestMsg() {
        return "MQ生产者服务暂未启用，如需发送消息请先创建MqProducerService";
    }
}