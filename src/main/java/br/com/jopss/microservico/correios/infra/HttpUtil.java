package br.com.jopss.microservico.correios.infra;

import br.com.jopss.microservico.correios.dominio.exception.CorreiosException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Classe para realização de acessos HTTP/HTTPS permitindo configurações diversas.
 * 
 * Uma instância dessa classe pode ser configurada de forma sequencial antes de efetivamente realizar a conexão. E depois de fazer a conexão (de forma síncrona) permitirá o acesso às informações
 * obtidas também sequencialmente.
 */
@NotThreadSafe
public class HttpUtil {

	private static final Logger logger = Logger.getLogger(HttpUtil.class);

	/**
	 * ATRIBUTOS DA REQUEST
	 */
	private final String url;
	private final RequestMethod method;
	private Map<String, String> params;
	private Map<String, String> paramsReqPost;
	private Map<String, String> cookies;
	private HttpEntity entityParam;
	private Map<String, String> headers;

	private ProxyType proxyType = ProxyType.NONE;
	private String proxyHost;
	private String proxyPort;
	private String proxyUser;
	private String proxyPass;
	private boolean useProxyFromSystem;

	private final static Integer requestTimeout = 30;

	private final HttpClientContext context = HttpClientContext.create();
	private FollowRedirectStrategy followRedirectStrategy = FollowRedirectStrategy.SINGLE_CONNECTION;

	/**
	 * ATRIBUTOS DA RESPOSTA
	 */
	private Integer statusCode;
	private String statusReason;
	private String content;
	private String mimeType;
	private Charset charSet;
	private ConnectionStatus status = ConnectionStatus.NEW;
	private HttpUtil followingConnection;
	private String urlRedirect;
	private StopWatch timer = new StopWatch();

	/**
	 * ATRIBUTOS DE CONFIGURAÇÃO
	 */
	private final static PoolingHttpClientConnectionManager connManagerWithPool;
	private final static PoolingHttpClientConnectionManager connManagerWithoutPool;

