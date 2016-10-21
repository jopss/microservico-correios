package br.com.jopss.microservico.correios.dominio.repositorio;

import br.com.jopss.microservico.correios.beans.Bairro;
import br.com.jopss.microservico.correios.beans.Cidade;
import org.springframework.data.repository.CrudRepository;

public interface BairroRepository extends CrudRepository<Bairro, Long> {
	public Bairro findByNomeAndCidade(String nome, Cidade localidade);
}
