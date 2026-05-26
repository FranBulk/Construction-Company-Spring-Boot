package com.constructioncompany;

import com.constructioncompany.config.MessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MessagingProperties.class)
public class ConstructionCompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstructionCompanyApplication.class, args);
    }
}
