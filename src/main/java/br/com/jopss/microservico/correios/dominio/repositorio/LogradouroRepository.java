package br.com.jopss.microservico.correios.dominio.repositorio;

import br.com.jopss.microservico.correios.beans.Logradouro;
import org.springframework.data.repository.CrudRepository;

public interface LogradouroRepository extends CrudRepository<Logradouro, Long> {

	Logradouro findByCep(Integer cep);

}
