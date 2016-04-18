/**
 * 
 */
package com.protolounge.apprunner.service;

import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
/**
 * @author stacytt
 *
 */
@Service
public class PPTXConverterServiceImpl implements PPTXConverterService {

    Logger log = LoggerFactory.getLogger(PPTXConverterServiceImpl.class);

    /* (non-Javadoc)
     * @see com.protolounge.apprunner.PPTXConverterService#convert(java.io.File)
     */
    @Override
    @Async("taskExecutor")
    public void cleanup(Path presoPath) {
        FileUtils.deleteQuietly(presoPath.toFile());
    }
}
