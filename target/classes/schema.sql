-- Drop and recreate database
DROP DATABASE IF EXISTS leave_service_db;
CREATE DATABASE leave_service_db;
USE leave_service_db;

-- Create tables
CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password_hash VARCHAR(255),
    salting_value VARCHAR(255),
    role VARCHAR(50),
    leave_balance DECIMAL(10, 2),
    leave_refresh_date DATE,
    leave_refresh_amount DECIMAL(10, 2),
    is_default BOOLEAN DEFAULT TRUE
);

CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    FOREIGN KEY (employee_id) REFERENCES user_info(employee_id)
);

-- Insert mock data
INSERT INTO user_info (employee_id, first_name, last_name, password_hash, salting_value, role, leave_balance, leave_refresh_date, leave_refresh_amount, is_default) VALUES
(1001, 'John', 'Doe', 'XTNByqr8+SIUhoMAOVJ9Rx53+dZCjPZdKhtzy9RKaRY=', 'a2f4d2ca715947f1ab83ef985a50cb35', 'ADMIN', 20.0, '2025-01-01', 20.0, false),
(1002, 'John', 'Donut', 'Rwd+jHdQTEsWlom5anVPqAP4P25bOwerCfKexCCG6ao=', 'e4b98cbb0c244f03b09a09d7ca859d4b', 'MANAGER', 20.0, '2025-01-01', 20.0, false),
(1003, 'Robert', 'Swinderdale', 'm6Xx+mD7Vf6Q0dSQ1AJxNQ+izT/SeZwfP5yfQQircAo=', '6d0c528345424838ae26b9b0a9641db7', 'USER', 20.0, '2025-01-01', 20.0, true);

INSERT INTO leave_requests (employee_id, start_date, end_date, status) VALUES
(1001, '2024-02-01', '2024-02-05', 'APPROVED');


















-- get user data

USE leave_service_db;
SELECT * FROM user_info WHERE employee_id = 1002;






