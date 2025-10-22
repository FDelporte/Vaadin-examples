package be.webtechie.vaadin.pi4j.config;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Pi4JConfig {

    private static final Logger logger = LoggerFactory.getLogger(Pi4JConfig.class);

    @Value("${crowpi.version:not-set}")
    private String crowpiVersion;

    @Bean
    public Context pi4jContext() {
        logger.info("Configured CrowPi Version: {}", crowpiVersion);

        return Pi4J.newAutoContext();
    }
}
