package Hotel.bff.controller;

import Hotel.bff.service.BffService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints publicos del BFF. */
@RestController
@RequestMapping("/api/v1/bff")
public class BffController {

    @Autowired
    private BffService ser;

    /** Agrega usuario + reservas + el catálogo de servicios. */
    @GetMapping("/dashboard")
    public Map<String, Object> bc_dashboard(@AuthenticationPrincipal Jwt jwt) {
        return ser.b_dashboard("Bearer " + jwt.getTokenValue());
    }

    /** Resumen global, solo ADMIN. */
    @GetMapping("/admin/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> bc_admin(@AuthenticationPrincipal Jwt jwt) {
        return ser.b_admin("Bearer " + jwt.getTokenValue());
    }
}