package de.fhg.fokus.teaglewebsite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import teagle.vct.model.ModelManager;
import teagle.vct.model.RepoClientConfig;

public class TeagleServletContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void contextInitialized(ServletContextEvent arg0) {
//		Properties props = new Properties();
//		props.setProperty("repo.username", "testuser");
//		props.setProperty("repo.password", "test");
		//props.setProperty("repo.url", "http://repos.pii.tssg.org:8080/repository/rest");
		//props.setProperty("repo.url", "http://192.168.144.11:8080/repository/rest");//for teagle playground
//		props.setProperty("repo.url", "http://127.0.0.1:8080/repository/rest");//for ngn2fi playground, repo in same tomcat
//		props.setProperty("repo.cache.autoclear.enabled", "true");
//		props.setProperty("repo.cache.autoclear.interval", "300000");
//		props.setProperty("repo.cache.prefetching.enabled", "true");
		try {
			ModelManager.getInstance().config(new RepoClientConfig(new URL("http://193.175.132.210:8080/repository/rest"), "testuser", "test", true));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
