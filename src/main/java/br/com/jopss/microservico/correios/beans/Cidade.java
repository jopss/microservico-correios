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
public class Cidade extends Modelos {

	private static final long serialVersionUID = 8765060059417187982L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "cidadeGenerator")
	@TableGenerator(name = "cidadeGenerator", allocationSize = 100)
	private Long id;

	@NotNull
	@NotEmpty
	private String nome;

	@ManyToOne
	private UF uf;

	private Long ibge;

	public Cidade() {
	}

	public Cidade(Long id) {
		this.id = id;
	}

	public Cidade(UF uf) {
		this.uf = uf;
	}

	public Cidade(UF uf, Long ibge) {
		this.uf = uf;
		this.ibge = ibge;
	}

	public Cidade(String nome, UF uf, Long ibge) {
		this.nome = nome;
		this.uf = uf;
		this.ibge = ibge;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public UF getUf() {
		return uf;
	}

	public Long getIbge() {
		return ibge;
	}

}
