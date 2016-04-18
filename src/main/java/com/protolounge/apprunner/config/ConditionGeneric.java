/**
 * 
 */
package com.protolounge.apprunner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author stacytt
 *
 */
@Configuration
public class ConditionGeneric implements Condition {

    private static Logger log = LoggerFactory.getLogger(ConditionGeneric.class);
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        log.debug("OS : {}", context.getEnvironment().getProperty("os.name"));
    
        return !context.getEnvironment().getProperty("os.name").contains("Windows");
    }
}