package org.brownie.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * The entry point of the Spring Boot application.
 */
@ServletComponentScan
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    public static final long MAX_UPLOAD_FILE_SIZE = 20480000000L;
    public static final long MAX_REQUEST_SIZE = 20480000000L;

    public static final System.Logger LOGGER = System.getLogger("Brownie server");

    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(MAX_UPLOAD_FILE_SIZE));
        factory.setMaxRequestSize(DataSize.ofBytes(MAX_REQUEST_SIZE));
        return factory.createMultipartConfig();
    }
}
