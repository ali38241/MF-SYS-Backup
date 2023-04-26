package com.data.backup.app;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
	
	@GetMapping("/mongo/restore/{dbName}")
	public void restoreMulti(@PathVariable ArrayList<String> dbName) {
		appService.restore(dbName);
	}
	
	@GetMapping("/mongo/showAll")
	public Map<Integer, String> showAll() {
		return appService.showAll();
	}

	@GetMapping("/mongo/zip/{dbName}")
	public String zip(@PathVariable List<String> dbName) throws IOException {
		return appService.zip(dbName);
//		appService.createzipfile(dbName);
	}

//---------------------MYSQL-----------------------------------
	
	@GetMapping("/sql/getbackup/{dbname}")
	public List<Map<String, String>> backupDatabase(@PathVariable List<String> dbname)
			throws IOException, InterruptedException {
		List<Map<String, String>> map = appService.backupDatabase(dbname);
		return map;
	}
	
	
	@PostMapping("/sql/restore/{dbname}")
	public ResponseEntity<String> restoreDatabase(@PathVariable ArrayList<String> dbname) throws IOException, InterruptedException{
		boolean success = appService.restoreDatabase(dbname);
		if(success) {
			return ResponseEntity.ok("restore created successfully");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error restoring");
		}
	}
	
	 @GetMapping("/sql/alldatabases")
		public Map<Integer, String> showall() {
	        Map<Integer, String> result = appService.viewall();
			return result;
	    }
	 
//	 ---------------------------zipping files----------------
	 
	 
	 
	 @GetMapping("/sql/createzip/{filenames}")
	 public void createzipfiles(@PathVariable List<String> filenames) throws IOException{
		 appService.createzipfile(filenames);
		 
//		 boolean success = appService.createzipfile(filenames);
//		 if(success) {
//			 return ResponseEntity.ok("Zip file created successfully");
			 
//		 }else {
//			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating zip file");
//		 }
	 }
	 
	 
}
