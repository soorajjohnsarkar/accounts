package com.eazybytes.accounts.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.service.IAccountsService;

@WebMvcTest(AccountsController.class)
public class AccountsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IAccountsService iAccountsService;

	@Test
	void testCreateAccount() throws Exception {
		CustomerDto customerDto = new CustomerDto();
		doNothing().when(iAccountsService).createAccount(any(CustomerDto.class));
		iAccountsService.createAccount(customerDto);
		verify(iAccountsService, times(1)).createAccount(customerDto);
		mockMvc.perform(post("/api/create").contentType("application/json").content("""
				  {
				    "name": "Madan Reddy",
				    "email": "tutor@eazybytes",
				    "mobileNumber": "4354437687"
				}
				""")).andExpect(status().isCreated()).andExpect(jsonPath("$.statusCode").value("201"))
				.andExpect(jsonPath("$.statusMsg").value("Account created successfully"));
	}

	@Test
	void testFetchAccountDetails() throws Exception {
		CustomerDto customerDto = new CustomerDto();
		customerDto.setName("Madan Reddy");
		customerDto.setEmail("madan.reddy@tcs.com");
		customerDto.setMobileNumber("9567017116");
		customerDto.setAccountsDto(new AccountsDto(1189502144L, "Savings", "123 Main Street,New York"));
		when(iAccountsService.fetchAccount(any(String.class))).thenReturn(customerDto);
		mockMvc.perform(get("/api/fetch?mobileNumber=9567017116").contentType("application/json"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Madan Reddy"))
				.andExpect(jsonPath("$.email").value("madan.reddy@tcs.com"))
				.andExpect(jsonPath("$.mobileNumber").value("9567017116"))
				.andExpect(jsonPath("$.accountsDto.accountNumber").value(1189502144L))
				.andExpect(jsonPath("$.accountsDto.accountType").value("Savings"))
				.andExpect(jsonPath("$.accountsDto.branchAddress").value("123 Main Street,New York"));
	}

	@Test
	void testUpdateAccountDetailsPositiveScenario() throws Exception {

		when(iAccountsService.updateAccount(any(CustomerDto.class))).thenReturn(true);
		mockMvc.perform(put("/api/update").contentType(MediaType.APPLICATION_JSON).content("""
							    {
				                "name": "Madan Mohan",
				                "email": "tutor@eazybytes",
				                "mobileNumber": "4354437687",
				                "accountsDto": {
				        			"accountNumber": 1724830478,
				        			"accountType": "Savings",
				        			"branchAddress": "123 Main Street, New York"
				                                }
				                }
							""")).andExpect(status().isOk())
		                         .andExpect(jsonPath("$.statusCode").value("200"))
		                         .andExpect(jsonPath("$.statusMsg").value("Request processed successfully"));
 
	}
	
	@Test
	void testUpdateAccountDetailsNegativeScenario() throws Exception {

		when(iAccountsService.updateAccount(any(CustomerDto.class))).thenReturn(false);
		mockMvc.perform(put("/api/update").contentType(MediaType.APPLICATION_JSON).content("""
							    {
				                "name": "Madan Mohan",
				                "email": "tutor@eazybytes",
				                "mobileNumber": "4354437687",
				                "accountsDto": {
				        			"accountNumber": 1724830478,
				        			"accountType": "Savings",
				        			"branchAddress": "123 Main Street, New York"
				                                 }
				                }
							""")).andExpect(status().isExpectationFailed())
		                         .andExpect(jsonPath("$.statusCode").value("417"))
		                         .andExpect(jsonPath("$.statusMsg").value("Update operation failed. Please try again or contact Dev team"));
 
	}


	@Test
	void testDeleteAccountDetailsPositiveScenario() throws Exception {
		when(iAccountsService.deleteAccount(any(String.class))).thenReturn(true);
		mockMvc.perform(delete("/api/delete?mobileNumber=4354437687").contentType("application/json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCode").value("200"))
				.andExpect(jsonPath("$.statusMsg").value("Request processed successfully"));
	}
	
	@Test
	void testDeleteAccountDetailsNegativeScenario() throws Exception {
		when(iAccountsService.deleteAccount(any(String.class))).thenReturn(false);
		mockMvc.perform(delete("/api/delete?mobileNumber=4354437687").contentType("application/json"))
				.andExpect(status().isExpectationFailed())
				.andExpect(jsonPath("$.statusCode").value("417"))
				.andExpect(jsonPath("$.statusMsg").value("Delete operation failed. Please try again or contact Dev team"));
	}
}






