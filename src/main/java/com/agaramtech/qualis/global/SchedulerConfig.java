package com.agaramtech.qualis.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(
{
	"classpath:javaScheduler.properties",
    "classpath:databaseCleaner.properties"
})
public class SchedulerConfig {
}
