/**
 * 
 */
package com.protolounge.apprunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author stacytt
 *
 */
public class DocumentConverterWindows implements DocumentConverter {

    private static Logger log = LoggerFactory.getLogger(DocumentConverterWindows.class);

    @Value("${application.exec}")
    private String converterApplication;

    /* (non-Javadoc)
     * @see com.protolounge.apprunner.DocumentConverter#processDoc(org.springframework.web.multipart.MultipartFile, java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public Status processDoc(MultipartFile presoFile, Path presentationPath, Path originalPresentationPath) throws Exception {
        Status status = Status.SUCCESS;
        try {
            Path presentationLocalFile = Paths.get(originalPresentationPath.toString() + File.separator + presoFile.getOriginalFilename());
    
            Files.write(presentationLocalFile, presoFile.getBytes(), StandardOpenOption.CREATE);
            
            Runtime runtime = Runtime.getRuntime();
            
            Path currentRelativePath = Paths.get(converterApplication);
            
            log.debug("# procs : {} free mem : {} max mem : {} Application : {}  Processing File : {} to path : {} ", 
                      runtime.availableProcessors(), runtime.freeMemory(), runtime.maxMemory(), currentRelativePath.toAbsolutePath(), presentationLocalFile.toString(), presentationPath.toString() );
            
            String[] execute = {currentRelativePath.toAbsolutePath().toString(), presentationLocalFile.toString(), presentationPath.toString() + File.separator};
            
            Process proc = runtime.exec(execute);
    
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    
                    if (line.startsWith("Converted slide: ")) {
                        log.debug(line);
                    } else if (line.startsWith("Conversion failed")) {
                        log.debug(line);
                        if (line.contains("password")) {
                            // if the error message contains the word password, we have very likely
                            // hit a password protected PowerPoint file
                            status = Status.PASSWORD_PROTECTED;
                            log.debug("The presentation file you uploaded appears to be password protected and cannot be processed. Please remove the password protection and try again. Status {}", status);
                        } else {
                            status = Status.CONVERSION_FAILED;
                            log.debug("The PowerPoint file you uploaded is unsupported. Status {}", status);
                        }
                    }
                }
            }
    
            // the processed output result from the conversion app and if exit code is anything but 0 toss an exception.
            if (proc.exitValue() != 0) {
                log.error("Converter App had an issue. Exit Code : {} Message : {} ", proc.exitValue(), IOUtils.toString(proc.getErrorStream()));
                // don't override any status updates that may have happened above.
                if (Status.SUCCESS.equals(status)) {
                    status = Status.CONVERSION_EXCEPTION;
                }
            }
        } catch (Exception e) {
            log.error("Conversion failed for an unexpected reason : {} ", e.getMessage());
            // don't override any status updates that may have happened above.
            if (Status.SUCCESS.equals(status)) {
                status = Status.CONVERSION_EXCEPTION;
            }
        }
        
        return status;
    }
}