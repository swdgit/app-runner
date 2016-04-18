/**
 * 
 */
package com.protolounge.apprunner.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.protolounge.apprunner.DocumentConverter;
import com.protolounge.apprunner.DocumentConverterGeneric;
import com.protolounge.apprunner.DocumentConverterWindows;

/**
 * @author stacytt
 *
 */
@Configuration
public class ApplicationConfiguration {

    private static Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);
    
    @Value("${tomcat.max.connections}")
    private String maxConnections;

    
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        
        tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {

            @Override
            public void customize(Connector connector) {
                ((AbstractProtocol<?>)connector.getProtocolHandler()).setMaxConnections(Integer.valueOf(maxConnections));
            }
            
        });
        
        return tomcat;
    }
    
    @Bean(name="documentConverter")
    @Conditional(value=ConditionWindows.class)
    public DocumentConverter documentConverterWindows() {
        log.debug("ConditionWindows : create DocumentConverterWindows");
        
        return new DocumentConverterWindows();
    }

    @Bean(name="documentConverter")
    @Conditional(value=ConditionGeneric.class)
    public DocumentConverter documentConverterGeneric() {
        log.debug("ConditionGeneric : create DocumentConverterGeneric");
        
        return new DocumentConverterGeneric();
    }
}