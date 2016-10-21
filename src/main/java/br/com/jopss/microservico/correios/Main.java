package br.com.jopss.microservico.correios;

import static spark.Spark.*;

public class Main {

        public synchronized static void main(String[] args) {
                port(4567);
                new Initializer().init();
        }

}
