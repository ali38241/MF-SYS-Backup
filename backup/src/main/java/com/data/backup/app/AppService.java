package com.data.backup.app;

//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;

@Service
public class AppService {
	String host = "localhost";
	int port = 27017;
	String backPath = "C:\\Users\\mmghh\\OneDrive\\Desktop\\Dump";
	
	

	public void backup(ArrayList<String> dbName) {

		dbName.stream()
        .map(db -> new ProcessBuilder("mongodump", "--db", db, "--host", host, "--port", String.valueOf(port), "--out", backPath))
        .forEach(pb -> {
            try {
                Process p = pb.start();
                int exitCode = p.waitFor();

                if (exitCode == 0) {
                    System.out.println("Backup created successfully!");
                } else {
                    System.err.println("Error creating backup!");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
	}
		
	public void restore(ArrayList<String> dbName) {
		String backPath = "C:\\Users\\mmghh\\OneDrive\\Desktop\\Dump\\";
		for(String db: dbName) {
			ProcessBuilder pb = new ProcessBuilder("mongorestore", "-d", db, backPath+db);
			try {
	            Process p = pb.start();
	            int exitCode = p.waitFor();
	            
	            if (exitCode == 0) {
	                System.out.println("Database restored successfully!");
	            } else {
	                System.err.println("Error restoring Database!");
	            }
	        } catch (IOException | InterruptedException e) {
	            e.printStackTrace();
	        }

		}
	}
	
	public Map<Integer, String> showAll() {
		MongoClient mongo = MongoClients.create();
		MongoIterable<String> list = mongo.listDatabaseNames();
		Map<Integer, String> map = new HashMap<>();
		int i = 1;
		for (String name : list) {
			map.put(i++, name);
		}
		return map;
		
	}

//-------------------------------////eido////----------------------------------------//
	
	private String dbusername = "root";
	private String dbpassword = "root";
	private String outputfile = "C:\\Users\\Windows\\Desktop\\db\\";
	
	public boolean backupDatabase(ArrayList<String> dbname) throws IOException, InterruptedException{
			
			boolean i = false;
			
			for(String x: dbname) {
				
			String command = String.format("\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\" -u%s -p%s --databases %s -r %S",
					dbusername, dbpassword, x, outputfile+x+".sql");
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			i = process.exitValue()==0;
	}
			return i;
	}

}
	
	
	
	


