package com.wangzhen.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author wz
 * @ClassName ReggieApplication
 * @date 2023/1/5 15:48
 * @Description TODO
 */
@SpringBootApplication
@ServletComponentScan
@Slf4j
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动完成");
    }
}
