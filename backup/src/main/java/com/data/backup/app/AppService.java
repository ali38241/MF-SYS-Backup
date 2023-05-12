package com.data.backup.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AppService {

	// ----------------------------------Mongo--------------------------------------------
	private int port = 27017;
	private final String backupPath = System.getProperty("user.home") + File.separator + "Downloads";
	private String backupFolderName;
	private String backupFolderPath;
	private File backupFolder;
	private String status = "";

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
//			status = "Created folder \\Backup\\Mongo in " + backupPath;
			return true;
		}
		return false;
	}

//--------------------------------Backup Mongo Databases----------------------------------
	public List<Map<String, String>> backup(ArrayList<String> dbName) {
		Config config = getMongoHost();
		List<Map<String, String>> backupList = new ArrayList<>();
		if (createBackupFolder()) {
			System.out.println("Folder created with name: " + backupFolderName + " in " + backupFolderPath);
			System.out.println(status);
		} else {
			System.out.println("Error creating folder with name: " + backupFolderName + " in " + backupFolderPath);
		}
		MongoClient mongo = MongoClients.create();
		List<String> existingDbs = mongo.listDatabaseNames().into(new ArrayList<>());
		for (String db : dbName) {
			if (!existingDbs.contains(db)) {
				System.err.println("Database " + db + " doesn't exist in db");
				continue;
			}
			Map<String, String> map = new LinkedHashMap<>();
			map.put("Database", db);
			map.put("Date", backupFolderName);
			ProcessBuilder pb = new ProcessBuilder("mongodump", "--authenticationDatabase", "admin", "--username", config.getUser(), "--password",
					config.getPass(), "--db", db, "--host", config.getHost(),
					"--port", String.valueOf(port), "--out", backupFolderPath);
			try {
				Process p = pb.start();
				int exitCode = p.waitFor();

				if (exitCode == 0) {
					System.out.println("Backup created successfully for : " + db);

				} else {
					System.err.println("Error creating backup!");
				}

				backupList.add(map);

			} catch (Exception e) {
				System.out.printf("Error creating backup for: ", db, e);

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
			return (date + " doesn't exists in " + backupPath + "\\Backup\\Mongo");
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
	public String zip(String date) throws IOException {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		File directory = new File(backupPath + "\\Backup\\Mongo\\" + date);
		if (directory.isDirectory()) {
			String zipFileName = "backup_" + date + ".zip";
			for (File subDirectory : directory.listFiles()) {
				if (subDirectory.isDirectory()) {
					for (File file : subDirectory.listFiles()) {
						FileInputStream fis = new FileInputStream(file);
						zos.putNextEntry(new ZipEntry(subDirectory.getName() + "\\" + file.getName()));
						int length;
						while ((length = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, length);
						}
						zos.closeEntry();
						fis.close();
					}
				}
			}
			zos.close();
			System.out.println("Zip file created successfully: " + zipFileName);
		} else {
			throw new IllegalArgumentException("Directory not found: " + backupPath);
		}

		zos.close();
		baos.close();

		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + "backup_" + date + ".zip\"");

		ServletOutputStream sos = response.getOutputStream();
		sos.write(baos.toByteArray());
		sos.flush();
		sos.close();

		return ("Created zip file: " + backupPath + ".zip \n");
	}

	// ---------------------------------gethost----------------------------//

	public void saveMongoHost(Config body) {
		try (Writer writer = new FileWriter(backupPath + "\\Backup\\mongo.json")) {
			Gson gson = new Gson();
			gson.toJson(body, writer);
			writer.close();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Config getMongoHost() {
		Config config = null;
		try (Reader reader = Files.newBufferedReader(Paths.get(backupPath + "\\Backup\\mongo.json"))) {
			Gson gson = new Gson();
			config = gson.fromJson(reader, Config.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}

//-------------------------------////MYSQL////----------------------------------------//

//	private String dbusername = "root";
//	private String dbpassword = "root";
	private String sqlbackUpFolderName;
	private File sqlBackupFolder;
	private String path = "";

	public String getCurrentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-YYYY__HH-mm-ss");
		LocalDateTime dateTime = LocalDateTime.now();
		return formatter.format(dateTime);
	}

	private Boolean backupFolderName() {
		sqlbackUpFolderName = getCurrentDateTime();
		path = backupPath + File.separator + "Backup" + File.separator + "Mysql" + File.separator + sqlbackUpFolderName;
		sqlBackupFolder = new File(path);

		if (!sqlBackupFolder.exists()) {
			sqlBackupFolder.mkdirs();
			return true;

		} else {
			return false;
		}
	}

//	------------------------------ backup databses-------------------------//

	public List<Map<String, String>> backupDatabase(List<String> dbname) {
		Config config = getMysqlHost();
		boolean i;
		List<Map<String, String>> backupList = new ArrayList<>();

		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
				"-u" + config.getUser(), "-p" + config.getPass(), "-e", "show databases;");
		try {
			Process p = pb.start();
			String output = new String(p.getInputStream().readAllBytes());
			String[] databases = output.split("\n");

			for (String x : dbname) {
				boolean found = false;
				for (String db : databases) {
					if (db.trim().equals(x)) {
						found = true;
						break;
					}
				}
				if (found) {
					if (backupFolderName()) {
						System.out.println(
								"Folder created successfully with name: " + sqlbackUpFolderName + " in " + path);
					}
					String command = String.format(
							"\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\" -u%s -p%s --databases %s -r %S",
							config.getUser(), config.getPass(), x, path + File.separator + x);
					Process process = Runtime.getRuntime().exec(command);
					process.waitFor();
					Map<String, String> map = new HashMap<>();
					map.put("database", x);
					map.put("Date", getCurrentDateTime());
					i = process.exitValue() == 0;
					if (i) {
						System.out.println("Backup created successfully for: " + x);
					} else {
						System.out.println("Error creating backup");
					}
					backupList.add(map);
				} else {
					System.out.println("Database does not exist: " + x);
				}
			}
		} catch (Exception e) {
			System.out.println("An error occurred while performing the backup: " + e.getMessage());
		}
		return backupList;
	}

//	-----------------------------------restore databases----------------------

	public boolean restoreDatabase(String date, ArrayList<String> dbname) {
		Config config = getMysqlHost();
		boolean i = false;
		try {
			for (String x : dbname) {
				String command = String.format(
						"\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe\" -u%s -p%s -e \"source %S\"",
						config.getUser(), config.getPass(),
						backupPath + "\\Backup\\Mysql" + File.separator + date + File.separator + x);
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
				i = process.exitValue() == 0;
			}
		} catch (Exception e) {
			System.out.println("An Error occurred While performing the backup: " + e.getMessage());
		}
		return i;
	}

//	------------------------------------- show all databases-----------------------------------

	public Map<Integer, String> viewall() {
		Config config = getMysqlHost();
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
				"-u" + config.getUser(), "-p" + config.getPass(), "-e", "show databases;");
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

//	--------------------------Zip files sql-------------------

	public void createzipfile(String date) throws IOException {
		byte[] buffer = new byte[1024];

		String zipFileName = "backup_" + "_" + date + ".zip";
		String backupFolderPath = backupPath + File.separator + "Backup" + File.separator + "Mysql" + File.separator
				+ date;

		File backupFolder = new File(backupFolderPath);
		if (!backupFolder.exists()) {
			throw new FileNotFoundException("Backup folder not found for date " + date);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);

		File[] filesToZip = backupFolder.listFiles();
		for (File file : filesToZip) {
			FileInputStream fis = new FileInputStream(file);
			zos.putNextEntry(new ZipEntry(file.getName()));
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
			fis.close();
		}

		zos.close();
		byte[] zipBytes = baos.toByteArray();
		baos.close();

		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
		response.setContentLength(zipBytes.length);

		OutputStream os = response.getOutputStream();
		os.write(zipBytes);
		os.flush();
		os.close();
	}

//	-------------------------- Show All backup Databases-----------------

	public Map<String, List<String>> getBackupFileNames(String foldername) throws FileNotFoundException {
		Map<String, List<String>> map = new HashMap<>();
		File folder = new File(backupPath + "\\Backup\\Mysql");
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

	public void saveMysqlHost(Config body) {
		Gson gson = new Gson();
		try (Writer writer = new FileWriter(backupPath + "\\Backup\\mysql.json")) {
			gson.toJson(body, writer);
			writer.close();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Config getMysqlHost() {
		Config config = null;
		try (Reader reader = Files.newBufferedReader(Paths.get(backupPath + "\\Backup\\mysql.json"))) {
			Gson gson = new Gson();
			config = gson.fromJson(reader, Config.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}
}
