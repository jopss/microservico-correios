package br.com.jopss.microservico.correios.beans;

import br.com.jopss.microservico.correios.infra.Modelos;
import com.github.gilbertotorrezan.viacep.shared.ViaCEPEndereco;
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
public class Logradouro extends Modelos {

	private static final long serialVersionUID = 8765060059417187982L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "logradouroGenerator")
	@TableGenerator(name = "logradouroGenerator", allocationSize = 100)
	private Long id;

	@ManyToOne
	private Bairro bairro;

	@ManyToOne
	private Cidade cidade;

	@NotNull
	@NotEmpty
	private String nome;

	@NotNull
	private Integer cep;

	private String complemento;

	public Logradouro() {
	}

	public Logradouro(Integer cep) {
		this.cep = cep;
	}

	public Logradouro criar(Cidade cidade, Bairro bairro, ViaCEPEndereco viacep) {
		if (viacep != null) {
			this.cidade = cidade;
			if (this.cidade!=null) {
				this.cep = Integer.parseInt(viacep.getCep());
				this.nome = viacep.getLogradouro();
				this.complemento = viacep.getComplemento();
				this.bairro = bairro;
				return this;
			}
		}
		return null;
	}

	@Override
	public Long getId() {
		return id;
	}

	public Bairro getBairro() {
		return bairro;
	}

        public Cidade getCidade() {
                return cidade;
        }

	public String getNome() {
		return nome;
	}

	public Integer getCep() {
		return cep;
	}

	public String getComplemento() {
		return complemento;
	}

}
