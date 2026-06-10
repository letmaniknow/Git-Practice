package com.mmva.newsapp.domain.adminuser.service.refreshtokenvalidation;

import com.mmva.newsapp.domain.adminuser.model.refreshtokenvalidation.AdminRefreshTokenValidation;
import com.mmva.newsapp.domain.adminuser.repository.refreshtokenvalidation.AdminRefreshTokenValidationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AdminRefreshTokenValidationService {
    @Autowired
    private AdminRefreshTokenValidationRepository repository;

    public AdminRefreshTokenValidation save(AdminRefreshTokenValidation token) {
        return repository.save(token);
    }

    public Optional<AdminRefreshTokenValidation> findByToken(String token) {
        return repository.findByToken(token);
    }

    public void revokeToken(String token) {
        repository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setRevokedAt(Instant.now());
            repository.save(rt);
        });
    }

    public void deleteByToken(String token) {
        repository.deleteByToken(token);
    }
}
