package de.tub.av.pe.db.drools.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryException;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.identities.IdentitiesRelation;
import de.tub.av.pe.identities.IdentityObject;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.IdentityRepositoryException;
import de.tub.av.pe.model.drools.DrlPERulesetImpl;
import de.tub.av.pe.model.drools.PolicyValidation;
import de.tub.av.pe.rulecontext.PERuleset;
import de.tub.av.pe.rulecontext.ValidationErrorHandler;

public class DrlPolicyRepositoryImpl implements PolicyRepository {

	private PolicyRepoStorage db;

	private Logger log = LoggerFactory.getLogger(DrlPolicyRepositoryImpl.class);

	private Map<String, Object> rulesetObjcache = new HashMap<String, Object>();

	private Map<Object, DrlPERulesetImpl> rulesetcache = new HashMap<Object, DrlPERulesetImpl>();
	
	@Override
	public void config(Properties props) throws ConfigurationException {
		String jndi = props.getProperty("openpe.pol.db.jndi.url");
		DataSource datasource;
		if (jndi != null) {
			try {
				//datasource = (DataSource) InitialContext.doLookup(jndi);
				 Context ctx = new InitialContext();
				 datasource = (DataSource)ctx.lookup("java:comp/env/" + jndi);
				db = new PolicyRepoStorage(datasource);
			} catch (NamingException e) {
				throw new ConfigurationException(
						"Lookup failed for: [" + jndi+"]", e);
			}
		}
	}

	@Override
	public void config(Properties props, DataSource datasource)
			throws ConfigurationException {
		if (datasource == null)
			config(props);
		else
			db = new PolicyRepoStorage(datasource);
	}

	@Override
	public String getRepositoryType() {
		return "DroolsMySql";
	}

