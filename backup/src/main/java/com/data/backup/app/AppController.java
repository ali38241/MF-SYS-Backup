package com.data.backup.app;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@GetMapping("/mongo/restore/{dbName}")
	public void restoreMulti(@PathVariable ArrayList<String> dbName) {
		appService.restore(dbName);
	}
	
	@GetMapping("/mongo/showAll")
	public Map<Integer, String> showAll() {
		return appService.showAll();
	}
	@GetMapping("/mongo/showBackup/{date}")
		public Map<String, List<String>> showBackup(@PathVariable String date){
			return appService.showBackup(date);
		}

	@GetMapping("/mongo/zip/{dbName}")
	public String zip(@PathVariable List<String> dbName) throws IOException {
		return appService.zip(dbName);
//		appService.createzipfile(dbName);
	}

//---------------------MYSQL-----------------------------------
	
//	----------------------Backup DataBase----------------------
	
	@GetMapping("/sql/getbackup/{dbname}")
	public List<Map<String, String>> backupDatabase(@PathVariable List<String> dbname)
			throws IOException, InterruptedException {
		List<Map<String, String>> map = appService.backupDatabase(dbname);
		return map;
	}
	
	
//	----------------------------------------- Restore DataBase----------------------------------
	
	@PostMapping("/sql/restore/{date}/{dbname}")
	public ResponseEntity<String> restoreDatabase(@PathVariable String date,@PathVariable ArrayList<String> dbname) throws IOException, InterruptedException{
		boolean success = appService.restoreDatabase(date,dbname);
		if(success) {
			return ResponseEntity.ok("restore created successfully");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error restoring");
		}
	}
	
	
//	----------------------------------View All DataBases---------------------
	
	 @GetMapping("/sql/alldatabases")
		public Map<Integer, String> showall() {
	        Map<Integer, String> result = appService.viewall();
			return result;
	    }
	 
//	 ---------------------------zipping files----------------
	 
	 
	 
	 @GetMapping("/sql/createzip/{date}/{filenames}")
	 public void createzipfiles(@PathVariable String date,@PathVariable List<String> filenames) throws IOException{
		 appService.createzipfile(date,filenames);
		 
	 }
	 
	 
//	 ----------------------------- Show All Backup DataBases-----------------
	 
	 @GetMapping("/sql/showBackupFiles/{foldername}")
	 public ResponseEntity<Map<String, List<String>>> getBackupFileNames(@PathVariable String foldername) {
	     try {
	         Map<String, List<String>> backupFileNames = appService.getBackupFileNames(foldername);
	         return ResponseEntity.ok(backupFileNames);
	     } catch (FileNotFoundException e) {
	    	 String message =  "folder " + foldername + " does not exist";
	    	 List<String> errormessage = Collections.singletonList(message);
	         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", errormessage));
	     } catch (Exception e) {
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	     }
	 }
}


	 

