package br.com.jopss.microservico.correios.infra;

import br.com.jopss.microservico.correios.dominio.exception.CorreiosException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.transaction.TransactionSystemException;
import spark.Response;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Resposta implements Serializable {

	private static final int HTTP_STATUS_ERROR = 500;
	private static final int HTTP_STATUS_LOGIN = 401;
	private static final int HTTP_STATUS_VALIDATION = 403;
	private static final int HTTP_STATUS_SUCCESS = 200;

	private List lista;
	private List<Retorno> mensagens;

	public void setModelo(Serializable modelo, Response resp) {
		this.setModelo(modelo, resp, "sucesso");
	}

	public void setModelo(Serializable modelo, Response resp, String msg) {
                if(modelo!=null){
                        this.getLista().add(modelo);
                }
		resp.status(HTTP_STATUS_SUCCESS);
		if (getMensagens().isEmpty()) {
			getMensagens().add(new Retorno("mensagem", msg));
		}
	}

	public void setLista(List lista, Response resp) {
		this.setLista(lista, resp, "sucesso");
	}

	public void setLista(List lista, Response resp, String msg) {
		this.lista = lista;
		resp.status(HTTP_STATUS_SUCCESS);
		if (getMensagens().isEmpty()) {
			getMensagens().add(new Retorno("mensagem", msg));
		}
	}

	public void addSucesso(String msg, Response resp) {
		getMensagens().add(new Retorno("mensagem", msg));
		resp.status(HTTP_STATUS_SUCCESS);
	}

	/**
	 * Adiciona qualquer mensagem de erro, alterando o Status HTTP relativo. Pode tratar erros SQL nativo, como unique.
	 * 
	 * @param ex Exception
	 * @param resp Response
	 */
	public void addErro(Exception ex, Response resp) {
		resp.status(HTTP_STATUS_ERROR);
		if (ex instanceof TransactionSystemException) {
			addErrosConstraints((TransactionSystemException) ex, resp);
		}else{
                        getMensagens().add(new Retorno("mensagem", ex.getMessage()));
                        ex.printStackTrace();
                }
	}
        public void addErro(String msg, Response resp) {
		getMensagens().add(new Retorno("mensagem", msg));
		resp.status(HTTP_STATUS_ERROR);
	}
        public void addErroLogin(String msg, Response resp) {
		getMensagens().add(new Retorno("mensagem", msg));
		resp.status(HTTP_STATUS_LOGIN);
	}
        
	/**
	 * Adiciona qualquer mensagem de validacao gerada a partir do nao cumprimento de regras de negocio.
	 * 
	 * @param str String
	 * @param resp Response
	 */
	public void addValidacao(String msg, Response resp) {
		getMensagens().add(new Retorno("mensagem", msg));
		resp.status(HTTP_STATUS_VALIDATION);
	}
        public void addValidacao(CorreiosException e, Response resp) {
		addValidacao(e.getMessage(), resp);
	}

	private void addErrosConstraints(TransactionSystemException ex, Response resp) {
		if (ex.getRootCause() instanceof ConstraintViolationException) {
			ConstraintViolationException exContrain = (ConstraintViolationException) ex.getRootCause();
			Set<ConstraintViolation<?>> constraintViolations = exContrain.getConstraintViolations();
			if (constraintViolations != null) {
				for (ConstraintViolation cons : constraintViolations) {
					getMensagens().add(new Retorno(cons.getPropertyPath().toString(), cons.getMessage()));
				}
				resp.status(HTTP_STATUS_VALIDATION);
			}
		}
	}

	public List<Retorno> getMensagens() {
		if (mensagens == null) {
			mensagens = new ArrayList<>();
		}
		return mensagens;
	}

	public List<Serializable> getLista() {
                if(lista == null){
                        this.lista = new ArrayList<>();
                }
		return lista;
	}

	/**
	 * Indica campos ou chaves com seus respectivos valores sobre erros e validacoes.
	 */
	public static class Retorno implements Serializable {

		private String chave;
		private String valor;

		public Retorno(String chave, String valor) {
			this.chave = chave;
			this.valor = valor;
		}

		public String getChave() {
			return chave;
		}

		public String getValor() {
			return valor;
		}

	}
}
