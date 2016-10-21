package br.com.jopss.microservico.correios.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum que indica as versoes possiveis sobre regras de negocio.
 * Referenciado no interceptor para validacao da requisicao, e na descoberta da classe correta sobre a versao passada.
 */
public enum VersaoWS {

	v1_0("1.0");

	private final String numero;

	private VersaoWS(String numero) {
		this.numero = numero;
	}

	public static List<String> getVersoes() {
		List<String> versoes = new ArrayList<>();
		for(VersaoWS versao : values()){
			versoes.add(versao.numero);
		}
		return versoes;
	}

	public static VersaoWS transform(String numero) {
		for (VersaoWS versaoWS : values()) {
			if (versaoWS.numero.equalsIgnoreCase(numero)) {
				return versaoWS;
			}
		}
		throw new IllegalArgumentException("Numero de VersaoWS inexistente: " + numero);
	}

	public String getNumero() {
		return numero;
	}

}