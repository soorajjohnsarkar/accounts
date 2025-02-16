package com.eazybytes.accounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
//@ActiveProfiles("test")
public class AccountsIntegrationTest_mockito {
	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
        accountsRepository.deleteAll();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Perform POST request
		mockMvc.perform(post("/api/create").contentType("application/json")
                .content("""
                    {
                        "mobileNumber": "9567017116",
                        "name": "Test User",
                        "email": "test@example.com"
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(AccountsConstants.STATUS_201))
                .andExpect(jsonPath("$.statusMsg").value(AccountsConstants.MESSAGE_201));
        // Assert database state
        assertEquals(1, customerRepository.count());
        assertEquals(1, accountsRepository.count());
    }

    @Test
    void testCreateAccount_CustomerAlreadyExists() throws Exception {
		// Setup pre-existing customer
		Customer customer = new Customer();
		customer.setMobileNumber("9567017116");
		customer.setName("Existing User");
		customer.setEmail("test@example.com");
		customerRepository.save(customer);

        // Perform POST request
		mockMvc.perform(post("/api/create").contentType("application/json")
                .content("""
                    {	
                        "mobileNumber": "9567017116",
                        "name": "Test User",
                        "email": "test@example.com"
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFetchAccountDetails_Success() throws Exception {
        // Setup customer and account
        Customer customer = new Customer();
        customer.setMobileNumber("9567017116");
        customer.setName("Test User");
        customer.setEmail("test@example.com");
        customer = customerRepository.save(customer);

        Accounts account = new Accounts();
        account.setCustomerId(customer.getCustomerId());
        account.setAccountNumber(123456L);
        account.setAccountType("savings");
        account.setBranchAddress("Kodambakkam ,Chennai 676877");
        accountsRepository.save(account);

        // Perform GET request
        mockMvc.perform(get("/api/fetch")
                .param("mobileNumber", "9567017116"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.mobileNumber").value("9567017116"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accountsDto.accountNumber").value(123456L));
    }

    @Test
    void testFetchAccountDetails_NotFound() throws Exception {
        // Perform GET request with non-existent mobile number
        mockMvc.perform(get("/api/fetch")
                .param("mobileNumber", "9999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled
    void testUpdateAccountDetails_Success() throws Exception {
        // Setup customer and account
        Customer customer = new Customer();
        customer.setMobileNumber("9567017116");
        customer.setName("Test User");
		customer.setEmail("test@example.com");
        customer = customerRepository.save(customer);

        Accounts account = new Accounts();
        account.setCustomerId(customer.getCustomerId());
        account.setAccountNumber(123456L);
        account.setAccountType("savings");
        account.setBranchAddress("Kodambakkam ,Chennai 676877");
        accountsRepository.save(account);

        // Perform PUT request
        mockMvc.perform(put("/api/update").contentType("application/json")
                .content("""
					    {
		                "name": "Madan Mohan",
		                "email": "tutor@eazybytes",
		                "mobileNumber": "9567017116",
		                "accountsDto": {
		        			"accountNumber": 1724830478,
		        			"accountType": "Savings",
		        			"branchAddress": "123 Main Street, New York"
		                                }
		                }
					"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AccountsConstants.STATUS_200))
                .andExpect(jsonPath("$.message").value(AccountsConstants.MESSAGE_200));

        // Assert database changes
        Customer updatedCustomer = customerRepository.findByMobileNumber("9567017116").orElseThrow();
        assertEquals("Updated User", updatedCustomer.getName());

        Accounts updatedAccount = accountsRepository.findByCustomerId(updatedCustomer.getCustomerId()).orElseThrow();
        assertEquals("New Branch", updatedAccount.getBranchAddress());
    }

    @Test
    @Disabled
    void testDeleteAccountDetails_Success() throws Exception {
        // Setup customer and account
        Customer customer = new Customer();
        customer.setMobileNumber("9567017116");
        customer.setName("Test User");
		customer.setEmail("test@example.com");
        customer = customerRepository.save(customer);

        Accounts account = new Accounts();
        account.setCustomerId(customer.getCustomerId());
        account.setAccountType("savings");
        account.setBranchAddress("Kodambakkam ,Chennai 676877");
        accountsRepository.save(account);

        // Perform DELETE request
        mockMvc.perform(delete("/api/delete")
                .param("mobileNumber", "9567017116"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AccountsConstants.STATUS_200))
                .andExpect(jsonPath("$.message").value(AccountsConstants.MESSAGE_200));

        // Assert database state
        assertEquals(0, customerRepository.count());
        assertEquals(0, accountsRepository.count());
    }

}
