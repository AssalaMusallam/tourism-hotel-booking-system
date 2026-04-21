package com.swer313.projectstep1.auth;

import com.swer313.projectstep1.security.JwtService;
import com.swer313.projectstep1.user.DuplicateUserEmailException;
import com.swer313.projectstep1.user.User;
import com.swer313.projectstep1.user.UserRepository;
import com.swer313.projectstep1.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
    هذا test للكلاس AuthServiceImpl

    الفكرة:
    نحن لا نختبر الداتابيس الحقيقي ولا الـ Spring الحقيقي
    بل نختبر فقط منطق AuthServiceImpl نفسه

    لذلك نعمل Mock لكل dependency يعتمد عليها:
    - UserRepository
    - PasswordEncoder
    - JwtService
    - AuthenticationManager
*/
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    /*
        هذا هو الكلاس الحقيقي الذي نريد اختباره
        لكن dependencies تبعته كلها mocks
    */
    private AuthServiceImpl authService;

    /*
        ArgumentCaptor يفيدنا عندما نريد أن نمسك الـ object
        الذي تم إرساله إلى save(...) مثلًا
        حتى نفحص القيم التي بداخله
    */
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void register_success_shouldTrimAndLowercaseEmail_encodePassword_saveGuest_generateToken() {
        // ---------- Arrange ----------
        // نجهز البيانات التي سندخلها إلى register
        RegisterRequest req = new RegisterRequest();
        req.setFullName(" Alice ");
        req.setEmail("  ALICE@example.COM ");
        req.setPassword("Pa55word");
        req.setPhone("  +123 ");

        /*
            هنا نحدد سلوك الـ mocks:

            1) الإيميل بعد trim + lowercase سيكون alice@example.com
            2) هذا الإيميل غير موجود مسبقًا
            3) تشفير الباسورد يعيد ENCODED_pw
            4) توليد التوكن يعيد jwt-token-123
         */
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Pa55word")).thenReturn("ENCODED_pw");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");

        // ---------- Act ----------
        // ننفذ الميثود الحقيقية التي نختبرها
        AuthResponse resp = authService.register(req);

        // ---------- Assert ----------
        // نتأكد أن النتيجة ليست null
        assertNotNull(resp);

        // نتأكد أن التوكن الذي رجع هو نفسه المتوقع
        assertEquals("jwt-token-123", resp.getToken());

        // نتأكد أن الخدمة فحصت الإيميل بعد تنظيفه
        verify(userRepository).existsByEmail("alice@example.com");

        // نتأكد أن الباسورد تم تشفيره
        verify(passwordEncoder).encode("Pa55word");

        /*
            نمسك الـ User الذي تم تمريره إلى save(...)
            حتى نفحص القيم التي أنشأها AuthServiceImpl
         */
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        // fullName في هذا الكود يمر كما هو بدون trim
        assertEquals(" Alice ", saved.getFullName());

        // الإيميل يجب أن يكون cleaned: trim + lowercase
        assertEquals("alice@example.com", saved.getEmail());

        // الباسورد يجب أن يكون مشفرًا وليس النص الأصلي
        assertEquals("ENCODED_pw", saved.getPasswordHash());

        // الهاتف في هذا الكود يمر كما هو
        assertEquals("  +123 ", saved.getPhone());

        // التسجيل العادي يجب أن ينشئ المستخدم بدور GUEST
        assertEquals(UserRole.GUEST, saved.getRole());

        // نتأكد أن التوكن تم توليده لهذا المستخدم نفسه
        verify(jwtService).generateToken(saved);
    }

    @Test
    void register_duplicateEmail_shouldThrowDuplicateUserEmailException_andNotSave() {
        // ---------- Arrange ----------
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Bob");
        req.setEmail("bob@example.com");
        req.setPassword("Password1");

        // هنا نقول للمـوك أن الإيميل موجود مسبقًا
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        // ---------- Act + Assert ----------
        // نتوقع أن الميثود ترمي DuplicateUserEmailException
        assertThrows(DuplicateUserEmailException.class, () -> authService.register(req));

        // نتأكد أن فحص الإيميل حصل فعلًا
        verify(userRepository).existsByEmail("bob@example.com");

        // بما أن الإيميل مكرر، لا يجب أن يتم الحفظ
        verify(userRepository, never()).save(any());

        // ولا يجب توليد توكن
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_success_shouldAuthenticate_findUser_generateToken() {
        // ---------- Arrange ----------
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("secret");

        /*
            ننشئ user وهمي سيمثل المستخدم الموجود في قاعدة البيانات
         */
        User user = new User("User", "user@example.com", "hash", "", UserRole.GUEST);
        user.setId(42L);

        /*
            login يعتمد على ثلاث خطوات هنا:
            1) authenticate
            2) findByEmail
            3) generateToken
         */
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("tok-42");

        // ---------- Act ----------
        AuthResponse resp = authService.login(req);

        // ---------- Assert ----------
        assertNotNull(resp);
        assertEquals("tok-42", resp.getToken());

        // نتأكد أن الـ response يحتوي user data
        assertNotNull(resp.getUser());
        assertEquals(42L, resp.getUser().getId());

        // نتأكد أن الـ authenticate تم استدعاؤه
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // نتأكد أن المستخدم تم جلبه من الريبو
        verify(userRepository).findByEmail("user@example.com");

        // نتأكد أن التوكن تم توليده
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldTrimAndLowercaseEmail_beforeAuthentication() {
        // ---------- Arrange ----------
        LoginRequest req = new LoginRequest();
        req.setEmail("  USER@ExamPle.Com  ");
        req.setPassword("pwd");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(
                        new User("U", "user@example.com", "h", "", UserRole.GUEST)
                ));

        when(jwtService.generateToken(any(User.class))).thenReturn("t");

        /*
            هنا نريد أن نمسك الـ token الذي أُرسل إلى authenticate
            حتى نرى ما هو الـ principal وما هي الـ credentials
         */
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        // ---------- Act ----------
        authService.login(req);

        // ---------- Assert ----------
        verify(authenticationManager).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken token = authCaptor.getValue();

        // نتأكد أن الإيميل تم تنظيفه قبل الإرسال إلى authenticate
        assertEquals("user@example.com", token.getPrincipal());

        // الباسورد يجب أن يمر كما هو
        assertEquals("pwd", token.getCredentials());
    }

    @Test
    void login_userNotFoundAfterAuthentication_shouldThrowUsernameNotFoundException() {
        // ---------- Arrange ----------
        LoginRequest req = new LoginRequest();
        req.setEmail("nouser@example.com");
        req.setPassword("x");

        // authentication نجح
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // لكن بعد ذلك لم يتم العثور على المستخدم
        when(userRepository.findByEmail("nouser@example.com"))
                .thenReturn(Optional.empty());

        // ---------- Act + Assert ----------
        assertThrows(UsernameNotFoundException.class, () -> authService.login(req));

        // بما أن المستخدم غير موجود، لا يجب توليد توكن
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_authenticationFails_shouldPropagateException_andNotGenerateToken() {
        // ---------- Arrange ----------
        LoginRequest req = new LoginRequest();
        req.setEmail("fail@example.com");
        req.setPassword("bad");

        /*
            هنا نفترض أن Spring Security رفض تسجيل الدخول
            بسبب credentials خاطئة
         */
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad creds"));

        // ---------- Act + Assert ----------
        assertThrows(BadCredentialsException.class, () -> authService.login(req));

        // إذا فشل authenticate من البداية، لا يجب أن نبحث عن المستخدم
        verify(userRepository, never()).findByEmail(any());

        // ولا يجب توليد توكن
        verify(jwtService, never()).generateToken(any());
    }
}