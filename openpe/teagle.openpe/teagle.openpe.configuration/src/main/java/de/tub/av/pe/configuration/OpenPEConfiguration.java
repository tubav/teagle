package de.tub.av.pe.configuration;


public class OpenPEConfiguration  extends ServiceConfiguration
{
	public static String OPENPE_RULE_ACTIONS="openpe.rule.actions";
	public static String OPENPE_MINIMUM_PRIORITY="openpe.priorities";
	public static String OPENPE_BASEDIR = "openpe.basedir";
	public static String OPENPE_POLICY_SCHEMAFILE = "openpe.policy.schemafile";
	public static String OPENPE_POL_DB_JNDI_URL = "openpe.pol.db.jndi.url";
	public static String OPENPE_DB_TYPE = "openpe.db.type";
	public static String OPENPE_ID_DB_TYPE = "openpe.identities.type";
	public static String OPENPE_ID_DB_JNDI_URL = "openpe.id.db.jndi.url";
	public static String ID_REPO_USERNAME = "repo.username";
	public static String ID_REPO_PASSWORD = "repo.password";
	public static String ID_REPO_URL = "repo.url";
	public static String ID_REPO_CACHE_AUTOCLEAR_ENABLED = "repo.cache.autoclear.enabled";
	public static String ID_REPO_CACHE_AUTOCLEAR_INTERVAL = "repo.cache.autoclear.interval";
	public static String ID_REPO_CACHE_PREFECTHING_ENABLED= "repo.cache.prefetching.enabled";
	
	
	public OpenPEConfiguration()
	{
		super("Open Policy Engine");
		addStringParameter(OPENPE_DB_TYPE,"DroolsMySql","The Pol DB type","");		
		addStringParameter(OPENPE_POL_DB_JNDI_URL,"jdbc/PE_JNDI","The JNDI name of the policy repository","");
		addStringParameter(OPENPE_ID_DB_TYPE,"TSSGRepo","Identities db type","");
		addStringParameter(ID_REPO_URL,"http://repos.pii.tssg.org:8080/repository/rest","Repo url","");
		addStringParameter(ID_REPO_USERNAME,"testuser","Username","");
		addStringParameter(ID_REPO_PASSWORD,"test","Password","");
		addStringParameter(ID_REPO_CACHE_AUTOCLEAR_ENABLED, "true", "Cache Autoclear", "");		
		addStringParameter(ID_REPO_CACHE_AUTOCLEAR_INTERVAL, "300000", "Cache Autoclear", "");		
		addStringParameter(ID_REPO_CACHE_PREFECTHING_ENABLED, "true", "Cache Prefetching", "");				
		addStringParameter(OPENPE_BASEDIR,"/var/lib/tomcat6/conf/openpecfg","Base Directory","The directory where the policy template resides");
	}
}


