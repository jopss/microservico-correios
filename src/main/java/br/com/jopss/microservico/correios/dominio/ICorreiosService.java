package br.com.jopss.microservico.correios.dominio;

import br.com.jopss.microservico.correios.beans.Cidade;
import br.com.jopss.microservico.correios.beans.Logradouro;
import br.com.jopss.microservico.correios.beans.UF;
import br.com.jopss.microservico.correios.dominio.exception.CorreiosException;
import br.com.jopss.microservico.correios.util.IRestServico;
import java.util.List;

public interface ICorreiosService extends IRestServico {
        
        public Logradouro buscarLogradouroPorCEP(Integer cep, boolean buscarRemoto) throws CorreiosException;
        public Cidade buscarCidadePorCEP(Integer cep, boolean buscarRemoto) throws CorreiosException;
        public Cidade buscarCidadePorId(Long id) throws CorreiosException;
        public Cidade buscarCidadePorIbge(Long ibge) throws CorreiosException;
        public List<Cidade> buscarCidadesPorUF(String uf) throws CorreiosException;
        public List<UF> buscarUfs() throws CorreiosException;
        
}
