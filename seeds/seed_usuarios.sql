USE h_usuario;

-- Usuarios demo. Deben existir para que el BFF resuelva /usuario/me
-- por el claim preferred_username del token (login real es por Entra/MSAL)
INSERT INTO usuario (nombre, s_nombre, a_paterno, a_materno, rut, dv_rut, edad, tipo_usuario, correo, contrasenia, telefono)
SELECT 'Admin', NULL, 'Prueba', NULL, 11111111, '1', 30, 'ADMIN', 'admin.prueba@tenaninternoemihormazabal.onmicrosoft.com', 'demo', 900000001
WHERE NOT EXISTS (SELECT 1 FROM h_usuario.usuario WHERE correo = 'admin.prueba@tenaninternoemihormazabal.onmicrosoft.com');

INSERT INTO usuario (nombre, s_nombre, a_paterno, a_materno, rut, dv_rut, edad, tipo_usuario, correo, contrasenia, telefono)
SELECT 'Cliente', NULL, 'Prueba', NULL, 22222222, '2', 25, 'CLIENTE', 'cliente.prueba@tenaninternoemihormazabal.onmicrosoft.com', 'demo', 900000002
WHERE NOT EXISTS (SELECT 1 FROM h_usuario.usuario WHERE correo = 'cliente.prueba@tenaninternoemihormazabal.onmicrosoft.com');