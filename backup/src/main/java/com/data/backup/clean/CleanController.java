package com.data.backup.clean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class CleanController {

	@Autowired
	private CleanService cleanService;
	
	@GetMapping("/sql/showTables/{db}")
	public List<String> showSqlTables(@PathVariable String db) {
		return cleanService.showSqlTables(db);
	}
	@PostMapping("/sql/deleteData/{tableName}")
	public void deleteSqlData(@PathVariable ArrayList<String> tableName) {
		cleanService.deleteSqlData(tableName);
	}
	
	@PostMapping("/mongo/clean/{dbName}/{requiredOrganization}")
	public void cleanMongo(@PathVariable String dbName,@PathVariable String requiredOrganization) throws IOException {
		 cleanService.clean(requiredOrganization, dbName);
	}
	
	 

	@GetMapping("/sql/deleteSpecialRow/{databaseName}/{organizationCode}")
	public void deleteSpecialRow(@PathVariable String databaseName, @PathVariable String organizationCode) {
		cleanService.deleteSpecialRow(databaseName,organizationCode);
	}
	
	@GetMapping("/sql/deleteRestOfRow/{databaseName}/{organizationCode}")
	public void deleteRestOfRow(@PathVariable String databaseName, @PathVariable String organizationCode) {
		cleanService.deleteRestOfRow(databaseName,organizationCode);
	}
	
	
}
