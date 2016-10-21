package br.com.jopss.microservico.correios;

import br.com.jopss.microservico.correios.dominio.exception.CorreiosException;
import br.com.jopss.microservico.correios.endpoint.AppEndpoint;
import br.com.jopss.microservico.correios.endpoint.CorreiosEndpoint;
import br.com.jopss.microservico.correios.infra.JsonTransformer;
import br.com.jopss.microservico.correios.infra.Resposta;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

public class Initializer {

        public void init() {
                configureWebPages();
                configureSpring();
                configureException();
                configureHeaders();
                configureEndpoints();
        }

        private void configureEndpoints() {
                new AppEndpoint().publish();
                new CorreiosEndpoint().publish();
        }

        private void configureWebPages() {
                staticFileLocation("/webapp");
        }
        
        /**
         * Coloca o contexto do Spring na memoria...
         */
        private void configureSpring() {
                new ClassPathXmlApplicationContext("classpath:appContext.xml");
        }

        private void configureException() {
                exception(Exception.class, (e, req, resp) -> {
                        Resposta ret = new Resposta();
                        if (e == null) {
                                ret.addErro("NullPointerException interno...", resp);
                        } else if (e instanceof CorreiosException) {
                                ret.addValidacao((CorreiosException) e, resp);
                        } else {
                                ret.addErro(e, resp);
                        }

                        resp.body(new JsonTransformer().render(ret));
                });
        }

        private void configureHeaders() {
                before((Request request, Response response) -> {
                        response.header("Access-Control-Allow-Origin", "*");
                        response.header("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
                        response.header("Access-Control-Allow-Credentials", "true");
                        response.header("Access-Control-Max-Age", "3600");
                        response.header("Access-Control-Allow-Headers", "api, versao, Content-Type, Access-Control-Allow-Headers, Access-Control-Request-Method, Authorization, X-Requested-With, Accept-Encoding");
                        response.header("Content-Type", "application/json");
                });
                
                after((request, response) -> {
                        response.header("Content-Encoding", "gzip");
                });
        }
        
}
