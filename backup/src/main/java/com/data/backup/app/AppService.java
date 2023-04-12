package com.data.backup.app;

//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.zip.ZipOutputStream;
import java.util.Map;

//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;

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
		for (String db : dbName) {
			ProcessBuilder pb = new ProcessBuilder("mongodump", "--db", db, "--host", host, "--port",
					String.valueOf(port), "--out", backPath);
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
		}
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

//-------------------------------////MYSQL////----------------------------------------//
	
	
	// -----------------------------SQL-------------------------//
	
	private String dbusername = "root";
	private String dbpassword = "root";
	private String outputfile = "C:\\Users\\Windows\\Desktop\\db\\";
	
	
//	------------------------------ backupdatabses-------------------------//
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

	
	
//	-----------------------------------restire databases----------------------
	
	public boolean restoreDatabase(ArrayList<String> dbname) throws IOException, InterruptedException{
		boolean i = false;
		for(String x: dbname) {
			
			String command = String.format("\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\" -u%s -p%s -e \"source %S\"",
					dbusername, dbpassword, outputfile+x+".sql");
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			i = process.exitValue()==0;
			
		}
		return i;
	}

	

//	------------------------------------- show all databases-----------------------------------
	
	public Map<Integer, String> viewall() {
	    ProcessBuilder pb = new ProcessBuilder(
	        "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
	        "-u" + dbusername,
	        "-p" + dbpassword,
	        "-e",
	        "show databases;"
	    );
	    Map<Integer, String> result = new HashMap<>();
	    try {
	        Process p = pb.start();
	        String output = new String(p.getInputStream().readAllBytes());
	        String[] lines = output.split("\n");
	        int i =1;
	        for (String line : lines) {
	            String[] parts = line.split("\t");
	            if (parts.length > 0) {
	                result.put(i++, parts[0]);
	            }
	        }
	        int exitCode = p.waitFor();

	        if (exitCode == 0) {
	            System.out.println("Shown.");
	        } else {
	            System.err.println("Error showing");
	        }
	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	    }
	    System.out.print(result);
	    return result;
	}



}
	
	
	
	


