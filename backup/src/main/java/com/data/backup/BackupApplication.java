package com.data.backup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.data.backup.app.AppService;


@SpringBootApplication
public class BackupApplication {

	public static void main(String[] args) {
//		AppService appService = new AppService();
//		appService.intialize();
		SpringApplication.run(BackupApplication.class, args);
		
	}

}
