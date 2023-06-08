package com.data.backup.clean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.data.backup.app.AppService;
import com.data.backup.app.Config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class CleanService {
	@Autowired
	private JdbcTemplate jdbctemp;

	@PersistenceContext
	private EntityManager entityManager;


	public List<String> showSqlTables(String db) {
		List<String> tableName = jdbctemp.queryForList("SHOW TABLES IN " + db, String.class);
		return tableName;
	}

	public void deleteSqlData(ArrayList<String> tableNames) {
		List<String> name = jdbctemp.queryForList("SHOW DATABASES", String.class);
		System.out.println(name);
		if (!tableNames.isEmpty()) {
			for (String table : tableNames) {
				jdbctemp.execute("TRUNCATE TABLE " + table);
				System.out.println(table + " truncated successfully");
			}
		} else {
			System.out.println("No table name provided");
		}
	}


	public List<String> showMongoTables(String col) {
		MongoClient mongo = MongoClients.create();
		MongoDatabase db = mongo.getDatabase(col);
		MongoCursor<String> collection = db.listCollectionNames().iterator();
		List<String> collName = new ArrayList<>();
		while (collection.hasNext()) {
			collName.add(collection.next());
		}

		return collName;

	}

	
	public void clean(String requiredOrganization, String dbName) {
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase database = client.getDatabase(dbName);
			for (String collName : database.listCollectionNames()) {
				MongoCollection<Document> collection = database.getCollection(collName);
				for (Document obj : collection.find()) {
					String orgacode = obj.getString("POR_ORGACODE");
					if (orgacode != null && orgacode.equals(requiredOrganization)) {
						System.out.println(database + " DB -> Deleting records of " + orgacode + " From collection ["
								+ collName + "] having ID --> [" + obj.get("_id") + "]");
						collection.deleteOne(obj);
					}
				}
			}
		}
	}


	@Transactional
	public void deleteSpecialRow(String databaseName, String organizationCode) {
		entityManager.createNativeQuery("USE " + databaseName).executeUpdate();


		String columnName = "POR_ORGACODE";
		String valueToDelete = organizationCode;

		try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/" + databaseName, "root",
				"root")) {
			com.mysql.cj.jdbc.DatabaseMetaData metadata = (com.mysql.cj.jdbc.DatabaseMetaData) connection.getMetaData();
			ResultSet tableResultSet = metadata.getTables(databaseName, null, null, new String[] { "TABLE" });
			Statement disableConstraintsStmt = connection.createStatement();
			disableConstraintsStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
			while (tableResultSet.next()) {
				System.out.println(tableResultSet);
				String tableName = tableResultSet.getString("TABLE_NAME");
				if (columnExists(connection, tableName, columnName)) {
					String deleteQuery = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";
					PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
					preparedStatement.setString(1, valueToDelete);
					preparedStatement.executeUpdate();

					System.out.println("Selected rows deleted successfully from table: " + tableName);
				} else {
					System.out.println("Column does not exist in the table: " + tableName);
				}
				disableConstraintsStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean columnExists(Connection connection, String tableName, String columnName)
			throws SQLException {
		com.mysql.cj.jdbc.DatabaseMetaData metadata = (com.mysql.cj.jdbc.DatabaseMetaData) connection.getMetaData();
		ResultSet resultSet = metadata.getColumns(null, null, tableName, columnName);
		return resultSet.next();
	}
	
	
	
	@Transactional
	public void deleteRestOfRow(String databaseName, String organizationCode) {
	    entityManager.createNativeQuery("USE " + databaseName).executeUpdate();

	    String columnName = "POR_ORGACODE";
	    String valueToKeep = organizationCode;

	    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/" + databaseName, "root", "root")) {
	        com.mysql.cj.jdbc.DatabaseMetaData metadata = (com.mysql.cj.jdbc.DatabaseMetaData) connection.getMetaData();
	        ResultSet tableResultSet = metadata.getTables(databaseName, null, null, new String[] { "TABLE" });
	        Statement disableConstraintsStmt = connection.createStatement();
	        disableConstraintsStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
	        while (tableResultSet.next()) {
	            System.out.println(tableResultSet);
	            String tableName = tableResultSet.getString("TABLE_NAME");
	            if (columnExists(connection, tableName, columnName)) {
	                String deleteQuery = "DELETE FROM " + tableName + " WHERE " + columnName + " != ?";
	                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
	                preparedStatement.setString(1, valueToKeep);
	                preparedStatement.executeUpdate();

	                System.out.println("Data deleted successfully from table: " + tableName);
	            } else {
	                System.out.println("Column does not exist in the table: " + tableName);
	            }
	        }
	        disableConstraintsStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	


}
