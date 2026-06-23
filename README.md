\# anhgagifcode - Hệ thống Gamification Đổi Quà \& Ấp Trứng



\## 1. Giới thiệu dự án (Project Overview)

Hệ thống khuyến mãi "Ấp Trứng Đổi Quà" tích hợp trực tiếp với nền tảng Sapo qua cơ chế Webhook nhằm mục đích kích cầu mua sắm và tự động hóa quy trình trao thưởng vật phẩm. Hệ thống giải quyết triệt để bài toán đặt đơn nhận quà rồi ngay lập tức hủy đơn hoặc hoàn hàng trên các nền tảng để trục lợi (bộ máy Anticheat), đồng thời tạo phễu chuyển đổi để điều hướng khách hàng từ các sàn thương mại điện tử (Shopee, TikTok Shop) sang mua sắm trực tiếp trên Website nội bộ của Sapo thông qua cơ chế rút ngắn thời gian chờ (Cooldown).



\## 2. Quy tắc Nghiệp vụ cốt lõi (Business Rules)

Mã đơn hàng gốc (`source\_identifier` hoặc `name`) từ dữ liệu Sapo bắn về được sử dụng làm Mã Code duy nhất để đổi quà trên hệ thống. Khách hàng không cần đăng ký tài khoản mà sử dụng chính Mã Code này làm token định danh (lưu tại `localStorage` của trình duyệt). Khi nhập Code hợp lệ, hệ thống cấp phát song song 2 loại trứng:

\* \*\*Trứng 1 (Nhận ngay):\*\*

&#x20;   \* \*Đơn từ Web Sapo:\* Kích hoạt ngay lập tức để lấy thông tin tài khoản ngẫu nhiên (Random Account Level 1).

&#x20;   \* \*Đơn từ Sàn TMĐT:\* Hệ thống giữ trạng thái chờ cho đến khi nhận được webhook xác nhận đơn hàng thành công/hoàn tất từ Sapo mới cho phép mở trứng.

\* \*\*Trứng 2 (Cần ấp):\*\*

&#x20;   \* \*Đơn từ Web Sapo:\* Thời gian đếm ngược (Cooldown) là \*\*2 ngày\*\*. Hết hạn hệ thống tự động duyệt, mở trứng và cấp phát tài khoản cấp cao.

&#x20;   \* \*Đơn từ Sàn TMĐT:\* Thời gian đếm ngược (Cooldown) là \*\*15 ngày\*\*. Hết hạn hệ thống tự động mở trứng. Quản trị viên (Admin) có đặc quyền duyệt thủ công để ép trứng nở sớm hơn tiến độ đếm ngược.

\* \*\*Cơ chế Chống gian lận (Anticheat):\*\* Hệ thống liên tục lắng nghe các sự kiện hủy đơn hoặc hoàn hàng từ Sapo (`orders/cancelled`, `refunds/create`). Nếu phát hiện đơn bị hủy trong lúc trứng đang ấp, tiến trình ấp lập tức bị đóng băng/hủy bỏ vĩnh viễn; thông tin tài khoản được trả về phía Client chỉ hiển thị duy nhất một lần để bảo mật kho quà.



\## 3. Công nghệ sử dụng (Tech Stack)

\* \*\*Backend Framework:\*\* Spring Boot 4.0.7 / Java 21

\* \*\*Build Tool:\*\* Maven

\* \*\*Database:\*\* MySQL 8.x

\* \*\*Key Dependencies:\*\*

&#x20;   \* `Spring Web`: Xây dựng các REST API và Endpoint tiếp nhận Webhook.

&#x20;   \* `Spring Data JPA`: Quản lý thực thể và tương tác cơ sở dữ liệu.

&#x20;   \* `MySQL Driver`: Trình điều khiển kết nối CSDL MySQL.

&#x20;   \* `Lombok`: Tự động cấu trúc mã boilerplate (Getter, Setter, Builder).

&#x20;   \* `Validation`: Xác thực tính hợp lệ của dữ liệu đầu vào.

