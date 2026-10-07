package Hotel.bff.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import tools.jackson.databind.json.JsonMapper;

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

    @Test
    void sinTokenResponde401ConAuth001() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/bff/dashboard/1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        config.authenticationEntryPoint(JsonMapper.builder().build())
                .commence(req, res, new InsufficientAuthenticationException("no autenticado"));

        assertEquals(401, res.getStatus());
        assertEquals("Bearer", res.getHeader("WWW-Authenticate"));
        assertTrue(res.getContentAsString().contains("AUTH-001"));
    }
}