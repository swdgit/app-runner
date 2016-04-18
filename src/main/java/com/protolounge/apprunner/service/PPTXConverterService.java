/**
 * 
 */
package com.protolounge.apprunner.service;

import java.nio.file.Path;

import org.springframework.scheduling.annotation.Async;

import com.protolounge.AppRunnerException;

/**
 * @author stacytt
 *
 */
public interface PPTXConverterService {

    /**
     * take the given pptx file and convert it to images for ThinkTank to leverage.
     * @param pptxFile
     * @param presoPath
     * @return
     * @throws AppRunnerException
     */
    @Async
    public void cleanup(Path pptxFile);
}
