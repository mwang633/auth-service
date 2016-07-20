CREATE OR REPLACE FUNCTION update_modified_time()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.modified_time = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION update_creation_time()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.creation_time = now();
  NEW.modified_time = now();
  RETURN NEW;
END;
$$ language 'plpgsql';


CREATE TABLE logins (
  user_email    VARCHAR(40) PRIMARY KEY,
  user_id  VARCHAR(40) NOT NULL,
  salt          VARCHAR(40) NOT NULL,
  password_hash VARCHAR(256) NOT NULL,
  creation_time TIMESTAMP,
  modified_time TIMESTAMP
);

CREATE TRIGGER update_logins_modified_time BEFORE INSERT ON logins FOR EACH ROW EXECUTE PROCEDURE update_creation_time();
CREATE TRIGGER update_logins_creation_time BEFORE UPDATE ON logins FOR EACH ROW EXECUTE PROCEDURE update_modified_time();


CREATE TABLE user_roles (
  user_id  VARCHAR(40),
  role          VARCHAR(40),
  creation_time TIMESTAMP,
  modified_time TIMESTAMP,
  PRIMARY KEY (user_id, role)
);

CREATE TRIGGER update_user_roles_modified_time BEFORE INSERT ON user_roles FOR EACH ROW EXECUTE PROCEDURE update_creation_time();
CREATE TRIGGER update_user_roles_creation_time BEFORE UPDATE ON user_roles FOR EACH ROW EXECUTE PROCEDURE update_modified_time();


CREATE TABLE permissions (
  role          VARCHAR(40),
  method        VARCHAR(20),
  path          VARCHAR,
  creation_time TIMESTAMP,
  modified_time TIMESTAMP,
  PRIMARY KEY (role, method, path)
);

CREATE TRIGGER update_permissions_modified_time BEFORE INSERT ON permissions FOR EACH ROW EXECUTE PROCEDURE update_creation_time();
CREATE TRIGGER update_permissions_creation_time BEFORE UPDATE ON permissions FOR EACH ROW EXECUTE PROCEDURE update_modified_time();