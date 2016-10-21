package br.com.jopss.microservico.correios.infra;

import br.com.jopss.microservico.correios.util.IRestServico;
import br.com.jopss.microservico.correios.util.VersaoWS;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import spark.Request;
import spark.Response;

public abstract class CorreiosEndpoints {
        
        public abstract void publish();
        
	private VersaoWS retornarVersaoRequisicao(String versao) {
		return VersaoWS.transform(versao);
	}
        
        public <T>T getServico(Request req, Class clazz){
                Map<String, Object> filters = AppContextUtil.getApplicationContext().getBeansOfType(clazz);
		for (Object servico : filters.values()) {
                        if(servico instanceof IRestServico){
                                IRestServico irs = (IRestServico) servico;
                                if (irs.getVersao().equals(this.retornarVersaoRequisicao(req.params("versao")))) {
                                        return (T) servico;
                                }
                        }
		}
                return (T) filters.values().iterator().next();
        }
        
        protected void download(Response resp, byte[] bytes, String nomeArquivo) throws IOException{
                this.download(resp, bytes, "application/octet-stream", nomeArquivo);
        }
        
        protected void download(Response resp, byte[] bytes, String contentType, String nomeArquivo) throws IOException{
                resp.raw().setContentType(contentType);
                resp.raw().setHeader("Content-Disposition","attachment; filename="+nomeArquivo);
                resp.raw().setHeader("Pragma", "No-cache");
                resp.raw().setHeader("Cache-Control", "no-cache");
                resp.raw().setCharacterEncoding("UTF-8");
                resp.raw().setContentType("UTF-8");
                resp.raw().setContentLength(bytes.length);

                ServletOutputStream out = resp.raw().getOutputStream();
                out.write(bytes, 0, bytes.length);
        }
        
        protected Resposta retornar(Serializable m, Response resp){
                Resposta resposta = new Resposta();
                resposta.setModelo(m, resp);
                return resposta;
        }
        
        protected Resposta retornar(Response resp){
                Resposta resposta = new Resposta();
                resposta.setModelo(null, resp);
                return resposta;
        }
        
        protected Resposta retornar(List lista, Response resp){
                Resposta resposta = new Resposta();
                resposta.setLista(lista, resp);
                return resposta;
        }
}
