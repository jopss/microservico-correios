package br.com.jopss.microservico.correios.infra;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Request;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

        private static ObjectMapper mapper;

        public JsonTransformer() {
                mapper = new ObjectMapper();
        }

        @Override
        public String render(Object model) {
                try {
                        return mapper.writeValueAsString(model);
                } catch (IOException ex) {
                        throw new RuntimeException(ex);
                }
        }

        public static <T> T fromBoby(Request req) {
                try {
                        @SuppressWarnings("unchecked")
                        T readValue = (T) mapper.readValue(req.body(), Modelos.class);
                        return readValue;
                } catch (IOException ex) {
                        throw new RuntimeException(ex);
                }
        }

        public static <T> T fromBoby(Request req, Class clazz) {
                try {
                        @SuppressWarnings("unchecked")
                        T readValue = (T) mapper.readValue(req.body(), clazz);
                        return readValue;
                } catch (IOException ex) {
                        throw new RuntimeException(ex);
                }
        }
        
        public static <T> T fromParam(Request req, String dados, Class clazz) {
                try {
                        @SuppressWarnings("unchecked")
                        T readValue = (T) mapper.readValue(req.raw().getParameter(dados), clazz);
                        return readValue;
                } catch (IOException ex) {
                        throw new RuntimeException(ex);
                }
        }
}