	static {
		CustomHttpConnectionSocketFactory httpConnectionFactory = new CustomHttpConnectionSocketFactory();
		CustomHttpsConnectionSocketFactory httpsConnectionFactory = new CustomHttpsConnectionSocketFactory();

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("https", httpsConnectionFactory).register("http", httpConnectionFactory)
				.build();

		connManagerWithPool = new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, null, requestTimeout, TimeUnit.SECONDS);
		connManagerWithoutPool = new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, null, -1, TimeUnit.MILLISECONDS);
	}

	// ===========================
	// MÉTODOS PARA SETAR PARAMETROS DA REQUEST
	// ===========================
	public HttpUtil(String url, RequestMethod method) {
		this.url = url.replaceAll(" ", "%20").replaceAll("<", "%3C").replaceAll(">", "%3E");
		this.method = method;
	}

	private HttpUtil(String redirectUrl, HttpUtil previousConnection) {
		this(redirectUrl, RequestMethod.GET);

		// Não seguirá novos redirects... Correto?
		this.followRedirectStrategy = FollowRedirectStrategy.DISABLED;

		this.cookies = previousConnection.cookies;
		this.headers = previousConnection.headers;

		this.proxyHost = previousConnection.proxyHost;
		this.proxyPort = previousConnection.proxyPort;
		this.proxyUser = previousConnection.proxyUser;
		this.proxyPass = previousConnection.proxyPass;
		this.proxyType = previousConnection.proxyType;
		this.useProxyFromSystem = previousConnection.useProxyFromSystem;
	}

	/**
	 * Informa os parâmetros a serem inclusos na requisicao. Sendo passados no formato &lt;chave, valor&gt;, serão usados para criar a query da URL (Ex. ?param=value) no caso de requisicao GET, ou
	 * para criar o {@link HttpEntity} que representará um &lt;form&gt; do POST.
	 * 
	 * @param params
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withParams(Map<String, String> params) {
		this.params = params;
		return this;
	}

	/**
	 * Informa o corpo do POST a ser usado na requisicao. Caso sejam informados parâmetros por &lt;chave, valor&gt;, este {@link HttpEntity} sera desprezado.
	 * 
	 * @param entity
	 * @return a propria instancia HttpConnection
	 * @see HttpConnection#withParams(java.util.Map)
	 */
	public HttpUtil withPostParam(HttpEntity entity) {
		this.entityParam = entity;
		return this;
	}

	/**
	 * Informa o corpo do POST a ser usado na requisicao. Caso sejam informados parâmetros por &lt;chave, valor&gt;, este {@link Serializable} sera desprezado.
	 * 
	 * @param serializableObj
	 * @return a propria instancia HttpConnection
	 * @throws IOException
	 * @see HttpConnection#withParams(java.util.Map)
	 */
	public HttpUtil withPostParam(Serializable serializableObj) throws IOException {
		this.entityParam = new SerializableEntity(serializableObj);
		return this;
	}

	/**
	 * Informa os parâmetros a serem inclusos na requisicao de um POST. Sendo passados no formato &lt;chave, valor&gt;, serão usados para criar a query da URL (Ex. ?param=value).
	 * 
	 * @param paramsReqPost
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withPostURLParam(Map<String, String> paramsReqPost) {
		this.paramsReqPost = paramsReqPost;
		return this;
	}

	/**
	 * Informa um {@link Map} de cookies a ser utilizado na requisicao. Esse Map será atualizado com os cookies setados pela requisicao. Como a referencia nao e alterada, os novos cookies poderao ser
	 * consultados pela referencia original.
	 * 
	 * @param cookies
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withCookies(Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * Inicializa o {@link Map} de cookies internamente a fim de manter as atualizacoes geradas pela requisicao. Depois de completada a requisicao, os cookies gerados poderao ser obtidos.
	 * 
	 * @return a propria instancia HttpConnection
	 * @see HttpConnection#getCookies()
	 */
	public HttpUtil withCookies() {
		this.cookies = new HashMap<>();
		return this;
	}

	/**
	 * Configura as informacoes necessarias para acesso via proxy sem autenticacao. Caso tenha que ser utilizado um Proxy HTTP com autenticacao, use o metodo
	 * {@link HttpConnection#withHttpProxy(java.lang.String, java.lang.String, java.lang.String, java.lang.String) }. <br/>
	 * <br/>
	 * Não há suporte para proxy SOCKS com autenticacao. <br/>
	 * <br/>
	 * <b>WARNING!</b> O uso de pool de conexoes para conexoes com proxy SOCKS e outras conexoes sem esse proxy poderá causar que todas as conexoes acabem usando o proxy SOCKS. Aparentemente a conexao
	 * aberta no pool mantem algumas configuracoes da ultima conexao realizada. Ou seja, se foi usado o proxy SOCKS, a proxima conexao a aproveitar o pool utilizara esse proxy independente da
	 * configuracao da instancia do HttpConnection. <br/>
	 * <br/>
	 * Sendo assim, se for usar proxy SOCKS, melhor não usar pool de conexões.
	 * 
	 * @param host
	 * @param port
	 * @param type
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withProxy(String host, String port, ProxyType type) {
		this.proxyHost = host;
		this.proxyPort = port;
		this.proxyType = type;
		return this;
	}

	/**
	 * Confugyra as informacoes necessarias para acesso via proxy HTTP com autenticacao. Para configurar um proxy sem autenticacao, use o metodo
	 * {@link HttpConnection#withProxy(java.lang.String, java.lang.String, com.ideebox.advprocess.core.ws.HttpConnection.ProxyType) }. <br/>
	 * <br/>
	 * Não há suporte para proxy SOCKS com autenticacao.
	 * 
	 * @param host
	 * @param port
	 * @param user
	 * @param pass
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withHttpProxy(String host, String port, String user, String pass) {
		this.proxyUser = user;
		this.proxyPass = pass;
		return withProxy(host, port, ProxyType.HTTP);
	}

	public HttpUtil withHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 * Seta o charset a ser utilizado na obtencao da resposta da requisicao. Caso nao seja informado, sera tentado obte-lo a partir da resposta da requisicao e, se nao for possivel determina-lo, sera
	 * considerado como default 'ISO-8859-1'. <br/>
	 * <br/>
	 * O charset utilizado para encodar a resposta podera ser obtido via {@link HttpConnection#getCharSet()
         * }.
	 * 
	 * @param charset
	 * @return a propria instancia HttpConnection
	 */
	public HttpUtil withCharset(Charset charset) {
		this.charSet = charset;
		return this;
	}

	/**
	 * Se <code>true</code> tentara obter os parametros de configuracao do proxy via propriedades do sistema, configurados na inicialização do container. <br/>
	 * <br/>
	 * Default: <code>false</code> <br/>
	 * <br/>
	 * Valores esperados:
	 * <ul>
	 * <li>conn.proxyHost</li>
	 * <li>conn.proxyPort</li>
	 * <li>conn.proxyUser</li>
	 * <li>conn.proxyPass</li>
	 * <li>conn.proxyType</li>
	 * </ul>
	 * <br/>
	 * Para o atributo <i>proxyType</i> espera-se 'HTTP' ou 'SOCKS', sendo 'NONE' o dafault. Caso seja informado 'SOCKS', <i>proxyUser</i> e <i>proxyPass</i> serao ignorados. <br/>
	 * <br/>
	 * As informacoes de proxy setadas especificamente para esta instancia terao preferencia sobre as configuracoes provindas do sistema.
	 * 
	 * @param useProxyFromSystem
	 * @return a propria instancia HttpConnection
	 * @see HttpConnection#withProxy(java.lang.String, java.lang.String, com.ideebox.advprocess.core.ws.HttpConnection.ProxyType)
	 * @see HttpConnection#withHttpProxy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public HttpUtil usingProxyFromSystem(boolean useProxyFromSystem) {
		this.useProxyFromSystem = useProxyFromSystem;
		return this;
	}

	/**
	 * Determina a estratégia para seguir os redirecionamentos.
	 * <ul>
	 * <li> {@link FollowRedirectStrategy#SINGLE_CONNECTION} (<b>Default</b>) Seguirá automaticamente os redirects detro do mesmo {@link HttpConnection} sem dar pistas destes através de código de
	 * status ou url. Ou seja, independente de quantos redirects forem feitos, se chegar a algum lugar, terá status code 200, a URL passada inicialmente e status da conexão
	 * {@link ConnectionStatus#SUCCESS}.</li>
	 * <li> {@link FollowRedirectStrategy#MULTIPLE_CONNECTION} Seguirá automaticamente os redirects criando novas {@link HttpConnection} para cada um. A conexão final terá um status code 200 (com a URL
	 * desse redirecionamento), mas as conexões anteriores terão status code 301 ou 302 além do status {@link ConnectionStatus#REDIRECT_SUCCESS}.
	 * <ul>
	 * <li>TODO: Atualmente para evitar loop infinito está fazendo apenas 1 redirect</li>
	 * </ul>
	 * </li>
	 * <li> {@link FollowRedirectStrategy#DISABLED} Desativa redirecionamentos e, caso exista um, retornar um {@link CorreiosException}, conexão com status code 301 ou 302 e status
	 * {@link ConnectionStatus#ERROR}</li>
	 * </ul>
	 * 
	 * @param strategy
	 * @return a propria instancia HttpUtil
	 * @see HttpUtil#getFollowingConnection()
	 */
	public HttpUtil withFollowRedirectStrategy(FollowRedirectStrategy strategy) {
		this.followRedirectStrategy = strategy;
		return this;
	}

	// ===========================
	// MÉTODOS PARA REALIZAR A REQUEST
	// ===========================
	public void sendRequest() throws CorreiosException {
		this.timer.start();
		this.status = ConnectionStatus.SENDING;
		try {
			completeRequest();
		} finally {
			this.timer.stop();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
		builder.append(" [").append(this.status.name()).append("]: ");
		if (this.url != null) {
			builder.append(this.url);
		}
		return builder.toString();
	}

	// ===========================
	// MÉTODOS PARA OBTER OS RESULTADOS
	// ===========================
	/**
	 * Obtem o {@link Map} (nome, valor) com os cookies participantes da requisicao. Se foram passados cookies para a requisicao, o Map devera ser o mesmo com pares de valores inclusos ou alterados
	 * pela requisicao. <br/>
	 * Se o Map de cookies nao foi passado ou inicializado, esse metodo retornara <code>null</code>
	 * 
	 * @return cookies atualizados pela requisicao
	 */
	public Map<String, String> getCookies() {
		return cookies;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public String getContent() {
		return content;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getUrlRedirect() {
		return urlRedirect;
	}

	/**
	 * Obtem o charset usado para encodar a resposta da requisicao. Se foi passado um {@link Charset} durante a configuracao desta instancia, esse mesmo charset foi forçadamente utilizado e será
	 * retornado aqui. <br/>
	 * Caso contrário, se nenhum charset foi passado, sera tentado obter o charset a partir da resposta da requisicao. Se nao for possivel, sera considerado como padrao 'ISO-8859-1'.
	 * 
	 * @return {@link Charset}
	 */
	public Charset getCharSet() {
		return charSet;
	}

	/**
	 * Retorna o status desta instancia de acordo com o ciclo de vida.
	 * <ul>
	 * <li>{@link ConnectionStatus#NEW} - Recem criada e sendo configurada</li>
	 * <li>{@link ConnectionStatus#SENDING} - Foi enviado o comando para conectar</li>
	 * <li>{@link ConnectionStatus#SUCCESS} - A requisicao completou com sucesso</li>
	 * <li>{@link ConnectionStatus#ERROR} - A requisicao completou com erro</li>
	 * <li>{@link ConnectionStatus#REDIRECT_ERROR} - O redirect falhou ou teve mais redirects</li>
	 * <li>{@link ConnectionStatus#REDIRECT_SUCCESS} - O redirect finalizou com sucesso</li>
	 * </ul>
	 * 
	 * @return {@link ConnectionStatus}
	 */
	public ConnectionStatus getStatus() {
		return status;
	}

	/**
	 * Retorna a conexão seguinte caso algum redirect tenha sido seguido. Só existira se foi usado o
	 * {@link HttpConnection#withFollowRedirectStrategy(com.ideebox.advprocess.core.ws.HttpConnection.FollowRedirectStrategy)
         * } para setar como {@link FollowRedirectStrategy#MULTIPLE_CONNECTION} e
	 * houve redirects.
	 * 
	 * @return retorna a conexão usada para seguir o redirecionamento. Ou <code>null</code> se não houve redirect.
	 */
	public HttpUtil getFollowingConnection() {
		return followingConnection;
	}

	/**
	 * Retorna o tempo de execução da requisição em milissegundos.
	 * 
	 * @return Tempo de execução
	 */
	public Long getExecutionTime() {
		return this.timer.getTime();
	}

	// ===========================
	// MÉTODOS PRIVADOS PARA A GERAÇÃO DA REQUISIÇÃO
	// ===========================
	private void completeRequest() throws CorreiosException {
		try {
			initializeProxyFromSystem();
			HttpRequestBase request = generateRequest();
			CloseableHttpClient client = generateHttpClient();
			BasicCookieStore cookieStore = configCookieStore();
			CloseableHttpResponse response = client.execute(request, this.context);

			if (cookieStore != null) {
				for (Cookie cookie : cookieStore.getCookies()) {
					this.cookies.put(cookie.getName(), cookie.getValue());
				}
			}

			try {
				this.statusCode = response.getStatusLine().getStatusCode();

				if (this.statusCode == 200) {
					consumeResponseContent(response);
				} else if ((this.statusCode == 301 || this.statusCode == 302) && FollowRedirectStrategy.MULTIPLE_CONNECTION.equals(this.followRedirectStrategy)) {
					sendFollowingConnection(response);
				} else {
					generateErrorMessage(response);
					throw new CorreiosException(response.getStatusLine().getStatusCode(), this.statusReason);
				}

			} finally {
				EntityUtils.consume(response.getEntity());
				response.close();
			}

		} catch (IOException io) {
			throw new CorreiosException(io);
		}
	}

	private void sendFollowingConnection(CloseableHttpResponse response) throws CorreiosException {
		Header locationHeader = response.getFirstHeader("Location");
		if (locationHeader == null) {
			throw new CorreiosException(this.statusCode, "Falha ao obter URL de redirect");
		}

		String url = "http://www2.correios.com.br/sistemas/rastreamento/" + locationHeader.getValue();

		// Obrigatoriamente GET
		HttpUtil redirectConn = new HttpUtil(url, this);
		this.followingConnection = redirectConn;
		this.urlRedirect = url;

		this.status = ConnectionStatus.REDIRECT_ERROR;
		redirectConn.sendRequest();
		this.status = ConnectionStatus.REDIRECT_SUCCESS;
		this.content = redirectConn.getContent();
		this.charSet = redirectConn.getCharSet();
	}

	private HttpRequestBase generateRequest() throws CorreiosException {
		HttpRequestBase request;

		if (RequestMethod.POST.equals(this.method)) {
			HttpPost httpPost = new HttpPost(this.generatePostUrlWithQueryParams());
			try {
				configPostEntityFromParams();
			} catch (UnsupportedEncodingException ex) {
				throw new CorreiosException(ex);
			}

			if (this.entityParam != null) {
				httpPost.setEntity(this.entityParam);
			}
			request = httpPost;
		} else {
			request = new HttpGet(this.generateGetUrlWithQueryParams());
		}

		request = configHeaders(request);
		request = configTimeoutAndHttpProxy(request);

		return request;
	}

	private HttpRequestBase configHeaders(HttpRequestBase request) {
		if (this.headers != null) {
			for (String key : this.headers.keySet()) {
				request.addHeader(key, headers.get(key));
			}
		}
		return request;
	}

	private void initializeProxyFromSystem() {
		if (this.useProxyFromSystem && this.proxyHost == null) {
			this.proxyHost = getProxyHostFromSystem();
			this.proxyPort = getProxyPortFromSystem();
			this.proxyUser = getProxyUserFromSystem();
			this.proxyPass = getProxyPassFromSystem();
			this.proxyType = getProxyTypeFromSystem();
			this.proxyType = this.proxyType == null ? ProxyType.NONE : this.proxyType;
		}
	}

	private HttpRequestBase configTimeoutAndHttpProxy(HttpRequestBase request) {

		RequestConfig.Builder requestConfig = RequestConfig.custom();
		requestConfig.setConnectTimeout(requestTimeout * 1000);
		requestConfig.setConnectionRequestTimeout(requestTimeout * 1000);
		requestConfig.setSocketTimeout(requestTimeout * 1000);

		if (ProxyType.HTTP.equals(this.proxyType)) {
			HttpHost proxy = new HttpHost(this.proxyHost, Integer.valueOf(this.proxyPort));
			requestConfig.setProxy(proxy);

		} else if (ProxyType.SOCKS.equals(this.proxyType)) {
			InetSocketAddress socksaddr = new InetSocketAddress(this.proxyHost, Integer.valueOf(this.proxyPort));
			context.setAttribute("socks.address", socksaddr);
		}

		request.setConfig(requestConfig.build());
		return request;
	}

	private HttpClientBuilder configHttpProxyAuth(HttpClientBuilder clientBuilder) {
		if (ProxyType.HTTP.equals(this.proxyType) && this.proxyUser != null && this.proxyPass != null) {
			HttpClientBuilder builder = clientBuilder == null ? HttpClients.custom() : clientBuilder;

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyHost, Integer.valueOf(proxyPort)), new UsernamePasswordCredentials(proxyUser, proxyPass));
			builder.setDefaultCredentialsProvider(credsProvider);
			return builder;
		}

		return clientBuilder;
	}

	private HttpClientBuilder configConnectionManager(HttpClientBuilder clientBuilder) {
		HttpClientBuilder builder = clientBuilder == null ? HttpClients.custom() : clientBuilder;
		builder.setConnectionManager(connManagerWithoutPool);
		builder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
		builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
		builder.useSystemProperties();

		return builder;
	}

	private CloseableHttpClient generateHttpClient() {

		HttpClientBuilder builder = configHttpProxyAuth(null);
		builder = configConnectionManager(builder);
		builder.disableRedirectHandling();

		return builder.build();
	}

	private BasicCookieStore configCookieStore() throws MalformedURLException {

		if (this.cookies == null) {
			return null;
		}

		BasicCookieStore cookieStore = new BasicCookieStore();
		String domain = HttpConnectionUtil.getDomainFromUrl(this.url);

		// sincronizado para o caso de serem cookies compartilhados, ou seja,
		// um Map singleton para um certo robô.
		final Map<String, String> staticCookies = this.cookies;
		synchronized (staticCookies) {
			for (String cookieName : staticCookies.keySet()) {
				BasicClientCookie cookie = new BasicClientCookie(cookieName, staticCookies.get(cookieName));
				cookie.setDomain(domain);
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
			}
		}

		this.context.setCookieStore(cookieStore);
		return cookieStore;
	}

	private static String getHeader(String name, CloseableHttpResponse response) {
		for (Header h : response.getAllHeaders()) {
			if (h.getName().equals(name)) {
				return h.getValue();
			}
		}
		return null;
	}

	private void generateErrorMessage(CloseableHttpResponse response) {
		if (this.statusCode != 200) {
			this.status = ConnectionStatus.ERROR;
			StringBuilder message = new StringBuilder();
			message.append("Falha no acesso a ").append(this.url);
			message.append(" com status ").append(response.getStatusLine().getStatusCode());
			message.append(" (").append(response.getStatusLine().getReasonPhrase()).append(")");

			String location = getHeader("Location", response);
			if (location != null) {
				message.append(" [").append(location).append("]");
			}

			this.statusReason = message.toString();
		}
	}

	private void consumeResponseContent(CloseableHttpResponse response) throws CorreiosException {
		HttpEntity entity = response.getEntity();
		this.mimeType = ContentType.get(entity) == null ? null : ContentType.get(entity).getMimeType();

		try {
			if (ContentType.TEXT_HTML.getMimeType().equals(this.mimeType) || ContentType.TEXT_PLAIN.getMimeType().equals(this.mimeType) || ContentType.TEXT_XML.getMimeType().equals(this.mimeType)
					|| ContentType.APPLICATION_JSON.getMimeType().equals(this.mimeType)) {

				this.charSet = this.charSet == null ? ContentType.get(response.getEntity()).getCharset() : this.charSet;
				this.charSet = this.charSet == null ? Charset.forName("ISO-8859-1") : this.charSet;
				this.content = IOUtils.toString(entity.getContent(), this.charSet);
			} else {
				this.content = Base64.encodeBase64String(IOUtils.toByteArray(entity.getContent()));
			}
			this.status = ConnectionStatus.SUCCESS;

		} catch (IOException e) {
			throw new CorreiosException(e);
		}
	}

	private void configPostEntityFromParams() throws UnsupportedEncodingException {
		if (this.params != null) {
			List<NameValuePair> postParams = new ArrayList<>();
			for (String key : this.params.keySet()) {
				postParams.add(new BasicNameValuePair(key, this.params.get(key)));
			}

			this.entityParam = new UrlEncodedFormEntity(postParams);
		}
	}

	private String generatePostUrlWithQueryParams() {
		return HttpConnectionUtil.generateGetUrlWithParams(url, paramsReqPost);
	}

	private String generateGetUrlWithQueryParams() {
		return HttpConnectionUtil.generateGetUrlWithParams(url, params);
	}

	private static String getProxyHostFromSystem() {
		return getProxyInfoFromSystem("conn.proxyHost");
	}

	private static String getProxyPortFromSystem() {
		return getProxyInfoFromSystem("conn.proxyPort");
	}

	private static String getProxyUserFromSystem() {
		return getProxyInfoFromSystem("conn.proxyUser");
	}

	private static String getProxyPassFromSystem() {
		return getProxyInfoFromSystem("conn.proxyPass");
	}

	private static ProxyType getProxyTypeFromSystem() {
		String name = getProxyInfoFromSystem("conn.proxyType");
		try {
			return ProxyType.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			return ProxyType.NONE;
		}
	}

	private static String getProxyInfoFromSystem(String mainName) {
		String info = System.getProperty(mainName);
		return info;
	}

	// ===========================
	// CLASSES AUXIALIARES
	// ===========================
	public static enum RequestMethod {
		POST,
		GET
	}

	public static enum ProxyType {
		NONE,
		HTTP,
		SOCKS
	}

	public static enum ConnectionStatus {
		NEW,
		SENDING,
		SUCCESS,
		ERROR,
		REDIRECT_SUCCESS,
		REDIRECT_ERROR
	}

	public static enum FollowRedirectStrategy {
		SINGLE_CONNECTION,
		MULTIPLE_CONNECTION,
		DISABLED
	}

	public static class CustomHttpsConnectionSocketFactory extends SSLConnectionSocketFactory {

		public CustomHttpsConnectionSocketFactory() {
			super(generateSslContext(), NoopHostnameVerifier.INSTANCE);
		}

		private static SSLContext generateSslContext() {
			try {
				SSLContextBuilder builder = SSLContexts.custom();
				builder.loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				});

				SSLContext sslContext = builder.build();
				return sslContext;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public CustomHttpsConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
			super(sslContext, hostnameVerifier);
		}

		@Override
		public Socket createSocket(HttpContext context) throws IOException {
			InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
			if (socksaddr == null) {
				return super.createSocket(context);
			}

			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}
	}

	public static class CustomHttpConnectionSocketFactory extends PlainConnectionSocketFactory {

		@Override
		public Socket createSocket(HttpContext context) throws IOException {
			InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
			if (socksaddr == null) {
				return super.createSocket(context);
			}

			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}
	}
}
