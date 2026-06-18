package com.bloodcircle.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's PostgreSQL connection string format:
 *   postgres://user:password@host:port/database
 * to Spring Boot's JDBC format:
 *   jdbc:postgresql://host:port/database
 * and extracts username/password into separate properties.
 */
@Component
public class DatabaseUrlConverter {

    public static void convertDatabaseUrl(ConfigurableEnvironment env) {
        String url = env.getProperty("SPRING_DATASOURCE_URL");
        if (url == null) url = env.getProperty("DATABASE_URL");

        if (url != null && url.startsWith("postgres://")) {
            // postgres://user:pass@host:port/db → jdbc:postgresql://host:port/db
            String withoutScheme = url.substring("postgres://".length());
            String credentials = withoutScheme.substring(0, withoutScheme.indexOf("@"));
            String hostAndDb = withoutScheme.substring(withoutScheme.indexOf("@") + 1);

            String username = credentials.contains(":") ? credentials.split(":")[0] : credentials;
            String password = credentials.contains(":") ? credentials.split(":", 2)[1] : "";

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", "jdbc:postgresql://" + hostAndDb);
            props.put("spring.datasource.username", username);
            props.put("spring.datasource.password", password);

            env.getPropertySources().addFirst(new MapPropertySource("renderDbUrl", props));
        } else if (url != null && url.startsWith("postgresql://")) {
            String withoutScheme = url.substring("postgresql://".length());
            String credentials = withoutScheme.substring(0, withoutScheme.indexOf("@"));
            String hostAndDb = withoutScheme.substring(withoutScheme.indexOf("@") + 1);

            String username = credentials.contains(":") ? credentials.split(":")[0] : credentials;
            String password = credentials.contains(":") ? credentials.split(":", 2)[1] : "";

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", "jdbc:postgresql://" + hostAndDb);
            props.put("spring.datasource.username", username);
            props.put("spring.datasource.password", password);

            env.getPropertySources().addFirst(new MapPropertySource("renderDbUrl", props));
        }
    }
}
