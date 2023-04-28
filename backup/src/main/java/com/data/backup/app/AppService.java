package com.data.backup.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;


@Service
public class AppService {

	// ----------------------------------Mongo--------------------------------------------
	String host = "localhost";
	int port = 27017;
	String backPath = "C:\\Users\\mmghh\\OneDrive\\Desktop\\MongoBackup";
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	LocalDate date = LocalDate.now();
	String backupFolderName = dtf.format(date);
	String backupFolderPath = backPath + "\\" + backupFolderName;
	File backupFolder = new File(backupFolderPath);

//--------------------------------Backup Mongo Databases----------------------------------
	public List<Map<String, String>> backup(ArrayList<String> dbName) {
	    List<Map<String, String>> backupList = new ArrayList<>();
		boolean success = backupFolder.mkdir();
		if (!success) {
			System.out.println("Folder already exists with name: " + backupFolderName + " in " + backPath
					+ ". Not created new one.");
		} else {
			System.out.println("Folder created with name: " + backupFolderName + " in " + backPath);
		}

		for (String db : dbName) {
            Map<String, String> map = new HashMap<>();
			map.put("Database", db);
			map.put("Date", backupFolderName);
			File backupFile = new File(backupFolderPath + "\\" + db);
			if (backupFile.exists()) {
				String message = String.format("A backup for database \"%s\" already exists at \"%s\". Updating Backup.",
						db,
						backupFolderPath);

				System.out.println(message);
//				continue;
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
				backupList.add(map);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return backupList;
	}

	// -----------------------------Restore Mongo Databases----------------------
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

	// ------------------------------Display All Mongo
	// Databases----------------------
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
	//-----------------Mongo-zip-download----------------------------
	public String zip(List<String> folderNames) throws IOException {
	    byte[] buffer = new byte[1024];
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos);
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
	            throw new IllegalArgumentException(backupFolderPath + "\\" + folderName + " Does not exists.");
	        }
	    }
	    zos.close();
	    baos.close();

	    // Set the response headers
	    HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + backupFolderName + ".zip\"");

	    // Write the content of the generated zip file to the response output stream
	    ServletOutputStream sos = response.getOutputStream();
	    sos.write(baos.toByteArray());
	    sos.flush();
	    sos.close();

	    return ("Created zip file: " + backupFolderPath + ".zip \n" + "Files added to the zip: " + folderNames);
	}
//-------------------------------////MYSQL////----------------------------------------//
	
	
	// -----------------------------SQL-------------------------//
	
	private String dbusername = "root";
	private String dbpassword = "root";
	private String outputfile = "C:\\Users\\Windows\\Desktop\\mysqlbackup";
	DateTimeFormatter sqldtf = DateTimeFormatter.ofPattern("dd-MM-YYYY");
	LocalDate sqldate = LocalDate.now();
	String sqlbackUpFolderName = sqldtf.format(sqldate);
	String path = outputfile + "\\" + sqlbackUpFolderName;
	File sqlbackupFolder = new File(path);
	
	
//	------------------------------ backup databses-------------------------//

	public List<Map<String, String>> backupDatabase(List<String> dbname) throws IOException, InterruptedException{
			boolean i = false;
		    List<Map<String, String>> backupList = new ArrayList<>();
			boolean success = sqlbackupFolder.mkdir();
			if(!success) {
				System.out.println("folder already exist with name: " + sqlbackUpFolderName);
			}else {
				System.out.println("folder created successfully with name:" + sqlbackUpFolderName);
			}
			for(String x: dbname) {
				String outputfilename = path + x + ".sql";
				File filename = new File(outputfilename);
				if(filename.exists()) {
					System.out.println(outputfilename +" already exists");
				}else {
			String command = String.format("\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\" -u%s -p%s --databases %s -r %S",
					dbusername, dbpassword, x, path+"\\"+x+".sql");
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
            Map<String, String> map = new HashMap<>();
			map.put("database", x);
			map.put("Date", sqlbackUpFolderName);
			i = process.exitValue()==0;
			if (i) {
				System.out.println("Backup Created successfully for: " + x);
			}else {
				System.out.println("Error creating backup");
			}
			backupList.add(map);
				}
	}
	return backupList;
	}

	
	
//	-----------------------------------restore databases----------------------
	
	public boolean restoreDatabase(ArrayList<String> dbname) throws IOException, InterruptedException{
		boolean i = false;
		for(String x: dbname) {
			
			String command = String.format("\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\" -u%s -p%s -e \"source %S\"",
					dbusername, dbpassword, path+"\\"+x+".sql");
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
	                result.put(i++, parts[0].replaceAll("\r", ""));
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

	
//	--------------------------Zip files-------------------
	public void createzipfile(List<String> filenames) throws IOException {
	    byte[] buffer = new byte[1024];
	    boolean hasFile = false;
	    for (String filename : filenames) {
	        File file = new File(path + "\\" + filename + ".sql");
	        if (file.exists()) {
	            hasFile = true;
	            break;
	        }
	    }
	    if (!hasFile) {
	        System.out.println("No files to zip");
	        return;
	    }
	    LocalDate ld = LocalDate.now();
	    String x = "\\"+ "backup_" + ld.toString() + "_" + System.currentTimeMillis() + ".zip";
//	    String zipFilename = path + x;


	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos);
	    for (String filename : filenames) {
	        File file = new File(path + "\\" + filename + ".sql");
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
	    byte[] zipBytes = baos.toByteArray();
	    baos.close();
	    HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + x + "\"");
	    response.setContentLength(zipBytes.length);

	    OutputStream os = response.getOutputStream();
	    os.write(zipBytes);
	    os.flush();
	    os.close();
	}



}
	
	
	
	


