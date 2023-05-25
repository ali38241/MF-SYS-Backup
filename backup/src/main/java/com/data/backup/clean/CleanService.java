package com.data.backup.clean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;


@Service
public class CleanService {
	@Autowired
	private JdbcTemplate jdbctemp;
	
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
	public List<String> showMongoTables(String col){
		MongoClient mongo = MongoClients.create();
		MongoDatabase db = mongo.getDatabase(col);
		MongoCursor<String> collection = db.listCollectionNames().iterator();
		List<String> collName = new ArrayList<>();
		while(collection.hasNext()) {
			collName.add(collection.next());
		}
		
		return collName;
		
	}
}
