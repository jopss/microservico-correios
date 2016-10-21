package br.com.jopss.microservico.correios.util;

/**
 * Interface pai de todas as INTERFACES de servicos. Restringe a todos os filhos terem um método para retorno da versao.
 */
public interface IRestServico {
	public VersaoWS getVersao();
}
