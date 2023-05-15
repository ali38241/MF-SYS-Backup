package com.data.backup.loan;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LoanService {
	
	@Autowired
	private LoanRepo loanRepo;
	
	public Optional<Loan> findByOrga(Long org) {
		
		Optional<Loan> x = loanRepo.findById(org);
//		System.out.println(x.get().getCollection());
		return x;
	}
	
	
}
