package com.data.backup.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
	private String host = "localhost";
	int port = 27017;
	String home = System.getProperty("user.home");
	private String backupPath = home + File.separator + "Downloads";
	private String backupFolderName;
	private String backupFolderPath;
	private File backupFolder;
	String status = "";
	
	private String getBackupName() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy__HH-mm-ss");
		return dtf.format(LocalDateTime.now());
	}

	public Boolean createBackupFolder() {

		backupFolderName = getBackupName();
		backupFolderPath = backupPath + "\\Backup\\Mongo" + File.separator + backupFolderName;
		backupFolder = new File(backupFolderPath);
		if (!backupFolder.exists()) {
			backupFolder.mkdir();
			status = "Created folder \\Backup\\Mongo in " + backupPath;
			return true;
		}
		return false;
	}

//--------------------------------Backup Mongo Databases----------------------------------
	public List<Map<String, String>> backup(ArrayList<String> dbName) {
		List<Map<String, String>> backupList = new ArrayList<>();
		if (createBackupFolder()) {
			System.out.println("Folder created with name: " + backupFolderName + " in " + backupFolderPath);
			System.out.println(status);
		}else {
			System.out.println("Error creating folder with name: " + backupFolderName + " in " + backupFolderPath);
		}

		for (String db : dbName) {
			Map<String, String> map = new HashMap<>();
			map.put("Database", db);
			map.put("Date", backupFolderName);
			ProcessBuilder pb = new ProcessBuilder("mongodump", "--db", db, "--host", host, "--port",
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
	public String restore(String date, ArrayList<String> dbName) {
		String path = backupPath + "\\Backup\\Mongo" + File.separator + date;
		String result = "";
		File file = new File(path);
		if (file.exists()) {
			for (String db : dbName) {
				ProcessBuilder pb = new ProcessBuilder("mongorestore", "-d", db, path + File.separator + db);
				try {
					Process p = pb.start();
					int exitCode = p.waitFor();

					if (exitCode == 0) {
						System.out.println("Database restored successfully for : " + db);
						result += ("Database restored successfully!:" + db + "\r\n");
					} else {
						System.out.println("Error restoring Database!" + db);
						result = "Error restoring Database!";
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			return result;

		} else {
			return (date + " doesn't exists in " + backupPath);
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

//	----------------------Show backup on disk---------------------
	public Map<String, List<String>> showBackup(String date) {
		File directory = new File(backupPath + "\\Backup\\Mongo");
		Map<String, List<String>> map = new HashMap<>();

		if (!directory.exists() || !directory.isDirectory()) {
			System.out.println("Folder doesn't exist");
			return Collections.emptyMap();
		}

		File[] contents = directory.listFiles();

		if (contents == null) {
			System.out.println("Directory is empty");
			return Collections.emptyMap();
		}

		for (File file : contents) {
			if (file.isDirectory() && file.getName().startsWith(date)) {
				List<String> backupList = new ArrayList<>();
				File[] subDirectories = file.listFiles();
				for (File subDirectory : subDirectories) {
					if (subDirectory.isDirectory() && !subDirectory.isFile()) {
						backupList.add(subDirectory.getName());
						System.out.println(subDirectory.getName());
					}
				}
				map.put(file.getName(), backupList);
			}
		}

		if (map.isEmpty()) {
			System.out.println("No backup exists with name " + date);
			return null;
		}
		return map;
	}

	// -----------------Mongo-zip-download----------------------------
	public String zip(String date, List<String> folderNames) throws IOException {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		for (String folderName : folderNames) {
			File directory = new File(
					backupPath + "\\Backup\\Mongo" + File.separator + date + File.separator + folderName);
			if (directory.isDirectory()) {
				for (File file : directory.listFiles()) {
					System.out.println("Adding file " + file.getName() + " to zip");
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(folderName + File.separator + file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			} else {
				throw new IllegalArgumentException(
						backupPath + "\\Backup\\Mongo" + File.separator + folderName + " Does not exists.");
			}
		}
		zos.close();
		baos.close();

		// Set the response headers
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + date + ".zip\"");

		// Write the content of the generated zip file to the response output stream
		ServletOutputStream sos = response.getOutputStream();
		sos.write(baos.toByteArray());
		sos.flush();
		sos.close();

		return ("Created zip file: " + backupPath + ".zip \n" + "Files added to the zip: " + folderNames);
	}
//-------------------------------////MYSQL////----------------------------------------//

	// -----------------------------SQL-------------------------//

	private String dbusername = "root";
	private String dbpassword = "root";

	private String outputfile = "C:\\Users\\Windows\\Desktop\\mysqlbackup";
//	private String outputPath = System.getProperty("user.home")+File.separator+"Downloads"+File.separator+"sqlbackup";
	String sqlbackUpFolderName;
//	String path = outputfile + "\\" + sqlbackUpFolderName;
	File sqlBackupFolder;

	public String getCurrentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-YYYY__HH-mm-ss");
		LocalDateTime dateTime = LocalDateTime.now();
		return formatter.format(dateTime);
	}

	public Boolean backupFolderName() {
		sqlbackUpFolderName = getCurrentDateTime();
		String path = outputfile + File.separator + sqlbackUpFolderName;
		sqlBackupFolder = new File(path);

		if (!sqlBackupFolder.exists()) {
			sqlBackupFolder.mkdir();
			return false;
		} else {
			return true;
		}
	}

//	------------------------------ backup databses-------------------------//

	public List<Map<String, String>> backupDatabase(List<String> dbname) throws IOException, InterruptedException {
		boolean i = false;
		List<Map<String, String>> backupList = new ArrayList<>();
		if (backupFolderName()) {
			System.out.println("folder already exist with name: " + getCurrentDateTime());
		}
//			Map<String, String> map = new HashMap<>();
			boolean success = sqlBackupFolder.mkdir();
			if (!success) {
				System.out.println("folder already exist with name: " + sqlbackUpFolderName);

			} else {
				System.out.println("folder created successfully with name:" + getCurrentDateTime());
			}
			for (String x : dbname) {
				String outputfilename = outputfile + File.separator + x + ".sql";
				File filename = new File(outputfilename);
				if (filename.exists()) {
					System.out.println(outputfilename + " already exists");
				} else {
					String command = String.format(
							"\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\" -u%s -p%s --databases %s -r %S",
							dbusername, dbpassword, x,
							outputfile + File.separator + sqlbackUpFolderName + "\\" + x + ".sql");
					Process process = Runtime.getRuntime().exec(command);
					process.waitFor();
					Map<String, String> map = new HashMap<>();
					map.put("database", x);
					map.put("Date", getCurrentDateTime());
					i = process.exitValue() == 0;
					if (i) {
						System.out.println("Backup Created successfully for: " + x);
					} else {
						System.out.println("Error creating backup");
					}
					backupList.add(map);
				}
			}
			return backupList;

		}
	

//	------------------------------ backup databses-------------------------//

//	-----------------------------------restore databases----------------------

	public boolean restoreDatabase(String date, ArrayList<String> dbname) throws IOException, InterruptedException {
		boolean i = false;
		for (String x : dbname) {
			String command = String.format(
					"\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\" -u%s -p%s -e \"source %S\"",
					dbusername, dbpassword, outputfile + File.separator + date + File.separator + x + ".sql");
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			i = process.exitValue() == 0;
		}
		return i;
	}

//	------------------------------------- show all databases-----------------------------------

	public Map<Integer, String> viewall() {
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
				"-u" + dbusername, "-p" + dbpassword, "-e", "show databases;");
		Map<Integer, String> result = new HashMap<>();
		try {
			Process p = pb.start();
			String output = new String(p.getInputStream().readAllBytes());
			String[] lines = output.split("\n");
			int i = 1;
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
	public void createzipfile(String date, List<String> filenames) throws IOException {
		byte[] buffer = new byte[1024];
		boolean hasFile = false;
		for (String filename : filenames) {
			File file = new File(outputfile + File.separator + date + File.separator + filename + ".sql");
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
		String x = "\\" + "backup_" + ld.toString() + "_" + date + ".zip";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		for (String filename : filenames) {
			File file = new File(outputfile + File.separator + date + "\\" + filename + ".sql");
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
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + x + "\"");
		response.setContentLength(zipBytes.length);

		OutputStream os = response.getOutputStream();
		os.write(zipBytes);
		os.flush();
		os.close();
	}

//	-------------------------- Show All backup Databases----------

	public Map<String, List<String>> getBackupFileNames(String foldername) throws FileNotFoundException {
		Map<String, List<String>> map = new HashMap<>();
		File folder = new File(outputfile);
		if (!folder.exists()) {
			throw new FileNotFoundException("Folder " + foldername + " does not exist");
		}
		File[] dateFolders = folder.listFiles();
		if (dateFolders == null) {
			throw new FileNotFoundException("No folders found in folder " + foldername);
		}
		for (File dateFolder : dateFolders) {
			if (dateFolder.isDirectory() && dateFolder.getName().startsWith(foldername)) {
				File[] backupFiles = dateFolder.listFiles();
				if (backupFiles != null) {
					List<String> backupFileNames = new ArrayList<>();
					for (File backupFile : backupFiles) {
						backupFileNames.add(backupFile.getName());
					}
					map.put(dateFolder.getName(), backupFileNames);
				}
			}
		}
		if (map.isEmpty()) {
			throw new FileNotFoundException("No backup files found in folder " + foldername);
		}
		return map;
	}

}
