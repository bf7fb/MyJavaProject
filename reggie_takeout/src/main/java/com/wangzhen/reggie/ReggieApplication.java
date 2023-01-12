package com.wangzhen.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wz
 * @ClassName ReggieApplication
 * @date 2023/1/5 15:48
 * @Description TODO
 */
@SpringBootApplication
@ServletComponentScan  // 扫描所有servlet/请求  过滤器用的
@EnableTransactionManagement // 添加事务管理
@EnableCaching // 开启springbootcache缓存
@Slf4j
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动完成");
    }
}
