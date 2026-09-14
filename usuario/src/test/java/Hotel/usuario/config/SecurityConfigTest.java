package Hotel.usuario.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    private Jwt jwtConRoles(String... roles) {
        Jwt.Builder b = Jwt.withTokenValue("t")
                .header("alg", "none")
                .subject("user");
        b.claim("roles", List.of(roles));
        return b.build();
    }

    @Test
    void claimRolesSeMapeaAAuthoritiesROLE() {
        AbstractAuthenticationToken token = config.jwtAuthenticationConverter()
                .convert(jwtConRoles("ADMIN", "GERENTE"));
        var authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_GERENTE"));
    }

    @Test
    void claimEntraAdminSeMapeaAROLE_ADMIN() {
        AbstractAuthenticationToken token = config.jwtAuthenticationConverter()
                .convert(jwtConRoles("Admin", "Cliente"));
        var authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_CLIENTE"));
    }
}
