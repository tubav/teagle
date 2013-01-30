package de.tub.av.pe.identities.repo.tssg.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import teagle.vct.model.ModelManager;
import teagle.vct.model.Organisation;
import teagle.vct.model.Ptm;
import teagle.vct.model.RepoClientConfig;
import teagle.vct.model.ResourceSpec;
import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.identities.IdentitiesRelation;
import de.tub.av.pe.identities.IdentityObject;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.IdentityRepositoryException;

public class TSSGIdentityRepositoryImpl implements IdentityRepository {

	@Override
	public List<IdentityObject> getIdentities(String identityType)
			throws IdentityRepositoryException {
		return null;
	}

	@Override
	public int addIdentity(String identityType, String identity)
			throws IdentityRepositoryException, DuplicateValueException {
		return 0;
	}

	@Override
	public void deleteIdentity(String identityType, String identity)
			throws IdentityRepositoryException {

	}

	@Override
	public void deleteIdentity(String identityType, int id)
			throws IdentityRepositoryException {
		// TODO Auto-generated method stub
	}

	@Override
	public void config(Properties props0) throws ConfigurationException {
		String repoUrl = props0.getProperty("repo.url");
		String username = props0.getProperty("repo.username");
		String password = props0.getProperty("repo.password");
		String doPrefetching = props0
				.getProperty("repo.cache.prefetching.enabled");
		String doAutoclear = props0.getProperty("repo.cache.autoclear.enabled");

		RepoClientConfig rpc;
		try {
			if (repoUrl != null && username != null && password != null
					&& doPrefetching != null && doAutoclear != null) {
				rpc = new RepoClientConfig(new URL(repoUrl), username,
						password, Boolean.parseBoolean(doPrefetching),
						Boolean.parseBoolean(doAutoclear));
				ModelManager.getInstance().config(rpc);
			}
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e.getMessage(), e);
		}

	}

	@Override
	public void config(Properties props0, DataSource datasource)
			throws ConfigurationException {

		String repoUrl = props0.getProperty("repo.url");
		String username = props0.getProperty("repo.username");
		String password = props0.getProperty("repo.password");
		String doPrefetching = props0
				.getProperty("repo.cache.prefetching.enabled");
		String doAutoclear = props0.getProperty("repo.cache.autoclear.enabled");

		RepoClientConfig rpc;
		try {
			if (repoUrl != null && username != null && password != null
					&& doPrefetching != null && doAutoclear != null) {
				rpc = new RepoClientConfig(new URL(repoUrl), username,
						password, Boolean.parseBoolean(doPrefetching),
						Boolean.parseBoolean(doAutoclear));
				ModelManager.getInstance().config(rpc);
			}
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e.getMessage(), e);
		}

	}

	@Override
	public String getRepositoryType() {
		return "TSSGRepo";
	}

	@Override
	public String getIdentity(String identityType, int id)
			throws IdentityRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdentityId(String identityType, String name)
			throws IdentityRepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateIdentity(IdentityObject io)
			throws DuplicateValueException, IdentityRepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IdentityObject> getIdentities(String identityType, int start,
			int length) throws IdentityRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdentitiesCount(String identityType)
			throws IdentityRepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateSecondIdentities(IdentitiesRelation relation)
			throws IdentityRepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IdentityObject> getSecondIdentities(IdentitiesRelation relation)
			throws IdentityRepositoryException {

		String firstIdentity = relation.getFirstIdentity();
		String firstIdentityType = relation.getFirstIdentityType();

		String secondIdentitiesType = relation.getSecondIdentitiesType();

		List<IdentityObject> ret = new ArrayList<IdentityObject>();

		if (secondIdentitiesType.equals("organisation")
				&& firstIdentityType.equals("user")) {
			// list organisations of the user

			List<Organisation> orgs = ModelManager.getInstance()
					.findOrganisationsByUserName(firstIdentity);
			for (Organisation org : orgs) {
				IdentityObject io = new IdentityObject();
				io.setName(org.getName());
				io.setType("organisation");
				ret.add(io);
			}
		} else if (secondIdentitiesType.equals("organisation")
				&& firstIdentityType.equals("resource")) {
			// list organisations of the resources
			ResourceSpec spec = ModelManager.getInstance().getResourceSpec(
					firstIdentity);
			if (spec != null) {
				String organisation = spec.getProvider();
				IdentityObject io = new IdentityObject();
				io.setName(organisation);
				io.setType("organisation");
				ret.add(io);
			}
		} else if (secondIdentitiesType.equals("ptm")
				&& firstIdentityType.equals("resource")) {
			// list organisations of the resources
			ResourceSpec spec = ModelManager.getInstance().getResourceSpec(
					firstIdentity);
			String ptmName = null;
			int pos = spec.getCommonName().indexOf('.');
			if (pos > 0) {
				ptmName = spec.getCommonName().substring(0, pos);
			}

			if (ptmName != null) {
				List<? extends Ptm> ptms = ModelManager.getInstance()
						.listPtms();
				for (Ptm ptm : ptms) {
					if (ptm.getCommonName().equals(ptmName)) {
						IdentityObject io = new IdentityObject();
						io.setName(ptmName);
						io.setType("ptm");
						ret.add(io);
						;
					}
				}
			}
		}

		return ret;
	}

	@Override
	public List<String> getSecondIdentitiesTypeList(String identityType) {

		List<String> lst = new ArrayList<String>();

		if (identityType.equals("user")) {
			lst.add("organisation");
		} else if (identityType.equals("resource")) {
			lst.add("organisation");
		} else if (identityType.equals("organisation")) {
			lst.add("user");
			lst.add("resource");
		}
		return lst;
	}
}
