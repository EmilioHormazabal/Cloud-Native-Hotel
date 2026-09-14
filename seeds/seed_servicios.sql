USE h_servicio;

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Habitacion Estandar', 'habitacion', 101, 'invitado', 'Habitacion con 2 camas individuales, bano privado y wifi incluido', 50000, 2, b'1'
WHERE NOT EXISTS (SELECT 1 FROM h_servicio.servicio WHERE id = 1);

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Suite VIP', 'habitacion', 201, 'VIP', 'Suite con cama king, jacuzzi, minibar y vista panoramica', 120000, 3, b'1'
WHERE NOT EXISTS (SELECT 1 FROM h_servicio.servicio WHERE id = 2);

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Buffet Desayuno', 'comida', 0, 'miembro', 'Desayuno buffet continental con frutas, panaderia y cafe', 15000, 50, b'1'
WHERE NOT EXISTS (SELECT 1 FROM h_servicio.servicio WHERE id = 3);

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Piscina Climatizada', 'recreativo', 0, 'miembro', 'Piscina techada con temperatura regulada y toallas', 10000, 30, b'1'
WHERE NOT EXISTS (SELECT 1 FROM h_servicio.servicio WHERE id = 4);