package org.brownie.server;

import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.recoder.VideoDecoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.nio.file.Paths;

/**
 * The entry point of the Spring Boot application.
 */
@ServletComponentScan
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    public static final String BASE_PATH =
            System.getProperty("catalina.base") == null ?
                    Paths.get("").toFile().getAbsolutePath() : System.getProperty("catalina.base");
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static final long MAX_UPLOAD_FILE_SIZE = 40960000000L;
    public static final long MAX_REQUEST_SIZE = 40960000000L;

    public static final System.Logger LOGGER = System.getLogger("Brownie server");

    public static void main(String[] args) {
        DBConnectionProvider.getInstance();

    	ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                if (!VideoDecoder.getDecoder().getExecutor().isShutdown())
                    VideoDecoder.getDecoder().getExecutor().shutdown();
            }
        });
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(MAX_UPLOAD_FILE_SIZE));
        factory.setMaxRequestSize(DataSize.ofBytes(MAX_REQUEST_SIZE));
        return factory.createMultipartConfig();
    }
}
