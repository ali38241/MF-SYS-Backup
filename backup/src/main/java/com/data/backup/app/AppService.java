package com.data.backup.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;

@Service
public class AppService {
	String host = "localhost";
	int port = 27017;
	String backPath = "C:\\Users\\mmghh\\OneDrive\\Desktop\\MongoBackup";
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyy");
	LocalDate date = LocalDate.now();
	String backupFolderName = dtf.format(date);
	String backupFolderPath = backPath + "\\" + backupFolderName;
	File backupFolder = new File(backupFolderPath);

	public void backup(ArrayList<String> dbName) {

		boolean success = backupFolder.mkdir();
		if (!success) {
			System.out.println("Folder already exists with name: " + backupFolderName + " in " + backPath
					+ ". Not created new one.");
		} else {
			System.out.println("Folder created with name: " + backupFolderName + " in " + backPath);
		}

		for (String db : dbName) {
			File backupFile = new File(backupFolderPath + "\\" + db);
			if (backupFile.exists()) {
				String message = String.format("A backup for database \"%s\" already exists at \"%s\". Skipping backup",
						db,
						backupFolderPath);

				System.out.println(message);
				continue;
			}
			ProcessBuilder pb = new ProcessBuilder("mongodump", "--db", db, "--host", host,
					"--port",
					String.valueOf(port), "--out", backupFolderPath);
			try {
				Process p = pb.start();
				int exitCode = p.waitFor();
				if (exitCode == 0) {
					System.out.println("Backup created successfully for : " + db);

				} else {
					System.err.println("Error creating backup!");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
		
	public void restore(ArrayList<String> dbName) {
		for(String db: dbName) {
			ProcessBuilder pb = new ProcessBuilder("mongorestore", "-d", db, backupFolderPath + "\\" + db);
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

	public boolean zip(List<String> folderNames) throws IOException {
		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(backupFolderPath + ".zip");
		ZipOutputStream zos = new ZipOutputStream(fos);
		for (String folderName : folderNames) {
			File directory = new File(backupFolderPath + "\\" + folderName);
			if (directory.isDirectory()) {
				for (File file : directory.listFiles()) {
	                System.out.println("Adding file " + file.getName() + " to zip");
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(folderName + "/" + file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			} else {
				throw new IllegalArgumentException(backupFolderPath + folderName + " is not a directory");
			}
		}
		zos.close();
		fos.close();
		System.out.println("Created zip file: " + backupFolderPath + ".zip");
		System.out.println("Files added to the zip: " + folderNames);
		return true;
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
				
				String outputfilename = outputfile + x + ".sql";
				File filename = new File(outputfilename);
				if(filename.exists()) {
					System.out.println(outputfilename +" already exists");
				}else {
				
			String command = String.format("\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\" -u%s -p%s --databases %s -r %S",
					dbusername, dbpassword, x, outputfile+x+".sql");
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			i = process.exitValue()==0;
				}
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

	
//	--------------------------zip files-------------------
	
	
	
	public boolean createzipfile(List<String> filenames) throws IOException {
	    String zipFolder = "C:\\Users\\Windows\\Desktop\\db\\";
	    File zipFolderFile = new File(zipFolder);
	    if (!zipFolderFile.exists()) {
	        zipFolderFile.mkdirs();
	    }

	    LocalDate ld = LocalDate.now();
	    String zipFilename = zipFolder + "backup_" + ld.toString() + "_" + System.currentTimeMillis() + ".zip";

	    byte[] buffer = new byte[1024];
	    boolean hasFile = false;
	    for (String filename : filenames) {
	        File file = new File(zipFolder + filename + ".sql");
	        if (file.exists()) {
	            hasFile = true;
	            break;
	        }
	    }
	    if (!hasFile) {
	        System.out.println("No files to zip");
	        return false;
	    }

	    FileOutputStream fos = new FileOutputStream(zipFilename);
	    ZipOutputStream zos = new ZipOutputStream(fos);
	    for (String filename : filenames) {
	        File file = new File(zipFolder + filename + ".sql");
	        if (file.exists()) {
	            FileInputStream fis = new FileInputStream(file);
	            zos.putNextEntry(new ZipEntry(filename + ".sql"));
	            int length;
	            while ((length = fis.read(buffer)) > 0) {
	                zos.write(buffer, 0, length);
	            }
	            zos.closeEntry();
	            fis.close();
	        } else {
	            System.out.println("File " + filename + ".sql" + " does not exist");
	        }
	    }

	    zos.close();
	    fos.close();
	    return true;
	}



}
	
	
	
	


