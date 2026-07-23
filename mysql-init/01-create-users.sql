-- mysql-init/01-create-users.sql
CREATE USER 'auth_user'@'%' IDENTIFIED BY 'auth_pass_local';
GRANT ALL PRIVILEGES ON cloudarchitecturedb.* TO 'auth_user'@'%';
FLUSH PRIVILEGES;