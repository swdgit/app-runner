/**
 * 
 */
package com.protolounge.apprunner;

import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author stacytt
 *
 */
public interface DocumentConverter {

    enum Status {SUCCESS,
                CONVERSION_EXCEPTION, 
                CONVERSION_FAILED,
                EXECUTION_EXCEPTION, 
                PASSWORD_PROTECTED}

    Status processDoc(MultipartFile presoFile, Path presentationPath, Path originalPresentationPath) throws Exception;
}
