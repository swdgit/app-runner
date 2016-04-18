/**
 * 
 */
package com.protolounge.apprunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.protolounge.apprunner.Application;
import com.protolounge.apprunner.DocumentConverter.Status;

/**
 * @author stacytt
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class DocControllerTest {

    private static Logger log = LoggerFactory.getLogger(DocControllerTest.class);
    RestTemplate template = new TestRestTemplate();
    
    /**
     * Test method for {@link com.protolounge.apprunner.DocController#getConversion(org.springframework.web.multipart.MultipartFile, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testValidPPTX() {

        ResponseEntity<byte[]> response = convertFile("/tt_test.pptx");

        String responseStatus = response.getHeaders().get("STATUS").get(0);
        
        if (Status.valueOf(responseStatus).equals(Status.SUCCESS)) {
            try {
                assertEquals(getEntryCount(response), 12);
            } catch (IOException e) {
                fail("Unexpected Exception processing zip file : " + e.getMessage());
            }
        } else {
            fail("Unexpected Status : " + responseStatus);
        }
        
    }

    @Test
    public void testFailOnPassword() {
        ResponseEntity<byte[]> response = convertFile("/tt_test_pwd.pptx");
        
        String responseStatus = response.getHeaders().get("STATUS").get(0);
        log.debug("response status : {}", responseStatus);
        
        assertTrue(Status.valueOf(responseStatus).equals(Status.PASSWORD_PROTECTED));
    }
    
    @Test 
    public void testValidPPT() {
        ResponseEntity<byte[]> response = convertFile("/tt_test.ppt");

        String responseStatus = response.getHeaders().get("STATUS").get(0);
        
        if (Status.valueOf(responseStatus).equals(Status.SUCCESS)) {
            try {
                assertEquals(getEntryCount(response), 12);
            } catch (IOException e) {
                fail("Unexpected Exception processing zip file : " + e.getMessage());
            }
        } else {
            fail("Unexpected Status : " + responseStatus);
        }
    }
    
    /**
     * @param filename
     * @return
     */
    private ResponseEntity<byte[]> convertFile(String filename) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

        Path testFile = Paths.get(getClass().getResource(filename).getFile());
        log.info("testing File : {}", testFile);
        
        map.add("file", new FileSystemResource(testFile.toAbsolutePath().toString()));
   
        ResponseEntity<byte[]> response = template.postForEntity("http://localhost:8080/pptx", map, byte[].class);
        
        return response;
    }

    /**
     * @param response
     * @return
     * @throws IOException
     */
    private int getEntryCount(ResponseEntity<byte[]> response) throws IOException {
        ByteArrayInputStream bais      = new ByteArrayInputStream(response.getBody());
        ZipInputStream       zipStream = new ZipInputStream(bais);
        ZipEntry             entry     = null;
   
        int entryCount = 0;
         
        while((entry = zipStream.getNextEntry()) != null) {
            if (entry.getName().endsWith("png")) {
                entryCount++;
            }
        }
   
        zipStream.close();
        return entryCount;
    }

    /**
     * Test method for {@link com.protolounge.apprunner.DocController#getStatus()}.
     */
    @Test
    public void testGetStatus() {
        ResponseEntity<String> response = template.getForEntity("http://localhost:8080/alive", String.class);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        
        assertEquals("I'm Alive", response.getBody().toString());
    }
}