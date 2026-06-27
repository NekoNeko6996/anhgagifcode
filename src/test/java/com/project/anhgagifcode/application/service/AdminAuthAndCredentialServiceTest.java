package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.UpdateAdminCredentialsRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginResponse;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminJpaRepository;
import com.project.anhgagifcode.infrastructure.security.CustomUserDetails;
import com.project.anhgagifcode.infrastructure.security.JwtTokenProvider;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthAndCredentialServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private AdminJpaRepository adminJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private Admins mockAdmin;

    @BeforeEach
    void setUp() {
        mockAdmin = new Admins("admin-1", "admin", "encodedPassword", "Admin Name", "ROLE_ADMIN", "ACTIVE", new Date());
    }

    @Test
    void testLogin_Success() {
        AdminAuthService authService = new AdminAuthService(authenticationManager, tokenProvider);
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("admin");
        request.setPassword("pass123");

        Authentication auth = mock(Authentication.class);
        com.project.anhgagifcode.domain.model.Admin domainAdmin = com.project.anhgagifcode.domain.model.Admin.builder()
                .username("admin")
                .role("ROLE_ADMIN")
                .build();
        CustomUserDetails principal = new CustomUserDetails(domainAdmin);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(principal);
        when(tokenProvider.generateToken("admin")).thenReturn("jwt-token");

        AdminLoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("admin", response.getUsername());
    }

    @Test
    void testLogin_Failed_ThrowsException() {
        AdminAuthService authService = new AdminAuthService(authenticationManager, tokenProvider);
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong_pass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BusinessRuleViolationException.class, () -> authService.login(request));
    }

    @Test
    void testUpdateCredentials_Success() {
        UpdateAdminCredentialsService credentialsService = new UpdateAdminCredentialsService(adminJpaRepository, passwordEncoder);
        UpdateAdminCredentialsRequest request = UpdateAdminCredentialsRequest.builder()
                .oldPassword("oldPass")
                .newUsername("newAdminName")
                .newPassword("newPass")
                .build();

        when(adminJpaRepository.findByUsernameAndStatus("admin", "ACTIVE")).thenReturn(Optional.of(mockAdmin));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(adminJpaRepository.findByUsernameAndStatus("newAdminName", "ACTIVE")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPass");

        credentialsService.updateCredentials("admin", request);

        assertEquals("newAdminName", mockAdmin.getUsername());
        assertEquals("newEncodedPass", mockAdmin.getPasswordHash());
        verify(adminJpaRepository, times(1)).save(mockAdmin);
    }

    @Test
    void testUpdateCredentials_WrongOldPassword_ThrowsException() {
        UpdateAdminCredentialsService credentialsService = new UpdateAdminCredentialsService(adminJpaRepository, passwordEncoder);
        UpdateAdminCredentialsRequest request = UpdateAdminCredentialsRequest.builder()
                .oldPassword("wrongOldPass")
                .build();

        when(adminJpaRepository.findByUsernameAndStatus("admin", "ACTIVE")).thenReturn(Optional.of(mockAdmin));
        when(passwordEncoder.matches("wrongOldPass", "encodedPassword")).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> credentialsService.updateCredentials("admin", request));
    }

    @Test
    void testUpdateCredentials_UsernameExists_ThrowsException() {
        UpdateAdminCredentialsService credentialsService = new UpdateAdminCredentialsService(adminJpaRepository, passwordEncoder);
        UpdateAdminCredentialsRequest request = UpdateAdminCredentialsRequest.builder()
                .oldPassword("oldPass")
                .newUsername("existingAdmin")
                .build();

        when(adminJpaRepository.findByUsernameAndStatus("admin", "ACTIVE")).thenReturn(Optional.of(mockAdmin));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(adminJpaRepository.findByUsernameAndStatus("existingAdmin", "ACTIVE")).thenReturn(Optional.of(mockAdmin));

        assertThrows(BusinessRuleViolationException.class, () -> credentialsService.updateCredentials("admin", request));
    }

    @Test
    void testUpdateCredentials_AdminNotFound_ThrowsException() {
        UpdateAdminCredentialsService credentialsService = new UpdateAdminCredentialsService(adminJpaRepository, passwordEncoder);
        UpdateAdminCredentialsRequest request = UpdateAdminCredentialsRequest.builder()
                .oldPassword("oldPass")
                .build();

        when(adminJpaRepository.findByUsernameAndStatus("unknown", "ACTIVE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> credentialsService.updateCredentials("unknown", request));
    }
}
