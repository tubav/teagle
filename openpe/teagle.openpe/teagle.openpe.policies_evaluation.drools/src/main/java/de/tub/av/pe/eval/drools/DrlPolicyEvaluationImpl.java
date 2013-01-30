package de.tub.av.pe.eval.drools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.model.drools.DrlPERuleImpl;
import de.tub.av.pe.model.drools.DrlPERulesetImpl;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.rule.utils.ReasonOverview;
import de.tub.av.pe.rule.utils.RulesEvalOverview;
import de.tub.av.pe.rule.utils.LoggingBean.LogEntry;
import de.tub.av.pe.rule.utils.ReasonOverview.ReasonEntry;
import de.tub.av.pe.rule.utils.RulesEvalOverview.RuleEntry;
import de.tub.av.pe.rulecontext.PERuleset;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;
import de.tub.av.pe.rulecontext.RuleContext;
import de.tub.av.pe.rulecontext.RuleContextFactory;

public class DrlPolicyEvaluationImpl implements PolicyEvaluation {

	private PolicyRepository polrepo; 
	private Logger log = LoggerFactory.getLogger(DrlPolicyEvaluationImpl.class);

	public enum EnforcementResult {
		ALLOW, DENY
	}

	@Override
	public boolean evaluateAndEnforcePolicies(RequestContextInterface reqContext) {

		Map<String, RuleContext> ruleContextMap = new HashMap<String, RuleContext>();
		RuleContextFactory rulectxfactory = reqContext.getOpenPEContext()
				.getRuleContextManager();
		RuleActionsRegistry actionsRegistry = reqContext.getOpenPEContext()
				.getActionsRegistry();
		ReasonOverview evalResultReason = reqContext.getLogEntryGUI()
				.getReason();

		try {
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
			List<PERuleset> rulesets = polrepo.getPERulesets(0, reqContext);
			for (PERuleset ruleset : rulesets) {
				Collection<KnowledgePackage> pkgs = ((DrlPERulesetImpl) ruleset)
						.getKnowledgePackage();
				if (pkgs != null) {
					kbase.addKnowledgePackages(pkgs);
				}
			}

			StatefulKnowledgeSession ksession = kbase
					.newStatefulKnowledgeSession();
			/** debug agenda and working memory events
			ksession.addEventListener(new DebugAgendaEventListener());
			ksession.addEventListener(new DebugWorkingMemoryEventListener());
			**/
			// prepare the fact
			PEInputRequest fact = new PEInputRequest(reqContext);
			DrlActionsManager actionsMng = new DrlActionsManager(
					actionsRegistry, ruleContextMap);

			ksession.addEventListener(new DrlAgendaEventListener(
					rulectxfactory, ruleContextMap));
			ksession.insert(fact);
			Map<String, List<Object>> paramsMap = reqContext.getParametersMap();
			Iterator<Entry<String, List<Object>>> it = paramsMap.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, List<Object>> entry = it.next();
				for (Object value : entry.getValue()) {
					Parameter param = new Parameter(entry.getKey(),
							(String) value);
					ksession.insert(param);
				}
			}
			ksession.insert(actionsMng);

			// build the overview
			for (PERuleset ruleset : rulesets) {

				DrlPERulesetImpl drlruleset = (DrlPERulesetImpl)ruleset;
				ReasonEntry reasonEntry = evalResultReason.createNewEntry();
				reasonEntry.setPolicyName(drlruleset.getId());
				RulesEvalOverview rulesEvalOverview = new RulesEvalOverview();
				reasonEntry.setRulesEvalOverview(rulesEvalOverview);
				
				String packageName = drlruleset.getPackage();
				List<String> ruleids = drlruleset.getRuleIds();
				for (String ruleid: ruleids)
				{
					String key = packageName+"/"+ruleid;
					RuleContext ruleCtx = ruleContextMap.get(key);
					if (ruleCtx == null)
					{
						reasonEntry.setReason("Rule "+ ruleid+" does not apply.");
					}else
					{
						reasonEntry.setReason("Rule "+ ruleid+" applies.");
						RuleEntry ruleEntry = rulesEvalOverview.createNewEntry();
						ruleEntry.setRule(ruleCtx.getRule());
						ruleCtx.setRuleEntryGUI(ruleEntry);
					}
				}				
			}
			//enforce the actions
			ksession.fireAllRules();
			ksession.dispose();

			EnforcementResult result = actionsMng.getEnforcementResult();
			LogEntry logEntry = reqContext.getLogEntryGUI();
			if (result == EnforcementResult.ALLOW) {
				log.debug("Message has been ALLOWED");
				logEntry.setFinalDecission("ALLOWED");
				return true;
			} else {
				log.debug("Message has been DENIED");
				logEntry.setFinalDecission("DENIED");
				return false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public String getType() {
		return "Drools";
	}

	@Override
	public void config(Properties props) throws ConfigurationException {
	}

	@Override
	public Object evalValueOfParameter(RuleContext ruleContext, String parameter) {
		return null;
	}

	@Override
	public void init(PolicyRepository polrepo) {
		this.polrepo = polrepo;
	}

	class DrlAgendaEventListener extends DefaultAgendaEventListener {

		private RuleContextFactory rulectxfactory;
		private Map<String, RuleContext> ruleContextMap;

		public DrlAgendaEventListener(RuleContextFactory rulectxfactory,
				Map<String, RuleContext> ruleContextMap) {
			this.rulectxfactory = rulectxfactory;
			this.ruleContextMap = ruleContextMap;
		}

		public void activationCreated(ActivationCreatedEvent event) {
			Rule rule = event.getActivation().getRule();
			String key = rule.getPackageName() + "/" + rule.getName();
			DrlPERuleImpl drlrule = new DrlPERuleImpl();
			drlrule.setRule(rule);
			drlrule.setAgenda(event.getKnowledgeRuntime().getAgenda());
			RuleContext rulectx = rulectxfactory.newRuleContext();
			rulectx.setRule(drlrule);
			ruleContextMap.put(key, rulectx);
			log.debug("Rule {} was activated "+key);
			super.activationCreated(event);
		}
	}

}
