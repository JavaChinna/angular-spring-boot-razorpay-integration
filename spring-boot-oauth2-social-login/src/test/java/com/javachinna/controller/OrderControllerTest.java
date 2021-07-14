package com.javachinna.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javachinna.config.TestSecurityConfig;
import com.javachinna.dto.payment.PaymentResponse;
import com.javachinna.service.OrderService;

@SpringBootTest(classes = TestSecurityConfig.class)
@AutoConfigureMockMvc
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OrderService orderService;

	@MockBean
	private PasswordEncoder passwordEncoder;

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testUpdateOrder() throws Exception {
		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setRazorpayPaymentId("22445566");
		String json = mapper.writeValueAsString(paymentResponse);
		mockMvc.perform(put("/api/order").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value("true")).andExpect(jsonPath("$.message").isNotEmpty());
	}
}
