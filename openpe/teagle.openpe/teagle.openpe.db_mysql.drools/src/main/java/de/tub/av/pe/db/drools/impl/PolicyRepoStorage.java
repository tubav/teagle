package de.tub.av.pe.db.drools.impl;


/**
 * Implementation of the Interface DBContext for a mySql database. For performance the implementation is using a pool of mysql connections.
 * 
 * @author ibo
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author ibo
 *
 */

public class PolicyRepoStorage {

	private  final Logger log = LoggerFactory.getLogger(PolicyRepoStorage.class);

	private  DataSource ds;


	public PolicyRepoStorage(DataSource datasource) {

		if (datasource == null)
			throw new IllegalArgumentException("DataSource MUST NOT be null!");

		this.ds = datasource;
	}
	
	/**
	 * Modifications:	
	 * 	- from type to scope
	 * 	- adding the priority flag
	 */	
	public String SQL_GET_POLICY = "SELECT * FROM drlpolicies WHERE identity=? AND idtype = ? AND scope=? AND event=? AND priority=?";
	public String SQL_GET_POLICY_BY_ID = "SELECT * FROM drlpolicies WHERE id=?";
	public String SQL_GET_POLICY_IDENTIFIER_BY_ID = "SELECT identity, idtype, scope, event, priority FROM drlpolicies WHERE id=?";
	public String SQL_LIST_POLICIES = "SELECT * FROM drlpolicies WHERE identity=? AND idtype = ? AND scope=? AND event=? AND priority=?";
	public String SQL_LIST_POLICIES_BY_TYPE = "SELECT * FROM drlpolicies WHERE idtype = ?";
	public String SQL_LIST_POLICIES_BY_ALL = "SELECT * FROM drlpolicies WHERE identity LIKE ? AND idtype LIKE ? AND scope LIKE ? AND event LIKE ? AND priority LIKE ?";
	public String SQL_COUNT_POLICIES_BY_TYPE = "SELECT count(*) FROM drlpolicies WHERE idtype = ?";
	
	public String SQL_LIST_POLICIES_IDF_BY_TYPE = "SELECT id, identity, idtype, scope, event, priority FROM drlpolicies WHERE idtype = ?";	
	public String SQL_LIST_POLICIES_IDF_BY_TYPE_WITH_LIMIT = "SELECT id, identity, idtype,scope, event, priority FROM drlpolicies WHERE idtype = ? LIMIT ?,?";	
	public String SQL_ADD_POLICY = "INSERT INTO drlpolicies (identity, idtype, scope, event, priority, policy) VALUES (?,?,?,?,?,?)";
	public String SQL_DELETE_POLICY_BY_ID = "DELETE FROM drlpolicies WHERE id=?";
	public String SQL_DELETE_POLICY = "DELETE FROM drlpolicies WHERE identity=? AND idtype = ? AND scope=? AND event=? AND priority = ?";
	public String SQL_UPDATE_POLICY = "UPDATE drlpolicies SET identity=?, idtype=?, scope=?, event=?, priority = ?, policy=? WHERE id=?";

	private enum QueryType {
		SELECT, DELETE, INSERT, UPDATE
	};

	public RowList select(String query, Object[] args) throws SQLException
	{
		return doQuery(QueryType.SELECT, query, args);
	}
	public Row select1(String query, Object[] args) throws SQLException
	{
			RowList rowList = doQuery(QueryType.SELECT, query, args);
			if(rowList.size() < 1)
				return null;
			return rowList.get(0);	
	}

	public void delete(String query, Object[] args) throws SQLException{
			doQuery(QueryType.DELETE, query, args);
	}
	
	public void update(String query, Object[] args) throws SQLException{
			doQuery(QueryType.UPDATE, query, args);		
	}
	
	public String insert(String query, Object[] args) throws SQLException{
			RowList rowList = doQuery(QueryType.INSERT, query, args);
			if(rowList.size() < 1)
				return null;
			return ((Long)rowList.get(0).get("GENERATED_KEY")).toString();
	}

	private RowList doQuery(QueryType queryType, String query, Object[] args)
	throws SQLException {
		RowList rowList;

		Connection conn = null;
		PreparedStatement prepstm = null;
		ResultSet resultSet = null;
		try {
			conn = getConnection();
			switch (queryType) {
			case SELECT:
				prepstm = conn.prepareStatement(query);
				for (int i = 0; i < args.length; i++)
					prepstm.setObject(i + 1, args[i]);
				resultSet = prepstm.executeQuery();
				rowList = new RowList();
				while (resultSet.next()) {
					Row row = new Row();
					ResultSetMetaData metadata = resultSet.getMetaData();
					for (int i = 1; i <= metadata.getColumnCount(); i++) {
						row.put(metadata.getColumnName(i),
								resultSet.getObject(i));
					}
					rowList.add(row);
				}
				return rowList;
			case INSERT:
				prepstm = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < args.length; i++)
					prepstm.setObject(i + 1, args[i]);
				prepstm.execute();
				resultSet = prepstm.getGeneratedKeys();				
				rowList = new RowList();
				if(resultSet.first())
				{
					Row row = new Row();
					ResultSetMetaData md = resultSet.getMetaData();
					for (int i=1; i<=md.getColumnCount(); i++)
						row.put(md.getColumnLabel(i), resultSet.getObject(i));				
					rowList.add(row);
				}else 
				{
					log.error("failed to retrieve generated key");
				}
				return rowList;
			case UPDATE:
				prepstm = conn.prepareStatement(query);
				for (int i = 0; i < args.length; i++)
					prepstm.setObject(i + 1, args[i]);
				prepstm.execute();
				break;				
			case DELETE:
				prepstm = conn.prepareStatement(query);
				for (int i = 0; i < args.length; i++)
					prepstm.setObject(i + 1, args[i]);
				prepstm.execute();
				break;
			default:
				break;
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (prepstm != null)
				prepstm.close();
			if (conn != null)
				conn.close();
		}
		return null;
	}

	private Connection getConnection() throws SQLException
	{
		//Old version
		//return DriverManager.getConnection (props.getProperty("mysql.url"), props.getProperty("mysql.username"), props.getProperty("mysql.password"));
		return ds.getConnection();
	}
		

}
