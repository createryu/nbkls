package com.yuqiliu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yuqiliu
 * @create 2020-06-22  19:38
 */

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
