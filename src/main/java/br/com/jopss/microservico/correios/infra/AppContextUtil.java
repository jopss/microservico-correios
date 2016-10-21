package br.com.jopss.microservico.correios.infra;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AppContextUtil implements ApplicationContextAware {

	private static ApplicationContext contexto;

	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		contexto = appContext;
	}

	public static ApplicationContext getApplicationContext() {
		return contexto;
	}
}
