package de.tub.av.pe.main.servlet;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.ngni.openpe.action.drl.denyrequest.DenyRequestReference;
import de.fhg.fokus.ngni.openpe.action.drl.doprint.DoPrintReference;
import de.tub.av.pe.configuration.ConfigurationBean;
import de.tub.av.pe.configuration.OpenPEConfiguration;
import de.tub.av.pe.configuration.OpenPEConfigurationListener;
import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.context.impl.OpenPEContextManagerImpl;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.drools.impl.DrlPolicyRepositoryImpl;
import de.tub.av.pe.editor.PEEditor;
import de.tub.av.pe.editor.drools.impl.PEEditorImpl;
import de.tub.av.pe.eval.drools.DrlPolicyEvaluationImpl;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.repo.tssg.impl.TSSGIdentityRepositoryImpl;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.rulecontext.ActionReference;

public class OpenPEServlet extends
		com.sun.xml.ws.transport.http.servlet.WSServlet {
	private static final long serialVersionUID = 8780976146710023204L;
	private Logger log = LoggerFactory.getLogger(OpenPEServlet.class);

	public static String PE_CONTEXT_MANAGER = "PE_CONTEXT_MANAGER";

	
	public void init(javax.servlet.ServletConfig servletConfig)
			throws javax.servlet.ServletException {
		OpenPEContextManager pemanager = new OpenPEContextManagerImpl();
		if(!configure(pemanager))
		{
			log.error ("Failed to propertly configure PE.");
		}
		else
			log.debug("PE was successfully configured");
		servletConfig.getServletContext().setAttribute(
					"PE_CONTEXT_MANAGER", pemanager);

		super.init(servletConfig);
	}

	private boolean configure(OpenPEContextManager pemanager)
			{
		boolean res = true;
		OpenPEContext pecontext = pemanager.getInstance();

		PolicyRepository polrepo = new DrlPolicyRepositoryImpl();
		pecontext.getPolicyRepositoryManager().register(polrepo);

		IdentityRepository identityrepo = new TSSGIdentityRepositoryImpl();
		pecontext.getIdentityRepositoryManager().register(identityrepo);

		Properties props = new Properties();
		props.put("openpe.db.type", polrepo.getRepositoryType());
		props.put("openpe.identities.type", identityrepo.getRepositoryType());
		try{
		pecontext.configRepos(props);
		}catch (ConfigurationException e)
		{
			e.printStackTrace();
			log.error(e.getMessage(), e);
			res = false;
		}
		PolicyEvaluation poleval = new DrlPolicyEvaluationImpl();

		poleval.init(pemanager.getInstance().getPolicyRepositoryManager()
				.getInstance());
		pecontext.getPolicyEvaluationManager().register(poleval);
		props.put("openpe.policyevaluation.type", poleval.getType());
		try {
			pecontext.getPolicyEvaluationManager().config(props);
		} catch (ConfigurationException e) {
			log.error (e.getMessage(), e);
			e.printStackTrace();
			res= false;
		}

		PEEditor editor = new PEEditorImpl();
		editor.config(pemanager);
		pecontext.getPEEditorManager().register(editor);
		props.put("openpe.editor.type", editor.getEditorType());
		try{
		pecontext.getPEEditorManager().config(props);
		}catch(ConfigurationException e)
		{
			e.printStackTrace();
			log.error(e.getMessage(), e);
			res = false;
		}
		// initialize the listener
		OpenPEConfigurationListener configListener = new OpenPEConfigurationListener();
		configListener.init(pemanager);
		OpenPEConfiguration config = new OpenPEConfiguration();
		config.addListener(configListener);
		ConfigurationBean.addServiceConfig(config);
		config.init("/etc/tomcat6");

		// register the actions
		ActionReference denyaction = new DenyRequestReference();
		pecontext.getActionsRegistry().addActionReference(
				denyaction.getActionDescription().getType(), denyaction);
		ActionReference doPrint = new DoPrintReference();
		pecontext.getActionsRegistry().addActionReference(
				doPrint.getActionDescription().getType(), doPrint);
		return res;
	}
}