	@Override
	public List<PolicyObject> getPolicies(String identityType)
			throws PolicyRepositoryException {
		RowList rowList = null;
		Object[] args = new Object[1];
		args[0] = identityType;
		List<PolicyObject> polst = new ArrayList<PolicyObject>();

		try {
			rowList = db.select(db.SQL_LIST_POLICIES_BY_TYPE, args);
			if (rowList != null) {
				for (Row row : rowList) {
					PolicyObject po = new PolicyObject();
					po.setPolicyContent((String) row.get("policy"));
					PolicyIdentifier pi = new PolicyIdentifier();
					po.setPolicyIdentifier(pi);
					pi.setId(((Integer) row.get("id")).toString());
					pi.setIdentity((String) row.get("identity"));
					pi.setIdType((String) row.get("idtype"));
					pi.setPriority(((Integer) row.get("priority")).intValue());
					pi.setScope((String) row.get("scope"));
					polst.add(po);
				}
			}
			return polst;

		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public int getPoliciesCount(String identityType)
			throws PolicyRepositoryException {
		Object[] args = new Object[1];
		args[0] = identityType;
		try {
			Row row = db.select1(db.SQL_COUNT_POLICIES_BY_TYPE, args);
			if (row != null)
				return ((Long) row.get("count(*)")).intValue();

		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public String getPolicyContent(PolicyIdentifier pi)
			throws PolicyRepositoryException {
		try {
			if (pi.getId() != null) {
				Object[] args = new Object[1];
				args[0] = pi.getId();
				Row row = db.select1(db.SQL_GET_POLICY_BY_ID, args);
				return row == null ? null : (String) row.get("policy");
			} else {
				Object[] args = new Object[5];
				args[0] = pi.getIdentity();
				args[1] = pi.getIdType();
				args[2] = pi.getScope();
				args[3] = pi.getEvent();
				args[4] = pi.getPriority();
				Row row = db.select1(db.SQL_GET_POLICY, args);
				return row == null ? null : (String) row.get("policy");
			}
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public PolicyObject getPolicy(PolicyIdentifier pi)
			throws PolicyRepositoryException {
		try {
			if (pi.getId() != null) {
				Object[] args = new Object[1];
				args[0] = pi.getId();
				Row row = db.select1(db.SQL_GET_POLICY_BY_ID, args);
				if (row != null) {
					PolicyObject po = new PolicyObject();
					po.setPolicyContent((String) row.get("policy"));
					po.setPolicyIdentifier(pi);
					pi.setIdentity((String) row.get("identity"));
					pi.setIdType((String) row.get("idtype"));
					pi.setScope((String) row.get("scope"));
					pi.setEvent((String) row.get("event"));
					pi.setPriority((Integer) row.get("priority"));
					return po;
				}
			} else {
				Object[] args = new Object[5];
				args[0] = pi.getIdentity();
				args[1] = pi.getIdType();
				args[2] = pi.getScope();
				args[3] = pi.getEvent();
				args[4] = pi.getPriority();
				Row row = db.select1(db.SQL_GET_POLICY, args);
				if (row != null) {
					PolicyObject po = new PolicyObject();
					String id = ((Integer) row.get("id")).toString();
					pi.setId(id);
					po.setPolicyContent((String) row.get("policy"));
					po.setPolicyIdentifier(pi);
					return po;
				}
			}
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void updatePolicy(PolicyObject po) throws PolicyRepositoryException,
			DuplicateValueException, ValidationException {
		PolicyIdentifier pi = po.getPolicyIdentifier();
		if (pi.getId() == null) {
			throw new PolicyRepositoryException("id must NOT be null");
		}
		ValidationErrorHandler handler = new ValidationErrorHandler();

		if (PolicyValidation.validate(po.getPolicyContent(), handler)) {
		Object[] args = new Object[7];
		args[0] = pi.getIdentity();
		args[1] = pi.getIdType();
		args[2] = pi.getScope();
		args[3] = pi.getEvent();
		args[4] = pi.getPriority();
		args[5] = po.getPolicyContent();
		args[6] = pi.getId();

		try {
			PolicyObject oldPolicyObject = this.getPolicyObject(pi.getId());
			db.update(db.SQL_UPDATE_POLICY, args);
			updatecache(oldPolicyObject.getPolicyIdentifier());
		} catch (SQLException e) {
			if (isDuplicationException(e)) {
				throw new DuplicateValueException(
						"Duplicated value becauseof  " + e.getMessage());
			} else
				throw new PolicyRepositoryException(e.getMessage(), e);
		}
		}else
			throw new ValidationException("failed when updating policy: ["+handler.getError()+"]");
	}

	@Override
	public String addPolicy(PolicyObject po) throws PolicyRepositoryException,
			DuplicateValueException, ValidationException {

		ValidationErrorHandler handler = new ValidationErrorHandler();
		if (PolicyValidation.validate(po.getPolicyContent(), handler)) {

			PolicyIdentifier pi = po.getPolicyIdentifier();
			Object[] args = new Object[6];
			args[0] = pi.getIdentity();
			args[1] = pi.getIdType();
			args[2] = pi.getScope();
			args[3] = pi.getEvent();
			args[4] = pi.getPriority();
			args[5] = po.getPolicyContent();
			try {
				String id = db.insert(db.SQL_ADD_POLICY, args);
				updatecache(pi);
				if (id == null) {
					throw new PolicyRepositoryException(
							"failed to fetch generating key when adding a new policy");
				} else {
					pi.setId(id);
					return id;
				}
			} catch (SQLException e) {
				if (isDuplicationException(e)) {
					throw new DuplicateValueException("duplicated policy", e);
				} else
					throw new PolicyRepositoryException(e.getMessage(), e);
			}
		}else
			throw new ValidationException("failed when adding a new policy: ["+handler.getError()+"]");
	}

	@Override
	public void deletePolicy(PolicyIdentifier pi)
			throws PolicyRepositoryException {
		try {
			if (pi.getId() != null) {
				Object[] args = new Object[1];
				args[0] = pi.getId();
				PolicyObject oldPolicyOBject = this.getPolicyObject(pi.getId());
				if(oldPolicyOBject != null)
					updatecache(oldPolicyOBject.getPolicyIdentifier());
				db.delete(db.SQL_DELETE_POLICY_BY_ID, args);
			} else {
				Object[] args = new Object[5];
				args[0] = pi.getIdentity();
				args[1] = pi.getIdType();
				args[2] = pi.getScope();
				args[3] = pi.getEvent();
				args[4] = new Integer(pi.getPriority());
				db.delete(db.SQL_DELETE_POLICY, args);
				updatecache(pi);
			}
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType)
			throws PolicyRepositoryException {
		Object[] args = new Object[1];
		args[0] = identityType;
		List<PolicyIdentifier> piList = new ArrayList<PolicyIdentifier>();
		try {
			RowList rowList = db.select(db.SQL_LIST_POLICIES_IDF_BY_TYPE, args);
			for (Row row : rowList) {
				PolicyIdentifier pi = new PolicyIdentifier();
				pi.setId(((Integer) row.get("id")).toString());
				pi.setIdentity((String) row.get("identity"));
				pi.setIdType((String) row.get("idtype"));
				pi.setPriority(((Integer) row.get("priority")).intValue());
				pi.setScope((String) row.get("scope"));
				pi.setEvent((String) row.get("event"));
				piList.add(pi);
			}
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
		return piList;
	}

	@Override
	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType,
			int start, int length) throws PolicyRepositoryException {
		Object[] args = new Object[3];
		args[0] = identityType;
		args[1] = start;
		args[2] = length;
		List<PolicyIdentifier> piList = new ArrayList<PolicyIdentifier>();
		try {
			RowList rowList = db.select(
					db.SQL_LIST_POLICIES_IDF_BY_TYPE_WITH_LIMIT, args);
			for (Row row : rowList) {
				PolicyIdentifier pi = new PolicyIdentifier();
				pi.setId(((Integer) row.get("id")).toString());
				pi.setIdentity((String) row.get("identity"));
				pi.setIdType((String) row.get("idtype"));
				pi.setPriority(((Integer) row.get("priority")).intValue());
				pi.setScope((String) row.get("scope"));
				pi.setEvent((String) row.get("event"));
				piList.add(pi);
			}
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
		return piList;
	}

	@Override
	public List<PERuleset> getPERulesets(int priority,
			RequestContextInterface reqContext)
			throws PolicyRepositoryException {

		List<PERuleset> rulesetList = new ArrayList<PERuleset>();

		IdentityRepository idrepo = reqContext.getOpenPEContext()
				.getIdentityRepositoryManager().getInstance();

		// originator
		String origidentity = reqContext.getOriginatorIdentity();
		if (origidentity != null) {
			String identityType = reqContext.getOriginatorIdentityType();
			identityType = identityType == null ? "user" : identityType;
			rulesetList.addAll(getPoliciesByScope("Originator", origidentity,
					identityType, priority, reqContext, idrepo));
		}

		// target
		List<String> targetidentities = reqContext.getTargetIdentities();
		String identityType = reqContext.getTargetIdentitiesType();
		identityType = identityType == null ? "user" : identityType;
		for (String targetIdentity : targetidentities) {
			rulesetList.addAll(getPoliciesByScope("Target", targetIdentity,
					identityType, priority, reqContext, idrepo));
		}

		return rulesetList;
	}

	@Override
	public String getPolicyId(PolicyIdentifier pi)
			throws PolicyRepositoryException {
		Object[] args = new Object[5];
		args[0] = pi.getIdentity();
		args[1] = pi.getIdType();
		args[2] = pi.getScope();
		args[3] = pi.getEvent();
		args[4] = pi.getPriority();
		try {
			Row row = db.select1(db.SQL_GET_POLICY, args);
			if (row != null) {
				String id = ((Integer) row.get("id")).toString();
				pi.setId(id);
				return id;
			} else
				return null;
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public PolicyObject getPolicyObject(String id)
			throws PolicyRepositoryException {
		Object[] args = new Object[1];
		args[0] = id;
		try {
			Row row = db.select1(db.SQL_GET_POLICY_BY_ID, args);
			if (row != null) {
				PolicyObject po = new PolicyObject();
				po.setPolicyContent((String) row.get("policy"));
				PolicyIdentifier pi = new PolicyIdentifier();
				po.setPolicyIdentifier(pi);
				pi.setId(id);
				pi.setIdentity((String) row.get("identity"));
				pi.setIdType((String) row.get("idtype"));
				pi.setScope((String) row.get("scope"));
				pi.setEvent((String) row.get("event"));
				pi.setPriority((Integer) row.get("priority"));
				return po;
			}
			return null;
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public PolicyIdentifier getPolicyIdentifier(String id)
			throws PolicyRepositoryException {
		Object[] args = new Object[1];
		args[0] = id;
		try {
			Row row = db.select1(db.SQL_GET_POLICY_IDENTIFIER_BY_ID, args);
			if (row != null) {
				PolicyIdentifier pi = new PolicyIdentifier();
				pi.setId(id);
				pi.setIdentity((String) row.get("identity"));
				pi.setIdType((String) row.get("idtype"));
				pi.setScope((String) row.get("scope"));
				pi.setEvent((String) row.get("event"));
				pi.setPriority((Integer) row.get("priority"));
				return pi;
			}
			return null;
		} catch (SQLException e) {
			throw new PolicyRepositoryException(e.getMessage(), e);
		}
	}

	private List<PERuleset> getPoliciesByScope(String scope, String identity,
			String identityType, int priority,
			RequestContextInterface reqContext, IdentityRepository idrepo)
			throws PolicyRepositoryException {
		List<PERuleset> rulesetList = new ArrayList<PERuleset>();

		String event = reqContext.getEvent();
		try {
			List<String> scIdList = idrepo
					.getSecondIdentitiesTypeList(identityType);

			for (String secondIdentityType : scIdList) {

				IdentitiesRelation relation = new IdentitiesRelation();
				relation.setFirstIdentityType(identityType);
				relation.setFirstIdentity(identity);
				relation.setSecondIdentitiesType(secondIdentityType);

				List<IdentityObject> values = idrepo
						.getSecondIdentities(relation);

				for (IdentityObject value : values) {
					PERuleset ruleset = fetchPolicy(value.getName(),
							secondIdentityType, scope, event, priority);
					if (ruleset != null)
						rulesetList.add(ruleset);
				}
			}

		} catch (IdentityRepositoryException e) {
			log.error(
					"error fetching collective identities associated to identity {} of type {}",
					new String[] { identity, identityType }, e);
		}

		//fetch organization.. 
		
		
		PERuleset ruleset = fetchPolicy(identity, identityType, scope, "*",
				priority);
		if (ruleset != null)
			rulesetList.add(ruleset);
			
		ruleset = fetchPolicy(identity, identityType, scope, event,
				priority);
		if (ruleset != null)
			rulesetList.add(ruleset);
		return rulesetList;
	}



	private PERuleset fetchPolicy(String identity, String type, String scope,
			String event, int priority) throws PolicyRepositoryException {
		Object[] args = new Object[5];
		args[0] = identity;
		args[1] = type;
		args[2] = scope;
		args[3] = event;
		args[4] = priority;

		StringBuffer name = new StringBuffer();
		name.append(type);
		name.append("/");
		name.append(identity);
		name.append("/");
		name.append(scope);
		name.append("/");
		name.append(event);

		Object syncobj;

		synchronized (rulesetObjcache) {
			syncobj = rulesetObjcache.get(name.toString());
			if (syncobj == null) {
				syncobj = new Object();
				rulesetObjcache.put(name.toString(), syncobj);
			}
		}
		synchronized (syncobj) {
			DrlPERulesetImpl peruleset = rulesetcache.get(syncobj);
			if (peruleset == null) {
				try {
					RowList rowlist = db.select(db.SQL_LIST_POLICIES, args);
					if (rowlist.size() == 0) {
						peruleset = new DrlPERulesetImpl();
						peruleset.setId("EMPTY");
					} else {
						String policy = (String) rowlist.get(0).get("policy");
						String id = ((Integer)rowlist.get(0).get("id")).toString();
						peruleset = new DrlPERulesetImpl();
						peruleset.setContent(policy);
						peruleset.setId(id+"/"+name.toString());
					}
					rulesetcache.put(syncobj, peruleset);
				} catch (SQLException e) {
					throw new PolicyRepositoryException(e.getMessage(), e);
				}
			}
			if (peruleset != null && peruleset.getId().equals("EMPTY"))
			{
					return null;
			}
			return peruleset;
		}
	}



	@Override
	public List<PERuleset> getPERulesets(PolicyIdentifier pi)
			throws PolicyRepositoryException {

		List<PERuleset> rulesetList = new ArrayList<PERuleset>();
		String id = pi.getId();
		if (id != null) {
			PolicyObject po = this.getPolicyObject(id);
			String policyContent = po.getPolicyContent();
			
			DrlPERulesetImpl ruleset = new DrlPERulesetImpl();
			ruleset.setContent(policyContent);
			rulesetList.add(ruleset);
		} else {
			Object[] args = new Object[5];
			args[0] = pi.getIdentity() == null ? "%" : pi.getIdentity();
			args[1] = pi.getIdType() == null ? "%" : pi.getIdType();
			args[2] = pi.getScope() == null ? "%" : pi.getScope();
			args[3] = pi.getEvent() == null ? "%" : pi.getEvent();
			args[4] = pi.getPriority() == -1 ? "%" : pi.getPriority();

			try {
				RowList rowList = db.select(db.SQL_LIST_POLICIES_BY_ALL, args);
				for (Row row : rowList) {
					String policyContent = (String) row.get("policy");
					if (policyContent != null) {
						
						DrlPERulesetImpl ruleset = new DrlPERulesetImpl();
						ruleset.setContent(policyContent);
						rulesetList.add(ruleset);
					}
				}
			} catch (SQLException e) {
				throw new PolicyRepositoryException(e.getMessage(), e);
			}
		}
		return rulesetList;
	}

	@Override
	public List<PolicyObject> getPolicies(PolicyIdentifier pi)
			throws PolicyRepositoryException {

		List<PolicyObject> poList = new ArrayList<PolicyObject>();
		String id = pi.getId();
		if (id != null) {
			PolicyObject po = this.getPolicyObject(id);
			poList.add(po);
		} else {
			Object[] args = new Object[5];
			args[0] = pi.getIdentity() == null ? "%" : pi.getIdentity();
			args[1] = pi.getIdType() == null ? "%" : pi.getIdType();
			args[2] = pi.getScope() == null ? "%" : pi.getScope();
			args[3] = pi.getEvent() == null ? "%" : pi.getEvent();
			args[4] = pi.getPriority() == -1 ? "%" : pi.getPriority();

			try {
				RowList rowList = db.select(db.SQL_LIST_POLICIES_BY_ALL, args);
				for (Row row : rowList) {
					PolicyIdentifier dbpi = new PolicyIdentifier();
					dbpi.setId(((Integer) row.get("id")).toString());
					dbpi.setIdentity((String) row.get("identity"));
					dbpi.setIdType((String) row.get("idtype"));
					dbpi.setScope((String) row.get("scope"));
					dbpi.setEvent((String) row.get("event"));
					dbpi.setPriority((Integer) row.get("priority"));
					String policyContent = (String) row.get("policy");
					PolicyObject po = new PolicyObject();
					po.setPolicyContent(policyContent);
					po.setPolicyIdentifier(dbpi);
					poList.add(po);
				}
			} catch (SQLException e) {
				throw new PolicyRepositoryException(e.getMessage(), e);
			}
		}
		return poList;
	}
	
	
	private void updatecache(PolicyIdentifier pi) {
		StringBuilder name = new StringBuilder();
		name.append(pi.getIdType());
		name.append("/");
		name.append(pi.getIdentity());
		name.append("/");
		name.append(pi.getScope());
		name.append("/");
		name.append(pi.getEvent());

		Object syncobj;
		synchronized (rulesetObjcache) {
			syncobj = rulesetObjcache.get(name.toString());
			if (syncobj != null) {
				rulesetObjcache.remove(name.toString());
			} else
				return;
		}
		synchronized (syncobj) {
			rulesetcache.remove(syncobj);
		}
	}
	
	
	private boolean isDuplicationException(SQLException e) {
			if (e.getSQLState().contains("23000")
					| e.getErrorCode() == 1022) {
				return true;
			}
		return false;
	}
	
}
