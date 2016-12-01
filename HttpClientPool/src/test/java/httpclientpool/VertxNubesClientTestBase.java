package httpclientpool;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public abstract class VertxNubesClientTestBase {



    protected static JsonObject config = new JsonObject();

    protected static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        if (vertx==null){
            vertx = Vertx.vertx();
            DeploymentOptions options = new DeploymentOptions();
            config.put("host","localhost");
            config.put("port",9002);
            options.setConfig(config);
            vertx.deployVerticle(NubesClientVerticle.class.getName(),options,
                    context.asyncAssertSuccess());
        }
    }
}