package com.data.backup.app;


import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backup")
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
	public String showAll() {
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
	
//	@PostMapping("/restore")
//	public ResponseEntity<String> restoreDatabase(@PathVariable)
//	

}
