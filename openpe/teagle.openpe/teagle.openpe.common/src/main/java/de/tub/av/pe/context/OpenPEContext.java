package de.tub.av.pe.context;
import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.db.PolicyRepositoryManager;
import de.tub.av.pe.editor.PEEditorManager;
import de.tub.av.pe.identities.IdentityRepositoryManager;
import de.tub.av.pe.pe.PolicyEvaluationManager;
import de.tub.av.pe.rule.utils.LoggingBean;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;
import de.tub.av.pe.rulecontext.RuleContextFactory;

public interface OpenPEContext {
	
	public LoggingBean getLoggingBeanObject();
	
	public PolicyRepositoryManager getPolicyRepositoryManager();

	public IdentityRepositoryManager getIdentityRepositoryManager();
	
	public RuleActionsRegistry getActionsRegistry();
		
	public PolicyEvaluationManager getPolicyEvaluationManager();
		
	public Object getProperty(String name);

	public PEEditorManager getPEEditorManager();
	
	public RuleContextFactory getRuleContextManager();
	
	public void configRepos(Properties props) throws ConfigurationException;

	public void configRepos(Properties props, DataSource datasource) throws ConfigurationException;

}
