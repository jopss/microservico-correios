package br.com.jopss.microservico.correios.endpoint;

import br.com.jopss.microservico.correios.infra.FormatadorUtil;
import br.com.jopss.microservico.correios.infra.CorreiosEndpoints;
import br.com.jopss.microservico.correios.infra.JsonTransformer;
import static spark.Spark.*;
import br.com.jopss.microservico.correios.dominio.ICorreiosService;

public class CorreiosEndpoint extends CorreiosEndpoints {

        @Override
        public void publish() {

                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/logradouro/cep/:numeroCep GET");
                get("/:versao/correios/logradouro/cep/:numeroCep", "text/plain", (req, resp) -> {
                        Integer numeroCep = Integer.parseInt(req.params("numeroCep"));
                        
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarLogradouroPorCEP(numeroCep, true), resp);
                }, new JsonTransformer());

                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/cidade/cep/:numeroCep GET");
                get("/:versao/correios/cidade/cep/:numeroCep", "text/plain", (req, resp) -> {
                        Integer numeroCep = Integer.parseInt(req.params("numeroCep"));
                        
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarCidadePorCEP(numeroCep, true), resp);
                }, new JsonTransformer());
                
                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/cidade/id/:id GET");
                get("/:versao/correios/cidade/id/:id", "text/plain", (req, resp) -> {
                        Long id = Long.parseLong(req.params("id"));
                        
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarCidadePorId(id), resp);
                }, new JsonTransformer());
                
                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/cidade/ibge/:ibge GET");
                get("/:versao/correios/cidade/ibge/:ibge", "text/plain", (req, resp) -> {
                        Long ibge = Long.parseLong(req.params("ibge"));
                        
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarCidadePorIbge(ibge), resp);
                }, new JsonTransformer());
                
                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/cidades/:uf GET");
                get("/:versao/correios/cidades/uf/:uf", "text/plain", (req, resp) -> {
                        String uf = req.params("uf");
                        
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarCidadesPorUF(uf), resp);
                }, new JsonTransformer());
                
                FormatadorUtil.addLogInfo(" --> Configurando path 'CorreiosEndpoint' /correios/ufs GET");
                get("/:versao/correios/ufs", "text/plain", (req, resp) -> {
                        ICorreiosService servico = super.getServico(req, ICorreiosService.class);
                        return super.retornar(servico.buscarUfs(), resp);
                }, new JsonTransformer());

        }
}

