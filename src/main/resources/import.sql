INSERT INTO roles(id, name) VALUES(1, 'ROLE_USER');
INSERT INTO roles(id, name) VALUES(2, 'ROLE_ADMIN');

INSERT INTO users(id, username, email, password, enabled) VALUES(1, 'admin', 'alex.pantec@gmail.com', '$2a$10$TB9YW0AYPqF1PSfMKrANAuvxLFZSYc2b7uJv0c3wZ8BM45zs1MBLO', 1);
INSERT INTO users(id, username, email, password, enabled) VALUES(2, 'Alice', 'alice@gmail.com', '$2a$10$TB9YW0AYPqF1PSfMKrANAuvxLFZSYc2b7uJv0c3wZ8BM45zs1MBLO', 1);
INSERT INTO users(id, username, email, password, enabled) VALUES(3, 'Bob', 'bob@gmail.com', '$2a$10$TB9YW0AYPqF1PSfMKrANAuvxLFZSYc2b7uJv0c3wZ8BM45zs1MBLO', 1);

INSERT INTO users_roles(user_id, role_id) VALUES(1, 1);
INSERT INTO users_roles(user_id, role_id) VALUES(1, 2);

INSERT INTO users_roles(user_id, role_id) VALUES(2, 1);

INSERT INTO users_roles(user_id, role_id) VALUES(3, 1);