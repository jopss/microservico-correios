package br.com.jopss.microservico.correios.infra;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class HttpConnectionUtil {

	/**
	 * Obtem o domínio da URL passada. Por exemplo, 'http://google.com/search?s=termo' retornará 'google.com'
	 * 
	 * @param url
	 * @return String com o domínio
	 * @throws MalformedURLException
	 */
	public static String getDomainFromUrl(String url) throws MalformedURLException {
		URL urla = new URL(url);
		return urla.getHost();
	}

	/**
	 * Obtem o domínio e protocolo da URL passada. Por exemplo, 'http://google.com/search?s=termo' retornará 'http://google.com'
	 * 
	 * @param url
	 * @return String com o domínio e protocolo
	 * @throws MalformedURLException
	 */
	public static String getDomainWithProtocolFromUrl(String url) throws MalformedURLException {
		URL urla = new URL(url);
		return urla.getProtocol() + "://" + urla.getHost();
	}

	/**
	 * Obtem o valor da query da URL e parametro passados Por exemplo, 'http://google.com/search?s=termo' e parâmetro 's' retornará 'termo'
	 * 
	 * @param url
	 * @param parameter
	 * @return String com o valor da query
	 * @throws MalformedURLException
	 */
	public static String getQueryFromUrl(String url, String parameter) throws MalformedURLException {
		String fullQuery = new URL(url).getQuery();
		String[] queries = fullQuery.split("&");
		for (String query : queries) {
			String[] parts = query.split("=");
			if (parts[0].equals(parameter)) {
				return parts[1];
			}
		}
		return null;
	}

	/**
	 * Insere a URL passada os parâmetros informados conforme espera-se de uma URL para requisições do tipo GET, utilizando as queries separadas por '&'.
	 * 
	 * A URL original pode já conter parâmetros; os novos parâmetros serão concatenados.
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String generateGetUrlWithParams(String url, Map<String, String> params) {
		StringBuilder urlWithParams = new StringBuilder(url);

		if (params != null) {
			for (String key : params.keySet()) {
				if (urlWithParams.lastIndexOf("?") > 0) {
					urlWithParams.append("&");
				} else {
					urlWithParams.append("?");
				}
				urlWithParams.append(key);
				urlWithParams.append("=");
				urlWithParams.append(params.get(key));
			}
		}

		return urlWithParams.toString();
	}

	/**
	 * Obtem o cookie de sessão Java (JSESSIONID) da coleção de cookies passada.
	 * 
	 * @param cookies
	 * @return <code>null</code> se não encontrado
	 */
	public static String getJavaSessionIDInCookie(Map<String, String> cookies) {
		return cookies.get("JSESSIONID");
	}
}
