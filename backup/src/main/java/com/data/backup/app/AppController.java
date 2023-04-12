package com.data.backup.app;


import java.io.IOException;
import java.util.ArrayList;
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
	public void backUpMultiple(@PathVariable ArrayList<String> dbName)  {
		appService.backup(dbName);
	}
	
	@GetMapping("/mongo/restore/{dbName}")
	public void restoreMulti(@PathVariable ArrayList<String> dbName) {
		appService.restore(dbName);
	}
	
	@GetMapping("/mongo/showAll")
	public Map<Integer, String> showAll() {
		return appService.showAll();
	}
//---------------------MYSQL-----------------------------------
	
	@GetMapping("/sql/getbackup/{dbname}")
	public ResponseEntity<String> backupDatabase(@PathVariable ArrayList<String> dbname) throws IOException,InterruptedException{
		boolean success = appService.backupDatabase(dbname);
		if(success) {
			return ResponseEntity.ok("Backup created successfully");
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating backup");
		}
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
	public String showall() {
		return appService.viewall();
		
	}
}