&#x20;   \* `SpringDoc OpenAPI`: Tự động biên dịch tài liệu API (giao diện Swagger UI).

&#x20;   \* `Apache POI (poi-ooxml)`: Thư viện đọc/ghi file Excel hỗ trợ Admin import kho tài khoản (phân loại Tier A, B, C, D, độ hiếm và sản phẩm map tương ứng).



\## 4. Kiến trúc mã nguồn (Architecture)

Dự án áp dụng cấu trúc \*\*Kiến trúc Lục giác (Hexagonal Architecture / Ports and Adapters)\*\* nhằm tách biệt lõi nghiệp vụ khỏi sự phụ thuộc vào framework và các tác nhân công nghệ bên ngoài:

```text

src/main/java/com/project/anhgagifcode/

├── domain/               # Lõi nghiệp vụ tinh khiết (Entities, Domain Services)

│   ├── model/            # Thực thể nghiệp vụ (Order, Egg, Account)

│   └── service/          # Logic ấp trứng, thuật toán tính ngày, bộ máy Anticheat

├── application/          # Tầng Ports (Giao diện tích hợp hệ thống)

│   ├── ports/

│   │   ├── in/           # Inbound Ports (Use Cases định nghĩa cho API/Webhook gọi vào)

│   │   └── out/          # Outbound Ports (Định nghĩa interface kết nối DB/External)

├── infrastructure/       # Tầng Adapters (Triển khai chi tiết công nghệ)

│   ├── adapters/

│   │   ├── in/           # REST Controllers (Sapo Webhook Listener, Client/Admin API)

│   │   └── out/          # JPA Repositories (Thực thi kết nối trực tiếp MySQL DB)

│   └── config/           # Cấu hình Spring Boot, OpenAPI, Scheduled Task (Cronjob)

└── AnhgagifcodeApplication.java  

```

\## 5. Hướng dẫn thiết lập ban đầu (Setup \& Installation)

Yêu cầu hệ thống (Prerequisites)

&#x20; \* Java JDK 21 trở lên

&#x20; \* MySQL Server 8.x

&#x20; \* Maven 3.8+



Cấu hình Môi trường

Tạo hoặc cập nhật tệp `src/main/resources/application.properties` với các thông tin cấu hình sau:

```properties

\# Cấu hình Server Port

server.port=8080



\# Cấu hình kết nối CSDL MySQL

spring.datasource.url=jdbc:mysql://localhost:3306/anhgagifcode\_db?useSSL=false\&serverTimezone=UTC\&allowPublicKeyRetrieval=true

spring.datasource.username=YOUR\_MYSQL\_USERNAME

spring.datasource.password=YOUR\_MYSQL\_PASSWORD

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver



\# Cấu hình Hibernate / JPA

spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.format\_sql=true



\# Cấu hình thông tin kết nối Sapo Private App

sapo.api.key=YOUR\_SAPO\_API\_KEY

sapo.api.secret=YOUR\_SAPO\_API\_SECRET

sapo.api.base-url=\[https://wintersnow.mysapo.net/admin](https://wintersnow.mysapo.net/admin)

```



Khởi chạy Ứng dụng

Thực thi lệnh sau tại thư mục gốc của dự án để đóng gói và chạy ứng dụng:

```bash

mvn clean spring-boot:run

```



\## 6. Tài liệu API (API Documentation)

Sau khi ứng dụng khởi chạy thành công, hệ thống tự động xuất bản tài liệu đặc tả API và giao diện thử nghiệm tích hợp (Swagger UI) tại đường dẫn mặc định:



http://localhost:8080/swagger-ui/index.html



\## 7. Chiến lược Sao lưu \& Vận hành (Backup \& Maintenance)

Hệ thống tích hợp tiến trình quét ngầm thông qua tính năng @Scheduled để tự động kiểm tra, xử lý trạng thái trứng đến hạn và gán tài khoản ngẫu nhiên từ kho quà.



Cơ sở dữ liệu được thiết lập cơ chế tự động dump định kỳ hàng ngày để bảo toàn cấu trúc vận hành.

