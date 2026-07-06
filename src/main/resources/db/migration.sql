-- 1. Tạo bảng cấu hình hệ thống
CREATE TABLE system_configs (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_at DATETIME NOT NULL
);

-- 2. Thêm cột unban_at (DATETIME) vào bảng customers
ALTER TABLE customers ADD COLUMN unban_at DATETIME DEFAULT NULL;

-- 3. Chèn cấu hình mặc định ban đầu
INSERT INTO system_configs (config_key, config_value, description, updated_at) VALUES 
('BAN_DAY', '7', 'Số ngày khóa tài khoản khi vi phạm hoàn hàng', NOW()),
('PERMANENT_BAN', 'false', 'Kích hoạt khóa vĩnh viễn (true/false)', NOW());
