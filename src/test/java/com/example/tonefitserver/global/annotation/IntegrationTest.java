package com.example.tonefitserver.global.annotation;

import com.example.tonefitserver.global.config.TestSupportConfiguration;
import com.example.tonefitserver.global.config.TestcontainerConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("integration")
@Import({TestcontainerConfiguration.class, TestSupportConfiguration.class})
public @interface IntegrationTest {
}
