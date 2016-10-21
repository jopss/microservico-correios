package br.com.jopss.microservico.correios.dominio.repositorio;

import br.com.jopss.microservico.correios.beans.UF;
import org.springframework.data.repository.CrudRepository;

public interface UFRepository extends CrudRepository<UF, Long> {

	UF findBySiglaOrderByNome(String sigla);
}
