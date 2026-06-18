package com.bloodcircle;

import com.bloodcircle.config.DatabaseUrlConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BloodCircleApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BloodCircleApplication.class);
        app.addInitializers(ctx -> {
            if (ctx.getEnvironment() instanceof org.springframework.core.env.ConfigurableEnvironment env) {
                DatabaseUrlConverter.convertDatabaseUrl(env);
            }
        });
        app.run(args);
    }
}
