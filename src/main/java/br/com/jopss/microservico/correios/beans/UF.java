package br.com.jopss.microservico.correios.beans;

import br.com.jopss.microservico.correios.infra.Modelos;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UF extends Modelos {

	private static final long serialVersionUID = 8765060059417187982L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ufGenerator")
	@TableGenerator(name = "ufGenerator", allocationSize = 100)
	private Long id;

	@NotNull
	@NotEmpty
	private String sigla;

	@NotNull
	@NotEmpty
	private String nome;

	public UF() {
	}

	public UF(String sigla) {
		this.sigla = sigla;
	}

	public UF(Long id, String sigla, String nome) {
		this.id = id;
		this.sigla = sigla;
		this.nome = nome;
	}

        @Override
	public Long getId() {
		return id;
	}

	public String getSigla() {
		return sigla;
	}

	public String getNome() {
		return nome;
	}

}
