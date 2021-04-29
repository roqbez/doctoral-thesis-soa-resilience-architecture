package br.ufsc.gsigma.infrastructure.util;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppContext implements ApplicationContextAware {

	private static ApplicationContext ctx;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		ctx = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return getApplicationContext(new String[] { "applicationContext.xml" });
	}

	public static ApplicationContext getApplicationContext(String... locations) {
		if (ctx == null)
			ctx = new ClassPathXmlApplicationContext(locations);

		return ctx;
	}

	@SuppressWarnings("all")
	public static <T> T getBean(Class<T> c) {

		Map m = AppContext.getApplicationContext().getBeansOfType(c);

		if (m.size() == 1)
			return (T) m.values().iterator().next();

		return null;

	}

	public static <T> T getBean(Class<T> c, String beanName) {

		T service = (T) AppContext.getApplicationContext().getBean(beanName, c);

		return service;
	}
}
