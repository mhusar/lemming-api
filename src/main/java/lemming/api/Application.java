package lemming.api;

import lemming.api.lemma.LemmaResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationPath("/")
public class Application extends ResourceConfig {

    public Application() {
        Logger logger = Logger.getLogger(Application.class.getName());

        // configure lemma resource logging
        ResourceConfig lemmaResourceConfig = new ResourceConfig(LemmaResource.class);
        lemmaResourceConfig.register(new LoggingFeature(logger, Level.ALL, LoggingFeature.Verbosity.PAYLOAD_ANY,
               LoggingFeature.DEFAULT_MAX_ENTITY_SIZE));

        packages("lemming.api.lemma");
        register(JacksonFeature.class);
    }
}
