package com.data.backup.loan;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class LoanController {

	@Autowired
	private LoanService loanService;

	@GetMapping("/sql/findOrga/{orga}")
	public Optional<Loan> removeRecord(@PathVariable Long orga) {
		return loanService.findByOrga(orga);
	}
	
}
