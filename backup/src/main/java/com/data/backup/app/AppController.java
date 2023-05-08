package com.data.backup.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class AppController {

	private AppService appService;

	public AppController(AppService appService) {
		this.appService = appService;
	}
	

	@GetMapping("/mongo/backup/{dbName}")
	public List<Map<String, String>> backUpMultiple(@PathVariable ArrayList<String> dbName) {
		return appService.backup(dbName);
	}
	@GetMapping("/mongo/restore/{date}/{dbName}")
	public String restore(@PathVariable String date, @PathVariable ArrayList<String> dbName) {
		return appService.restore(date, dbName);
	}

	@GetMapping("/mongo/showAll")
	public Map<Integer, String> showAll() {
		return appService.showAll();
	}

	@GetMapping("/mongo/showBackup/{date}")
	public Map<String, List<String>> showBackup(@PathVariable String date) {
		return appService.showBackup(date);
	}

	@GetMapping("/mongo/zip/{date}/{dbName}")
	public String zip(@PathVariable String date,@PathVariable List<String> dbName) throws IOException {
		return appService.zip(date, dbName);
//		appService.createzipfile(dbName);
	}

//---------------------MYSQL-----------------------------------


//	----------------------Backup DataBase----------------------

	@GetMapping("/sql/getbackup/{dbname}")
	public List<Map<String, String>> backupDatabase(@PathVariable List<String> dbname) {
		List<Map<String, String>> map = appService.backupDatabase(dbname);
		return map;

	}

//	----------------------------------------- Restore DataBase----------------------------------

	@GetMapping("/sql/restore/{date}/{dbname}")
	public String restoreDatabase(@PathVariable String date,@PathVariable ArrayList<String> dbname){
		boolean success = appService.restoreDatabase(date,dbname);
		if(success) {
			String x = "restore successfully done";
			return x;
			
		}else {
			return "Did not restore databases";
		}
		
	}

	
//	-------------------------- Showing all Databases--------------------
	 @GetMapping("/sql/alldatabases")
		public Map<Integer, String> showall() {
	        Map<Integer, String> result = appService.viewall();
			return result;
	    }
	 
//	 ---------------------------Database Zip files----------------
	 
	 @GetMapping("/sql/createzip/{date}")
	 public void createzipfiles(@PathVariable String date) throws IOException{
		 appService.createzipfile(date);
		 
	 }
	 
	 
//	 ----------------------------- Show All Backup DataBases-----------------
	 
	 @GetMapping("/sql/showBackupFiles/{foldername}")
	 public Map<String, List<String>> getBackupFileNames(@PathVariable String foldername) throws FileNotFoundException {
	     Map<String, List<String>> backupFileNames = appService.getBackupFileNames(foldername);
	     return backupFileNames;
	 }
}


	

