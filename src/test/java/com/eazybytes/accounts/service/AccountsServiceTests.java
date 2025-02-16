package com.eazybytes.accounts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.impl.AccountsServiceImpl;

@WebMvcTest(AccountsServiceImpl.class)
class AccountsServiceTests {

	@MockBean
	AccountsRepository accountsRepository;

	@MockBean
	CustomerRepository customerRepository;

	@Autowired
	private AccountsServiceImpl accountsService;

	@Test
	void testCreateAccount_Success() {
		// Mock data
		CustomerDto customerDto = new CustomerDto();
		customerDto.setMobileNumber("9567017116");
		Customer savedCustomer = new Customer();
		savedCustomer.setCustomerId(1L);
		Accounts mockAccount = new Accounts();

		// Mock behavior
		when(customerRepository.findByMobileNumber(any(String.class))).thenReturn(Optional.empty());
		when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
		/*
		 * You cannot use doNothing() with when() in below scenario because
		 * accountsRepository.save() is not a void method. Instead, it is designed to
		 * return an Accounts object (the saved entity) based on how Spring Data JPA
		 * works.
		 */
		when(accountsRepository.save(any(Accounts.class))).thenReturn(mockAccount);

		// Call the service method
		accountsService.createAccount(customerDto);

		// Verify
		verify(customerRepository, times(1)).findByMobileNumber(customerDto.getMobileNumber());
		verify(customerRepository, times(1)).save(any(Customer.class));
		verify(accountsRepository, times(1)).save(any(Accounts.class));
	}

	@Test
	void testCreateAccount_CustomerAlreadyExists() {
		// Mock data
		CustomerDto customerDto = new CustomerDto();
		customerDto.setMobileNumber("9567017116");

		// Mock behavior
		when(customerRepository.findByMobileNumber(customerDto.getMobileNumber()))
				.thenReturn(Optional.of(new Customer()));

		// Call the method and assert exception
		assertThrows(CustomerAlreadyExistsException.class, () -> accountsService.createAccount(customerDto));

		// Verify
		verify(customerRepository, times(1)).findByMobileNumber(customerDto.getMobileNumber());
		verifyNoInteractions(accountsRepository);
	}

