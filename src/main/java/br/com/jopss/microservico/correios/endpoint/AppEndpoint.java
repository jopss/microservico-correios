package br.com.jopss.microservico.correios.endpoint;

import br.com.jopss.microservico.correios.infra.FormatadorUtil;
import br.com.jopss.microservico.correios.infra.CorreiosEndpoints;
import static spark.Spark.*;

public class AppEndpoint extends CorreiosEndpoints {

        @Override
        public void publish() {
                before((request, response) -> {
                        if( request.raw().getMethod().equalsIgnoreCase("OPTIONS") ){
                                return;
                        }
                });

                FormatadorUtil.addLogInfo(" --> Configurando path 'AppEndpoint' /* OPTIONS (PARA CORS!)");
                options("/*", (req, resp) -> "OPTIONS OK");

                FormatadorUtil.addLogInfo(" --> Configurando path 'AppEndpoint' /stop/ GET");
                get("/stop/", "text/plain", (req, resp) -> {
                        stop();
                        return null;
                });

        }

}
