/**
 * 
 */
package com.protolounge.apprunner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hslf.examples.PPT2PNG;
import org.apache.poi.xslf.util.PPTX2PNG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author stacytt
 * Generic ppt/x converter that will run off of any OS that is not Windows based. 
 */
public class DocumentConverterGeneric implements DocumentConverter {

    private static Logger log = LoggerFactory.getLogger(DocumentConverterGeneric.class);
    
    /* (non-Javadoc)
     * @see com.protolounge.apprunner.DocumentConverter#processDoc(org.springframework.web.multipart.MultipartFile, java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public Status processDoc(MultipartFile presoFile, Path presentationPath, Path originalPresentationPath) throws Exception {
        Status status = Status.CONVERSION_EXCEPTION;
        
        try {
            log.debug("Processing File : {} to path : {} ", presoFile.getOriginalFilename(), presentationPath.toAbsolutePath() );        
            Path presentationLocalFile = Paths.get(presentationPath.toAbsolutePath() + FileSystems.getDefault().getSeparator() + presoFile.getOriginalFilename());
            
            Files.write(presentationLocalFile, presoFile.getBytes(), StandardOpenOption.CREATE);
            
            log.debug("Converting file : {} to {}", presentationLocalFile, presentationPath.toAbsolutePath());
            convertPresentation(presentationLocalFile, presentationPath.toAbsolutePath());
            
            renameImages(presentationPath, presoFile.getOriginalFilename());
            
            status = Status.SUCCESS;
        } catch (Exception e) {
            log.error(e.getMessage());
            // evaluate the exception here to determine what status we send back... e.g. can't open -- password protected.
            String message = e.getMessage();
            if (message.contains("Can't open the specified file")) {
                status = Status.PASSWORD_PROTECTED;
            } else {
                status = Status.CONVERSION_EXCEPTION;
            }
            log.error("Exception Status: {}", status);
        }
        log.debug("Exit status : {} ", status);
        return status;
    }
    /**
     * If this is used in production, we will want to copy the code from ppt2png/pptx2png, insert status updates and not print to stdout
     * @param presentationFile
     * @param presentationPath
     * @throws Exception
     */
    private void convertPresentation(Path presentationFile, Path presentationPath) throws Exception {
        
        String[] args = {presentationFile.toAbsolutePath().toString() };
        if (presentationFile.toString().endsWith(".ppt")) {
            PPT2PNG.main(args);
        } else if (presentationFile.toString().endsWith(".pptx")) {
            PPTX2PNG.main(args);
        } else {
            throw new IllegalArgumentException("Unexpected file type: " + presentationFile.getFileName());
        }
    }
        
    /**
     * the generic java converter creates images such as <base ppt name>-1.png and they need to have the base name stripped off
     * @param presentationPath
     * @param filename
     * @throws Exception
     */
    private void renameImages(Path presentationPath, String filename) throws Exception {

        log.debug("renaming Files ");
        
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(presentationPath.toAbsolutePath());
            
            String newName = null;

            String originalFilename = FilenameUtils.removeExtension(filename) + "-";
            
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    newName = path.getFileName().toString().replace(originalFilename, "");
                    log.debug("renaming : {} to {} ", path.getFileName().toString(), newName);
                    Files.move(path, path.resolveSibling(newName));
                }
            }
            
        } catch (IOException e) {
            log.error("Issue renaming files when processing the generic way " + e.getMessage());
            throw e;
        } 
    }
}