package be.webtechie.vaadin.pi4j;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "myapp")
@Push
@PWA(name = "Pi4J Vaadin Demo", shortName = "Pi4J Demo")
public class Application implements AppShellConfigurator {

    // public is needed here, otherwise Spring can't find the main method!
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
