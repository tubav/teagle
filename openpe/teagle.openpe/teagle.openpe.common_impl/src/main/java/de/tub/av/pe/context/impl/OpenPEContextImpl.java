
package de.tub.av.pe.context.impl;

import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.db.PolicyRepositoryManager;
import de.tub.av.pe.db.impl.PolicyRepositoryManagerImpl;
import de.tub.av.pe.editor.PEEditorManager;
import de.tub.av.pe.editor.impl.PEEditorManagerImpl;
import de.tub.av.pe.identities.IdentityRepositoryManager;
import de.tub.av.pe.identities.impl.IdentityRepositoryManagerImpl;
import de.tub.av.pe.pe.PolicyEvaluationManager;
import de.tub.av.pe.pe.impl.PolicyEvaluationManagerImpl;
import de.tub.av.pe.rule.utils.LoggingBean;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;
import de.tub.av.pe.rulecontext.RuleContextFactory;
import de.tub.av.pe.rulecontext.impl.RuleContextFactoryImpl;

public class OpenPEContextImpl implements OpenPEContext {

	private LoggingBean bean = new LoggingBean();

	private RuleActionsRegistry actionsRegistry;

	private PolicyRepositoryManager policyRepositoryManager;

	private IdentityRepositoryManager identityRepositoryManager;

	private PolicyEvaluationManager policyEvaluationManager;

	private PEEditorManager peeditorManager;

	private RuleContextFactory ruleContextManager;
	
	public OpenPEContextImpl() {
		this.actionsRegistry = new RuleActionsRegistryImpl();
		this.policyEvaluationManager = new PolicyEvaluationManagerImpl();
		this.policyRepositoryManager = new PolicyRepositoryManagerImpl();
		this.identityRepositoryManager = new IdentityRepositoryManagerImpl();
		this.peeditorManager = new PEEditorManagerImpl();
		this.ruleContextManager = new RuleContextFactoryImpl();
	}

	@Override
	public LoggingBean getLoggingBeanObject() {
		return this.bean;
	}

	@Override
	public PolicyRepositoryManager getPolicyRepositoryManager() {
		return this.policyRepositoryManager;
	}

	@Override
	public IdentityRepositoryManager getIdentityRepositoryManager() {
		return this.identityRepositoryManager;
	}

	@Override
	public RuleActionsRegistry getActionsRegistry() {
		return this.actionsRegistry;
	}

	@Override
	public PolicyEvaluationManager getPolicyEvaluationManager() {
		return policyEvaluationManager;
	}

	@Override
	public Object getProperty(String name) {
		return null;
	}

	@Override
	public PEEditorManager getPEEditorManager() {
		return peeditorManager;
	}

	@Override
	public void configRepos(Properties props) throws ConfigurationException {
		this.policyRepositoryManager.config(props);
		this.identityRepositoryManager.config(props);
	}

	@Override
	public void configRepos(Properties props, DataSource datasource)
			throws ConfigurationException {
		this.policyRepositoryManager.config(props, datasource);
		this.identityRepositoryManager.config(props, datasource);
	}

	@Override
	public RuleContextFactory getRuleContextManager() {
		return this.ruleContextManager;
	}

}
