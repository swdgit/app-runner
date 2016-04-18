/**
 * 
 */
package com.protolounge.apprunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author stacy
 * used to clean up the temp file path on startup and context refresh before the first set of requests coming in.
 *
 */
@Component
public class PathCleanupBean implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(PathCleanupBean.class);

    @Value("${guid.root}")
    private String guidRoot;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        Path presoPath = Paths.get(guidRoot);
        log.debug("Cleaning up root temp path : {} ", presoPath.toAbsolutePath());
        FileUtils.deleteQuietly(presoPath.toFile());
        
        try {
            Files.createDirectories(presoPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Unable to create temp path: {} ", presoPath);
        }
    }
}