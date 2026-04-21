package com.swer313.projectstep1.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.errors.GlobalExceptionHandler;
import com.swer313.projectstep1.security.JwtAuthFilter;
import com.swer313.projectstep1.security.JwtService;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserResponseDTO;
import com.swer313.projectstep1.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    هذا test خاص بالـ controller فقط

    لماذا نستخدم WebMvcTest؟
    لأننا نريد اختبار:
    - الـ endpoint
    - الـ status code
    - الـ request/response JSON
    - الـ validation

    ولا نريد تشغيل المشروع كله ولا قاعدة البيانات
 */
@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)

/*
    addFilters = false
    يعني لا نريد تنفيذ security filters فعليًا أثناء test الطلبات
 */
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	/*
        هذا هو dependency الحقيقي للـ controller
        لذلك نعمل له mock
     */
	@MockBean
	private AuthService authService;

	/*
        هذان الموك مهمّان بسبب الخطأ الذي ظهر عندك:

        Spring حاول ينشئ JwtAuthFilter داخل test context
        والفلتر يعتمد على JwtService
        ففشل تحميل الـ context لأنه لم يجد bean من نوع JwtService

        لذلك نعمل mock للفلتر نفسه ولـ JwtService
        حتى يحمّل الـ context بدون مشاكل
     */
	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	@MockBean
	private JwtService jwtService;

	@Test
	@DisplayName("register_validRequest_returns201_andResponseBody")
	void register_validRequest_returns201_andResponseBody() throws Exception {
		// ---------- Arrange ----------
		RegisterRequest req = new RegisterRequest();
		req.setFullName("Alice");
		req.setEmail("alice@example.com");
		req.setPassword("Pa55word");
		req.setPhone("+123");

		User user = new User("Alice", "alice@example.com", "hash", "+123", UserRole.GUEST);
		user.setId(10L);

		UserResponseDTO userDto = new UserResponseDTO(user);
		AuthResponse response = new AuthResponse("jwt-1", userDto);

        /*
            عندما يستدعي الكنترولر authService.register(...)
            نرجّع هذا الـ response الجاهز
         */
		when(authService.register(any(RegisterRequest.class))).thenReturn(response);

		// ---------- Act + Assert ----------
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token", is("jwt-1")))
				.andExpect(jsonPath("$.user.email", is("alice@example.com")));

		// نتأكد أن الكنترولر استدعى السيرفس
		verify(authService).register(any(RegisterRequest.class));
	}

	@Test
	@DisplayName("login_validRequest_returns200_andResponseBody")
	void login_validRequest_returns200_andResponseBody() throws Exception {
		// ---------- Arrange ----------
		LoginRequest req = new LoginRequest();
		req.setEmail("bob@example.com");
		req.setPassword("Secret1");

		User user = new User("Bob", "bob@example.com", "hash", "", UserRole.GUEST);
		user.setId(11L);

		UserResponseDTO userDto = new UserResponseDTO(user);
		AuthResponse response = new AuthResponse("jwt-2", userDto);

		when(authService.login(any(LoginRequest.class))).thenReturn(response);

		// ---------- Act + Assert ----------
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", is("jwt-2")))
				.andExpect(jsonPath("$.user.id", is(11)));

		verify(authService).login(any(LoginRequest.class));
	}

	@Test
	@DisplayName("register_missingRequiredFields_returns400")
	void register_missingRequiredFields_returns400() throws Exception {
        /*
            نرسل body فارغ
            المفروض الـ validation يفشل لأن الحقول المطلوبة ناقصة
         */
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

        /*
            بما أن الـ validation فشل في الكنترولر
            فلا يجب أن يصل الطلب إلى service أصلًا
         */
		verify(authService, never()).register(any());
	}

	@Test
	@DisplayName("register_invalidEmail_returns400")
	void register_invalidEmail_returns400() throws Exception {
		RegisterRequest req = new RegisterRequest();
		req.setFullName("C");
		req.setEmail("not-an-email");
		req.setPassword("Pa55word");

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

		verify(authService, never()).register(any());
	}

	@Test
	@DisplayName("register_weakPassword_returns400")
	void register_weakPassword_returns400() throws Exception {
		RegisterRequest req = new RegisterRequest();
		req.setFullName("D");
		req.setEmail("d@example.com");
		req.setPassword("password"); // ضعيف: لا يحتوي uppercase ولا digit

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

		verify(authService, never()).register(any());
	}

	@Test
	@DisplayName("login_missingEmail_returns400")
	void login_missingEmail_returns400() throws Exception {
		HashMap<String, String> body = new HashMap<>();
		body.put("password", "x");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

		verify(authService, never()).login(any());
	}

	@Test
	@DisplayName("login_invalidEmail_returns400")
	void login_invalidEmail_returns400() throws Exception {
		LoginRequest req = new LoginRequest();
		req.setEmail("not-email");
		req.setPassword("x");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

		verify(authService, never()).login(any());
	}

	@Test
	@DisplayName("login_missingPassword_returns400")
	void login_missingPassword_returns400() throws Exception {
		HashMap<String, String> body = new HashMap<>();
		body.put("email", "ok@example.com");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Validation failed")));

		verify(authService, never()).login(any());
	}
}