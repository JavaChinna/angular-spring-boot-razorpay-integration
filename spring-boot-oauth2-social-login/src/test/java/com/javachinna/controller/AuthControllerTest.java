package com.javachinna.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javachinna.config.MockUserUtils;
import com.javachinna.config.WithMockCustomUser;
import com.javachinna.dto.LocalUser;
import com.javachinna.dto.LoginRequest;
import com.javachinna.dto.SignUpRequest;
import com.javachinna.dto.SocialProvider;
import com.javachinna.exception.UserAlreadyExistAuthenticationException;
import com.javachinna.model.User;
import com.javachinna.service.UserService;

import dev.samstevens.totp.code.CodeVerifier;;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private CodeVerifier verifier;

	@MockBean
	private AuthenticationManager authenticationManager;

	private static User user = MockUserUtils.getMockUser("JavaChinna");

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testAuthenticateUser() throws Exception {
		LocalUser localUser = LocalUser.create(user, null, null, null);
		LoginRequest loginRequest = new LoginRequest(user.getEmail(), user.getPassword());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(localUser, null);
		Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
		String json = mapper.writeValueAsString(loginRequest);
		mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.authenticated").value("true")).andExpect(jsonPath("$.accessToken").isNotEmpty());

		// Test when user 2fa is enabled
		user.setUsing2FA(true);
		mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.authenticated").value("false")).andExpect(jsonPath("$.user").doesNotExist());
	}

	@Test
	public void testRegisterUser() throws Exception {
		SignUpRequest signUpRequest = new SignUpRequest("1234", "JavaChinna", user.getEmail(), user.getPassword(), user.getPassword(), SocialProvider.FACEBOOK);
		Mockito.when(userService.registerNewUser(any(SignUpRequest.class))).thenReturn(user);
		String json = mapper.writeValueAsString(signUpRequest);
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value("true")).andExpect(jsonPath("$.message").value("User registered successfully"));

		// Test when user provided email already exists in the database
		Mockito.when(userService.registerNewUser(any(SignUpRequest.class))).thenThrow(new UserAlreadyExistAuthenticationException("exists"));
		json = mapper.writeValueAsString(signUpRequest);
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value("false")).andExpect(jsonPath("$.message").value("Email Address already in use!"));
	}

	@Test
	@WithMockCustomUser
	public void testVerifyCodeWhenCodeIsNotValid() throws Exception {
		Mockito.when(verifier.isValidCode(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
		String json = mapper.writeValueAsString("443322");
		mockMvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value("false")).andExpect(jsonPath("$.message").value("Invalid Code!"));
	}

	@Test
	@WithMockCustomUser
	public void testVerifyCodeWhenCodeIsValid() throws Exception {
		Mockito.when(verifier.isValidCode(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		String json = mapper.writeValueAsString("443322");
		mockMvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.authenticated").value("true")).andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.user").exists());
	}
}
