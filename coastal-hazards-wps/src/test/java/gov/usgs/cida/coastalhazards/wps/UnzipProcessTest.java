package gov.usgs.cida.coastalhazards.wps;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.geotools.process.ProcessException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author cschroed
 */
public class UnzipProcessTest {
    
    private UnzipProcess instance;
    private static final String TEST_TOKEN = "ASDFQWER";
    private Path tempDir; 

    public UnzipProcessTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        instance = new UnzipProcess();
        Map<String, String> kvp = new HashMap<>();
        kvp.put(UnzipProcess.TOKEN_PROPERTY_NAME, TEST_TOKEN);
        tempDir = Files.createTempDirectory("temp");
        kvp.put(UnzipProcess.UNZIP_BASE_NAME, tempDir.toAbsolutePath().toString());
        instance.setProperties(new DynamicReadOnlyProperties(kvp));
    }
    
    @After
    public void tearDown() throws IOException { 
        //delete all temp files recursively
        Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Test of execute method, of class UnzipProcess.
     */
    @Test
    public void testExecute() {
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZipFromUrl method, of class UnzipProcess.
     */
    @Test
    public void testGetZipFromBadUrl() {
        int[] badCodes = new int[]{ 400, 401, 404, 500, 501};
        for(int badCode : badCodes){
            HttpClient client = mockHttpClient(badCode, null);
            try{
                instance.getZipFromUrl("http://owi.usgs.gov", client);
                fail("Should throw exception on HTTP error");
            } catch (ProcessException ex){
                //do nothing, this is expected
            }
        }
        return; //Exceptions were thrown on all HTTP errors
    }

    /**
     * Test of getZipFromUrl method, of class UnzipProcess.
     */
    @Test
    public void testGetZipFromGoodUrls() {
        int[] goodCodes = new int[]{200, 301, 302, 304, 307};
        byte[] empty = new byte[]{};
        for(int goodCode : goodCodes){
            HttpClient client = mockHttpClient(goodCode, new ByteArrayInputStream(empty));
            try{
                ZipInputStream zipStream = instance.getZipFromUrl("http://owi.usgs.gov", client);
                assertNotNull(zipStream);
            } catch (ProcessException ex){
                fail("Should not throw exception on good HTTP status codes");
            }
        }
        return; //No Exceptions were thrown on any HTTP status codes
    }
    
    /**
     * Test of unzipToDir method, of class UnzipProcess.
     */
    @Test
    public void testUnzipToDir() {
        fail("The test case is a prototype.");
    }
    public HttpClient mockHttpClient(int code, InputStream content){
        
        
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(code);
        
        HttpEntity entity = mock(HttpEntity.class);
        
        try {
            when(entity.getContent()).thenReturn(content);
        } catch (IOException | IllegalStateException ex) {
            throw new RuntimeException(ex);
        }
        
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(entity);
        
        HttpClient client = mock(HttpClient.class);
        try {
            when(client.execute(any(HttpUriRequest.class))).thenReturn(response);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        return client;
    }
    
}