	@Test
	void testFetchAccount_Success() {
		// Mock data
		String mobileNumber = "9567017116";
		Customer customer = new Customer(1L, "Madan Reddy", "madan.reddy@tcs.com", mobileNumber);
		Accounts accounts = new Accounts(customer.getCustomerId(), 123456L, "Savings", "123 Main Street,New York");

		// Mock behavior
		when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));
		when(accountsRepository.findByCustomerId(customer.getCustomerId())).thenReturn(Optional.of(accounts));

		// Call the method
		CustomerDto result = accountsService.fetchAccount(mobileNumber);

		// Assertions
		assertNotNull(result);
		assertEquals(123456L, result.getAccountsDto().getAccountNumber());
		assertEquals(mobileNumber, result.getMobileNumber());
		assertEquals("Madan Reddy", result.getName());
		assertEquals("madan.reddy@tcs.com", result.getEmail());
		assertEquals("Savings", result.getAccountsDto().getAccountType());
		assertEquals("123 Main Street,New York", result.getAccountsDto().getBranchAddress());

		// Verify
		verify(customerRepository, times(1)).findByMobileNumber(mobileNumber);
		verify(accountsRepository, times(1)).findByCustomerId(customer.getCustomerId());
	}

	@Test
	void testFetchAccount_Negative_Scenario_1() {
		// Mock data
		String mobileNumber = "9567017116";
		Customer customer = new Customer(1L, "Madan Reddy", "madan.reddy@tcs.com", mobileNumber);
		Accounts accounts = new Accounts(customer.getCustomerId(), 123456L, "Savings", "123 Main Street,New York");

		// Mock behavior
		when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.empty());
		when(accountsRepository.findByCustomerId(customer.getCustomerId())).thenReturn(Optional.of(accounts));

		// Assertions
		assertThrows(ResourceNotFoundException.class, () -> accountsService.fetchAccount(mobileNumber));
	}

	@Test
	void testFetchAccount_Negative_Scenario_2() {
		// Mock data
		String mobileNumber = "9567017116";
		Customer customer = new Customer(1L, "Madan Reddy", "madan.reddy@tcs.com", mobileNumber);
		Accounts accounts = new Accounts(customer.getCustomerId(), 123456L, "Savings", "123 Main Street,New York");

		// Mock behavior
		when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));
		when(accountsRepository.findByCustomerId(customer.getCustomerId())).thenReturn(Optional.empty());

		// Assertions
		assertThrows(ResourceNotFoundException.class, () -> accountsService.fetchAccount(mobileNumber));
	}

	@Test
	void testUpdateAccount_Success() {
		// Mock data
		CustomerDto customerDto = new CustomerDto();
		customerDto.setAccountsDto(new AccountsDto(123456L, "Savings", "123 Address"));

		Accounts accounts = new Accounts();
		accounts.setAccountNumber(123456L);
		accounts.setCustomerId(1L);

		Customer customer = new Customer();
		customer.setCustomerId(1L);

		// Mock behavior
		when(accountsRepository.findById(123456L)).thenReturn(Optional.of(accounts));
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);
		when(customerRepository.save(any(Customer.class))).thenReturn(any(Customer.class));

		// Call the method
		boolean isUpdated = accountsService.updateAccount(customerDto);

		// Assertions
		assertTrue(isUpdated);

		// Verify
		verify(accountsRepository, times(1)).findById(123456L);
		verify(customerRepository, times(1)).findById(1L);
		verify(accountsRepository, times(1)).save(accounts);
		verify(customerRepository, times(1)).save(customer);
		
	}
	@Test
	void testUpdateAccount_Failure_scenario_1() {
		// Mock data
		CustomerDto customerDto = new CustomerDto();
		customerDto.setAccountsDto(new AccountsDto(123456L, "Savings", "123 Address"));

		Accounts accounts = new Accounts();
		accounts.setAccountNumber(123456L);
		accounts.setCustomerId(1L);

		Customer customer = new Customer();
		customer.setCustomerId(1L);

		// Mock behavior
		when(accountsRepository.findById(123456L)).thenReturn(Optional.empty());
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);
		when(customerRepository.save(any(Customer.class))).thenReturn(any(Customer.class));


		assertThrows(ResourceNotFoundException.class, () -> accountsService.updateAccount(customerDto));
	    verify(accountsRepository, never()).findById(1L);
	    verify(accountsRepository, never()).save(any(Accounts.class));
	    verify(customerRepository, never()).save(customer);
	}
	
	@Test
	void testUpdateAccount_Failure_scenario_2() {
		// Mock data
		CustomerDto customerDto = new CustomerDto();
		customerDto.setAccountsDto(new AccountsDto(123456L, "Savings", "123 Address"));

		Accounts accounts = new Accounts();
		accounts.setAccountNumber(123456L);
		accounts.setCustomerId(1L);

		Customer customer = new Customer();
		customer.setCustomerId(1L);

		// Mock behavior
		when(accountsRepository.findById(123456L)).thenReturn(Optional.of(accounts));
		when(customerRepository.findById(1L)).thenReturn(Optional.empty());
		when(accountsRepository.save(any(Accounts.class))).thenReturn(accounts);
		when(customerRepository.save(any(Customer.class))).thenReturn(any(Customer.class));

		assertThrows(ResourceNotFoundException.class, () -> accountsService.updateAccount(customerDto));
	    verify(customerRepository, never()).save(customer);

	}

@Test
void testDeleteAccount_Failure() {
    // Mock data
    String mobileNumber = "9567017116";
    Customer customer = new Customer();
    customer.setCustomerId(1L);

    // Mock behavior
    when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));

    // Call the method
    boolean isDeleted = accountsService.deleteAccount(mobileNumber);

    // Assertions
    assertTrue(isDeleted);

    // Verify
    verify(customerRepository, times(1)).findByMobileNumber(mobileNumber);
    verify(accountsRepository, times(1)).deleteByCustomerId(customer.getCustomerId());
    verify(customerRepository, times(1)).deleteById(customer.getCustomerId());
}
}
