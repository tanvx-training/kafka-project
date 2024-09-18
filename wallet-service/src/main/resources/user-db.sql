CREATE TABLE _user
(
    id              UUID PRIMARY KEY,                                                                      -- ID duy nhất cho mỗi người dùng
    name            VARCHAR(255)   NOT NULL,                                                               -- Tên người dùng
    email           VARCHAR(255)   NOT NULL,                                                               -- Email người dùng
    phone           VARCHAR(20)    NOT NULL,                                                               -- Số điện thoại người dùng
    account_balance DECIMAL(18, 2) NOT NULL,                                                               -- Số dư tài khoản của người dùng
    account_status  VARCHAR(50)    NOT NULL CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')), -- Trạng thái tài khoản với ràng buộc giá trị hợp lệ
    created_at      TIMESTAMP,                                                                             -- Thời gian tạo tài khoản
    updated_at      TIMESTAMP,                                                                             -- Thời gian cập nhật thông tin tài khoản
    CONSTRAINT unique_email UNIQUE (email)                                                                 -- Ràng buộc email duy nhất
);

CREATE TABLE _transaction
(
    id                 UUID PRIMARY KEY,                                                                         -- ID duy nhất cho mỗi giao dịch
    sender_user_id     UUID,                                                                                     -- Khóa ngoại liên kết với người gửi
    receiver_user_id   UUID,                                                                                     -- Khóa ngoại liên kết với người nhận
    amount             DECIMAL(18, 2) NOT NULL,                                                                  -- Số tiền giao dịch
    timestamp          TIMESTAMP      NOT NULL,                                                                  -- Thời gian giao dịch
    transaction_status VARCHAR(50)    NOT NULL CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED')), -- Trạng thái giao dịch với ràng buộc giá trị hợp lệ
    CONSTRAINT fk_sender FOREIGN KEY (sender_user_id) REFERENCES _ user (id) ON DELETE SET NULL,
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_user_id) REFERENCES _ user (id) ON DELETE SET NULL
);
