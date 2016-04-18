package com.protolounge.apprunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.protolounge.AppRunnerException;
import com.protolounge.apprunner.DocumentConverter.Status;
import com.protolounge.apprunner.service.PPTXConverterService;

/**
 * @author stacy
 * Main document controller to handle requests for processing documents.
 */
@RestController
public class DocController {

    private static Logger log = LoggerFactory.getLogger(DocController.class);
    
    @Value("${guid.root}")
    private String guidRoot;
    
    @Autowired
    private PPTXConverterService converterService;

    @Autowired
    private DocumentConverter documentConverter;
        
    @RequestMapping("/")
    public String index() {
        return "Think Tank Document Conversion Micro Service";
    }
    
    /**
     * Trying this a little differently to see if we can keep it all in one. Works well as a single call and the 
     * external call to the ppt converter works well if more than one call is made to the service.
     * @param presoFile
     * @param response
     * @return
     * @throws AppRunnerException
     */
    @RequestMapping(value = "/pptx", method = RequestMethod.POST)
    public String getConversion(@RequestParam(value = "file") MultipartFile presoFile,
                                HttpServletResponse response) throws AppRunnerException {
        
        Status status = Status.SUCCESS;
        // add the header status here because we can't after flushed.
        response.addHeader("STATUS", status.toString());
        
        String uuid = UUID.randomUUID().toString();
        
        Path presoPath = Paths.get(guidRoot + uuid);
        Path presentationFile = Paths.get(presoPath.toAbsolutePath() + File.separator + "original");

        try {
            Files.createDirectory(presoPath);
            log.debug("presoPath created         : {} ", presoPath.toAbsolutePath());
            
            Files.createDirectory(presentationFile);
            log.debug("originalPresoPath created : {} ", presentationFile.toAbsolutePath());

            status = documentConverter.processDoc(presoFile, presoPath, presentationFile);
            if (Status.SUCCESS.equals(status)) {
                // keeping the zip file in memory
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // zipping up the items in the path
                try(ZipOutputStream zos = new ZipOutputStream(baos);
                    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(presoPath)) {
                    
                    log.debug("Starting Zip file");
    
                    ZipEntry entry = null;
                    // pulled this out as we need to close the stream to be able to delete the file on the filesystem.
                    FileInputStream fis = null;
    
                    for (Path path : directoryStream) {
                        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
    
                            log.debug("Adding File {} ", path.getFileName());
                            entry = new ZipEntry(path.getFileName().toString()); 
                            zos.putNextEntry(entry);
                            fis = new FileInputStream(path.toAbsolutePath().toString());
                            IOUtils.copyLarge(fis, zos);
                            
                            fis.close();
                            zos.closeEntry();
                        }
                    }
                    zos.flush();
                    zos.close();
                    
                    baos.flush();
                    baos.close();
                    
                } catch(IOException ioe) {
                  log.error(ioe.getMessage());
                  status = Status.CONVERSION_EXCEPTION;
                }
                
                // got this trick from a google search to avoid having to "return and entity".
                // https://twilblog.github.io/java/spring/rest/file/stream/2015/08/14/return-a-file-stream-from-spring-rest.html
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.setBufferSize(baos.size());
    
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                
                IOUtils.copyLarge(bais, response.getOutputStream());
                
                bais.close();
                
                response.flushBuffer();
            }

        } catch (Exception e) {
            status = Status.CONVERSION_EXCEPTION;
            log.error(e.getMessage());
        } finally {
            converterService.cleanup(presoPath);
        }
        
        response.setHeader("STATUS", status.toString());
        return status.toString();
    }
    
    /**
     * Just a dummy status check.
     * @return
     */
    @RequestMapping(value = "/alive", method = RequestMethod.GET) 
    public String getStatus() {
        return "I'm Alive";
    }   
}