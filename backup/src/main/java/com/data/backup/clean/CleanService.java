package com.data.backup.clean;

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


@Service
public class CleanService {
	@Autowired
	private JdbcTemplate jdbctemp;
	@Autowired
	private AppService appSerivce;
	
	public List<String> showSqlTables(String db) {
		List<String> tableName = jdbctemp.queryForList("SHOW TABLES IN "+db, String.class);
		return tableName;
	}
	public void deleteSqlData(ArrayList<String> tableNames) {
		List<String> name = jdbctemp.queryForList("SHOW DATABASES", String.class);
		System.out.println(name);
		if(!tableNames.isEmpty()) {
			for(String table : tableNames) {
				jdbctemp.execute("TRUNCATE TABLE "+table);
				System.out.println(table + " truncated successfully");
			}
		}else {
			System.out.println("No table name provided");
		}
	}
	public List<String> showMongoCollections(String col){
		Config config = appSerivce.getMongoHost();
		String host = "";
		if(config != null) {
		host = "mongodb://"+config.getHost();		
		}
		MongoClient mongo = MongoClients.create(host);
		MongoDatabase db = mongo.getDatabase(col);
		MongoCursor<String> collection = db.listCollectionNames().iterator();
		List<String> collName = new ArrayList<>();
		while(collection.hasNext()) {
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
}
