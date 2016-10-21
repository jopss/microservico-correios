package br.com.jopss.microservico.correios.dominio.repositorio;

import br.com.jopss.microservico.correios.beans.Cidade;
import br.com.jopss.microservico.correios.beans.UF;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CidadeRepository extends CrudRepository<Cidade, Long> {

	public List<Cidade> findByUfOrderByNome(UF uf);

	public Cidade findByIbge(Long ibge);

}
