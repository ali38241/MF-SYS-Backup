package com.data.backup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import com.data.backup.app.AppService;


@SpringBootApplication

public class BackupApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackupApplication.class, args);
		
		
		
		
	}

}
