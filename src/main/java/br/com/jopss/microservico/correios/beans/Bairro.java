package br.com.jopss.microservico.correios.beans;

import br.com.jopss.microservico.correios.infra.Modelos;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Bairro extends Modelos {

	private static final long serialVersionUID = 8765060059417187982L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "bairroGenerator")
	@TableGenerator(name = "bairroGenerator", allocationSize = 100)
	private Long id;

	@NotNull
	@NotEmpty
	private String nome;

	@ManyToOne
	private Cidade cidade;

	public Bairro() {
	}

	public Bairro(String nome, Cidade cidade) {
		this.nome = nome;
		this.cidade = cidade;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

        public Cidade getCidade() {
                return cidade;
        }

}
