package drools.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DrlParser;
import org.drools.io.ResourceFactory;
import org.drools.lang.descr.PackageDescr;
import org.drools.xml.XmlDumper;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import de.fhg.fokus.ngni.openpe.action.drl.denyrequest.DenyRequestReference;
import de.fhg.fokus.ngni.openpe.action.drl.doprint.DoPrintReference;
import de.tub.av.pe.configuration.ConfigurationBean;
import de.tub.av.pe.configuration.OpenPEConfiguration;
import de.tub.av.pe.configuration.OpenPEConfigurationListener;
import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.impl.OpenPEContextManagerImpl;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.drools.impl.DrlPolicyRepositoryImpl;
import de.tub.av.pe.editor.PEEditor;
import de.tub.av.pe.editor.drools.impl.PEEditorImpl;
import de.tub.av.pe.eval.drools.DrlPolicyEvaluationImpl;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.repo.tssg.impl.TSSGIdentityRepositoryImpl;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.pem1.processing.CallableInterfaceUtils;
import de.tub.av.pe.pem1.processing.RequestContextInCallableMode;
import de.tub.av.pe.rulecontext.ActionReference;

public class DroolsPolicyEvaluationTest {

	private PolicyRepository polrepo;
	private OpenPEContextManagerImpl pemanager;
	static {
		BasicConfigurator.configure(new LoggerContext());
	}

	@Before
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("datasource",
				"com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource");
		properties.setProperty("databaseurl",
				"jdbc:mysql://127.0.0.1/broker?user=root&password=fokus");

		Class<? extends DataSource> clazz = Class.forName(
				properties.getProperty("datasource")).asSubclass(
				DataSource.class);
		DataSource dataSource = clazz.newInstance();

		clazz.getMethod("setUrl", String.class).invoke(dataSource,
				properties.getProperty("databaseurl"));

		pemanager = new OpenPEContextManagerImpl();
		OpenPEContext pecontext = pemanager.getInstance();

		polrepo = new DrlPolicyRepositoryImpl();
		pecontext.getPolicyRepositoryManager().register(polrepo);

		IdentityRepository identityrepo = new TSSGIdentityRepositoryImpl();
		pecontext.getIdentityRepositoryManager().register(identityrepo);

		Properties props = new Properties();
		props.put("openpe.db.type", polrepo.getRepositoryType());
		props.put("openpe.identities.type", identityrepo.getRepositoryType());

		pecontext.configRepos(props, dataSource);

		PolicyEvaluation poleval = new DrlPolicyEvaluationImpl();

		poleval.init(pemanager.getInstance().getPolicyRepositoryManager()
				.getInstance());
		pecontext.getPolicyEvaluationManager().register(poleval);
		props.put("openpe.policyevaluation.type", poleval.getType());
		pecontext.getPolicyEvaluationManager().config(props);

		PEEditor editor = new PEEditorImpl();
		editor.config(pemanager);
		pecontext.getPEEditorManager().register(editor);
		props.put("openpe.editor.type", editor.getEditorType());
		pecontext.getPEEditorManager().config(props);

		// initialize the listener
		OpenPEConfigurationListener configListener = new OpenPEConfigurationListener();
		configListener.init(pemanager);
		OpenPEConfiguration config = new OpenPEConfiguration();
		config.addListener(configListener);
		ConfigurationBean.addServiceConfig(config);
		config.init("config/openpecfg");

		// register the actions
		ActionReference denyaction = new DenyRequestReference();
		pecontext.getActionsRegistry().addActionReference(
				denyaction.getActionDescription().getType(), denyaction);
		ActionReference doPrint = new DoPrintReference();
		pecontext.getActionsRegistry().addActionReference(
				doPrint.getActionDescription().getType(), doPrint);

	}

	private String readPolicyContent(String path) throws Exception {
				
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
        kbuilder.add( ResourceFactory.newClassPathResource(path, DroolsPolicyEvaluationTest.class ),ResourceType.DRL );

        if(kbuilder.hasErrors())
        {
        	System.out.println(kbuilder.getErrors().toString());
        }      
		
		InputStream stream = this.getClass().getResourceAsStream(path);
		Reader in = new InputStreamReader(stream, "UTF-8");
		// transform to xml now
		DrlParser parser = new DrlParser();
		PackageDescr pckd = parser.parse(in);
		System.setProperty("drools.schema.validating", "false");
		XmlDumper dumper = new XmlDumper();
		String str = dumper.dump(pckd);
		
		return str;
	}

	private String addPolicy(String identity, String type, String event,
			String scope, int priority, String policyPath) throws Exception {
		String policyContent = readPolicyContent(policyPath);

		PolicyObject po = new PolicyObject();

		PolicyIdentifier pi = new PolicyIdentifier();
		pi.setIdentity(identity);
		pi.setIdType(type);
		pi.setEvent(event);
		pi.setScope(scope);
		pi.setPriority(priority);

		po.setPolicyIdentifier(pi);
		po.setPolicyContent(policyContent);

		return polrepo.addPolicy(po);
	}

	private void deletePolicy(String polId) throws Exception {
		PolicyIdentifier pi = new PolicyIdentifier();
		pi.setId(polId);
		polrepo.deletePolicy(pi);
	}

	@Test
	public void evaluate() throws Exception {
		String identity = "Alice";
		String type = "user";
		String event = "testEvent";

		String id = addPolicy(identity, type, event, "Target", 0,
				"AliceTargetTestEvent.drl");
		Assert.assertNotNull(id);

		String id2 = addPolicy("Bob", type, event, "Originator", 0,
		"BobOriginatorTestEvent.drl");
		Assert.assertNotNull(id2);


		PolicyEvaluation polEval = pemanager.getInstance()
				.getPolicyEvaluationManager().getInstance();

		List<String> targets =new ArrayList<String>();
		targets.add("Alice");
		
		Map<String, List<Object>> params = new HashMap<String, List<Object>>();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		values.add("test2");
		params.put("msg", values);
		RequestContextInCallableMode reqContext = CallableInterfaceUtils
				.genCallableRequestContext(pemanager, null, null, targets,
						"user", "testEvent", params);
		boolean res = polEval.evaluateAndEnforcePolicies(reqContext);
		Assert.assertTrue(res);
		
		RequestContextInCallableMode reqContext2 = CallableInterfaceUtils
		.genCallableRequestContext(pemanager, "Bob", "user", null,
				null, "testEvent", null);
		boolean res2 = polEval.evaluateAndEnforcePolicies(reqContext2);
		Assert.assertFalse(res2);
		
		deletePolicy(id);
		deletePolicy(id2);
	}
	
	@Test
	public void add() throws Exception
	{
		String identity = "Alice";
		String type = "user";
		String event = "testEvent";

		String id = addPolicy(identity, type, event, "Target", 0,
				"AliceTargetTestEvent.drl");
		Assert.assertNotNull(id);
	}
}
