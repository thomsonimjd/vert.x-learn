package httpclientpool;

import io.vertx.core.VertxException;
import io.vertx.core.http.ConnectionPoolTooBusyException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class HttpClientPoolTest extends VertxNubesClientTestBase {

    protected final static String HC_RESPONCSE_STRING = "Server is running";

    private static final Logger logger = LoggerFactory.getLogger(HttpClientPoolTest.class);

    protected HttpRequest hcRequest= HttpRequest.request().withPath("/hc").withMethod("GET");


    protected HttpResponse hcResponse = HttpResponse.response().withBody(HC_RESPONCSE_STRING).withStatusCode(200);

    private HttpClient getClient(){
        int idleTimeoutSecs = 2;  // TimeUnit.SECONDS
        int connectTimeoutMillis = 2000; // TimeUnit.MILLISECONDS

        return vertx.createHttpClient(new HttpClientOptions().setDefaultHost("localhost")
                .setDefaultPort(mockServerRule.getPort())
                .setIdleTimeout(idleTimeoutSecs)
                .setMaxPoolSize(2)
                .setMaxWaitQueueSize(0)
                .setConnectTimeout(connectTimeoutMillis)
        );
    }

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient mockServer;

    // test with delay of 2s, 10s or any sleep will get error
    // test with no delay will get successful response

    @Test
    public void testNoDelay(TestContext context) throws Exception {


        // setting behaviour for test case
        mockServer.when(hcRequest).respond(hcResponse);

        AtomicInteger count = new AtomicInteger();
        for (int i = 1; i <= 4; i++) {
            final int t = i;
            try {
                Async async = context.async();
                getClient().get("/hc", response -> {
                    System.out.println("RESPONSE : " + response);
                    async.complete();

                    if (count.incrementAndGet() >= 4) {
                        mockServer.verify(hcRequest, VerificationTimes.exactly(4));
                    }
                }).exceptionHandler(ex -> {
                    logger.info("EXCEPTION_"+t +" --->"+ex.getLocalizedMessage() + " == "+ex.getClass());
                    if (t < 3) {

                        context.assertEquals(VertxException.class, ex.getClass());
                        context.assertEquals("Connection was closed", ex.getMessage());
                    } else {
                        context.assertEquals(ConnectionPoolTooBusyException.class, ex.getClass());
                    }
                }).end();

            } catch (Throwable ex) {
                System.out.println("GOT_AN_EXCEPTION");
                ex.printStackTrace();
            }

            // Thread.sleep(1000);
        }
       //  mockServer.verify(hcRequest, VerificationTimes.exactly(4));


    }


    //Case two
    @Test
    public void withDelayTest(TestContext context) throws Exception {

        HttpResponse hcDelayResponse = HttpResponse.response().withBody(HC_RESPONCSE_STRING)
                .withStatusCode(200)
                .withDelay(new Delay(TimeUnit.SECONDS, 3));
        // setting behaviour for test case
        mockServer.when(hcRequest).respond(hcDelayResponse);

        AtomicInteger count = new AtomicInteger();
        for (int i = 1; i <= 4; i++) {
            final int t = i;
            try {
                Async async = context.async();
                getClient().get("/hc", response -> {
                    System.out.println("RESPONSE : " + response);
                }).exceptionHandler(ex -> {
                    logger.info("EXCEPTION_"+t +" --->"+ex.getLocalizedMessage() + " == "+ex.getClass());
                    if (t < 3) {

                        context.assertEquals(VertxException.class, ex.getClass());
                        context.assertEquals("Connection was closed", ex.getMessage());
                    } else {
                        context.assertEquals(ConnectionPoolTooBusyException.class, ex.getClass());
                    }
                    async.complete();
                    if (count.incrementAndGet() >= 4) {
                        mockServer.verify(hcRequest, VerificationTimes.exactly(4));
                    }
                }).end();

                if (i == 2) {
                    Thread.sleep(2000);
                }

            } catch (Throwable ex) {
                System.out.println("GOT_AN_EXCEPTION");
                ex.printStackTrace();
            }
        }
    }

}