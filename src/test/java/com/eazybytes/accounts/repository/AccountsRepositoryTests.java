package com.eazybytes.accounts.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.service.impl.AccountsServiceImpl;

@WebMvcTest(AccountsServiceImpl.class)
public class AccountsRepositoryTests {

	@MockBean
	AccountsRepository accountsRepository;

	@MockBean
	CustomerRepository customerRepository;
	
	
	@Test
    void testFindByMobileNumber_Success() {
        // Mock data
        String mobileNumber = "9567017116";
		Customer testCustomer = new Customer(1L, "Madan Reddy", "madan.reddy@tcs.com", mobileNumber);

        // Mock behavior
        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(testCustomer));

        // Call the method
        Optional<Customer> result = customerRepository.findByMobileNumber(mobileNumber);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(testCustomer.getCustomerId(), result.get().getCustomerId());
        assertEquals(testCustomer.getMobileNumber(), result.get().getMobileNumber());
        assertEquals(testCustomer.getName(), result.get().getName());
        assertEquals(testCustomer.getEmail(), result.get().getEmail());

        // Verify
        verify(customerRepository, times(1)).findByMobileNumber(mobileNumber);
    }

    @Test
    void testFindByMobileNumber_NotFound() {
        // Mock data
        String mobileNumber = "9999999999";

        // Mock behavior
        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.empty());

        // Call the method
        Optional<Customer> result = customerRepository.findByMobileNumber(mobileNumber);

        // Assertions
        assertFalse(result.isPresent());

        // Verify
        verify(customerRepository, times(1)).findByMobileNumber(mobileNumber);
    }
    
    @Test
    void testFindByCustomerId_Success() {
        // Mock data
        Long customerId = 1L;
        Accounts mockAccount = new Accounts();
        mockAccount.setAccountNumber(123456L);
        mockAccount.setCustomerId(customerId);

        // Mock behavior
        when(accountsRepository.findByCustomerId(customerId)).thenReturn(Optional.of(mockAccount));

        // Call the method
        Optional<Accounts> result = accountsRepository.findByCustomerId(customerId);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(mockAccount.getAccountNumber(), result.get().getAccountNumber());
        assertEquals(mockAccount.getCustomerId(), result.get().getCustomerId());

        // Verify
        verify(accountsRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testFindByCustomerId_NotFound() {
        // Mock data
        Long customerId = 2L;

        // Mock behavior
        when(accountsRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        // Call the method
        Optional<Accounts> result = accountsRepository.findByCustomerId(customerId);

        // Assertions
        assertFalse(result.isPresent());

        // Verify
        verify(accountsRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testDeleteByCustomerId_Success() {
        // Mock data
        Long customerId = 1L;

        // Mock behavior
        doNothing().when(accountsRepository).deleteByCustomerId(customerId);

        // Call the method
        accountsRepository.deleteByCustomerId(customerId);

        // Verify
        verify(accountsRepository, times(1)).deleteByCustomerId(customerId);
    }
}
