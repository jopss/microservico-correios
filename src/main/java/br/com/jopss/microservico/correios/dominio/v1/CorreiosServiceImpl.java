package br.com.jopss.microservico.correios.dominio.v1;

import br.com.jopss.microservico.correios.beans.Bairro;
import br.com.jopss.microservico.correios.beans.Cidade;
import br.com.jopss.microservico.correios.beans.Logradouro;
import br.com.jopss.microservico.correios.beans.UF;
import br.com.jopss.microservico.correios.dominio.exception.CorreiosException;
import br.com.jopss.microservico.correios.dominio.repositorio.BairroRepository;
import br.com.jopss.microservico.correios.dominio.repositorio.CidadeRepository;
import br.com.jopss.microservico.correios.dominio.repositorio.LogradouroRepository;
import br.com.jopss.microservico.correios.dominio.repositorio.UFRepository;
import br.com.jopss.microservico.correios.infra.HttpUtil;
import br.com.jopss.microservico.correios.dominio.ICorreiosService;
import br.com.jopss.microservico.correios.util.VersaoWS;
import com.github.gilbertotorrezan.viacep.shared.ViaCEPEndereco;
import java.util.List;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorreiosServiceImpl implements ICorreiosService {

        private Logger logger = Logger.getLogger(CorreiosServiceImpl.class.getName());

        @Autowired
        private LogradouroRepository logradouroRepository;
        
        @Autowired
        private CidadeRepository cidadeRepository;

        @Autowired
        private BairroRepository bairroRepository;
        
        @Autowired
        private UFRepository ufRepository;

        @Override
        public VersaoWS getVersao() {
                return VersaoWS.v1_0;
        }

        private ViaCEPEndereco buscarCEPWSViaCep(String numeroCEP) {
                try {
                        ViaCEPEndereco endereco = new ViaCEPEndereco();

                        String url = "http://www.byjg.com.br/site/webservice.php/ws/cep?httpmethod=obterLogradouroAuth&cep=" + numeroCEP + "&usuario=usuario&senha=senha";
                        HttpUtil httpUtil = new HttpUtil(url, HttpUtil.RequestMethod.GET);
                        httpUtil.sendRequest();
                        String resposta = httpUtil.getContent();

                        if (resposta != null && !resposta.isEmpty()) {
                                String[] retorno = resposta.split(",");
                                endereco.setCep(numeroCEP);
                                endereco.setLogradouro(retorno[0].replace("OK|", "").trim());
                                endereco.setBairro(retorno[1].trim());
                                endereco.setLocalidade(retorno[2].trim());
                                endereco.setUf(retorno[3].trim());
                                endereco.setIbge(retorno[4].trim());
                        }

                        return endereco;

                } catch (Exception ex) {
                        addLog("EXCEPTION: : " + ex.getMessage());
                        return null;
                }
        }

        private void addLog(String log) {
                logger.info("--> " + log);
        }

        @Override
        public Logradouro buscarLogradouroPorCEP(Integer cep, boolean buscarRemoto) throws CorreiosException {
                Logradouro log = this.logradouroRepository.findByCep(cep);
                if (log == null) {
                        if (buscarRemoto) {
                                ViaCEPEndereco wsVia = this.buscarCEPWSViaCep(String.valueOf(cep));
                                if (wsVia == null) {
                                        if (wsVia == null) {
                                                throw new CorreiosException("CEP '" + cep + "' não encontrado.");
                                        }
                                } else {
                                        Cidade cidade = this.buscarCidadePorIbge(Long.parseLong(wsVia.getIbge()));
                                        Bairro bairro = this.bairroRepository.findByNomeAndCidade(wsVia.getBairro(), cidade);
                                        log = new Logradouro().criar(cidade, bairro, wsVia);
                                        log = this.logradouroRepository.save(log);
                                }
                        } else {
                                throw new CorreiosException("CEP '" + cep + "' não encontrado.");
                        }
                }
                return log;
        }

        @Override
        public Cidade buscarCidadePorCEP(Integer cep, boolean buscarRemoto) throws CorreiosException {
                return buscarLogradouroPorCEP(cep, buscarRemoto).getCidade();
        }

        @Override
        public Cidade buscarCidadePorId(Long id) throws CorreiosException {
                return this.cidadeRepository.findOne(id);
        }

        @Override
        public Cidade buscarCidadePorIbge(Long ibge) throws CorreiosException {
                return this.cidadeRepository.findByIbge(ibge);
        }

        @Override
        public List<Cidade> buscarCidadesPorUF(String sigla) throws CorreiosException {
                UF uf = this.ufRepository.findBySiglaOrderByNome(sigla);
                return this.cidadeRepository.findByUfOrderByNome(uf);
        }

        @Override
        public List<UF> buscarUfs() throws CorreiosException {
                return IteratorUtils.toList(this.ufRepository.findAll().iterator());
        }

}
