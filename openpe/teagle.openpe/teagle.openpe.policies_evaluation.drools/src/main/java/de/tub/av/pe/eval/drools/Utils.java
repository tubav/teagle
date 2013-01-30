package de.tub.av.pe.eval.drools;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.identities.IdentitiesRelation;
import de.tub.av.pe.identities.IdentityObject;
import de.tub.av.pe.identities.IdentityRepositoryException;
import de.tub.av.pe.identities.IdentityRepositoryManager;

public class Utils {
	private static IdentityRepositoryManager idrepoMng;
	private static Logger log = LoggerFactory.getLogger(Utils.class);

	public static void setIdentitiesRepository(
			IdentityRepositoryManager idrepomng) {
		idrepoMng = idrepomng;
	}

	public static String getOrganisation(String user) {
		IdentitiesRelation relation = new IdentitiesRelation();
		relation.setFirstIdentity(user);
		relation.setFirstIdentityType("user");
		relation.setSecondIdentitiesType("organisation");
		if (idrepoMng != null)
			try {
				List<IdentityObject> list = idrepoMng.getInstance()
						.getSecondIdentities(relation);
				if (list.size() > 0) {
					return list.get(0).getName();
				}
			} catch (IdentityRepositoryException e) {
				log.error(e.getMessage(), e);
			}
		return "";
	}

	public static String getPTMDomain(String resource) {
		IdentitiesRelation relation = new IdentitiesRelation();
		relation.setFirstIdentity(resource);
		relation.setFirstIdentityType("resource");
		relation.setSecondIdentitiesType("ptm");
		if (idrepoMng != null)
			try {
				List<IdentityObject> list = idrepoMng.getInstance()
						.getSecondIdentities(relation);
				if (list.size() > 0) {
					return list.get(0).getName();
				}
			} catch (IdentityRepositoryException e) {
				log.error(e.getMessage(), e);
			}
		return "";
	}

}
