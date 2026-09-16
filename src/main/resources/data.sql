INSERT INTO tbl_roles (name, description) 
VALUES ('ROLE_ADMIN', 'Administrador General del Sistema'),
       ('ROLE_USER', 'Usuario Operativo de Compras')
ON CONFLICT (name) DO NOTHING;

-- Contraseña encriptada para Admin123!: $2a$10$CaalCgQfjLiUqdQ9e.MX..ztwIrI9USq57kGDj3KwdP4F0L/FqHqW
INSERT INTO tbl_users (username, email, password_hash, status, created_at, updated_at, created_by, updated_by)
VALUES ('admin', 'admin@buyio.com', '$2a$10$CaalCgQfjLiUqdQ9e.MX..ztwIrI9USq57kGDj3KwdP4F0L/FqHqW', 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM')
ON CONFLICT (username) DO NOTHING;

INSERT INTO tbl_users (username, email, password_hash, status, created_at, updated_at, created_by, updated_by)
VALUES ('operator', 'operator@buyio.com', '$2a$10$CaalCgQfjLiUqdQ9e.MX..ztwIrI9USq57kGDj3KwdP4F0L/FqHqW', 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM')
ON CONFLICT (username) DO NOTHING;

INSERT INTO tbl_users (username, email, password_hash, status, created_at, updated_at, created_by, updated_by)
VALUES ('usuario1', 'usuario1@buyio.com', '$2a$10$CaalCgQfjLiUqdQ9e.MX..ztwIrI9USq57kGDj3KwdP4F0L/FqHqW', 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM')
ON CONFLICT (username) DO NOTHING;

INSERT INTO tbl_user_roles (user_id, role_id)
SELECT u.id, r.id FROM tbl_users u, tbl_roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO tbl_user_roles (user_id, role_id)
SELECT u.id, r.id FROM tbl_users u, tbl_roles r 
WHERE u.username = 'operator' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

