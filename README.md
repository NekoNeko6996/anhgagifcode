# anhgagifcode - Hệ thống Gamification Đổi Quà & Ấp Trứng (KiotViet Integration)

Hệ thống khuyến mãi **"Ấp Trứng Đổi Quà"** tích hợp trực tiếp với nền tảng quản lý bán hàng **KiotViet** qua cơ chế gọi API đồng bộ hóa đơn. Hệ thống được thiết kế để tự động hóa quy trình phát thưởng quà tặng số (ví dụ: Tài khoản Premium, Giftcode...) nhằm kích cầu mua sắm, đồng thời ngăn chặn triệt để hành vi trục lợi (đơn phương hủy đơn hoặc hoàn hàng để lấy quà) thông qua bộ máy **Anticheat** tích hợp và chính sách đối soát nghiêm ngặt.

---

## 1. Quy tắc Nghiệp vụ cốt lõi (Business Rules)

### 1.1. Xác thực đơn hàng & Tra cứu thông minh
* Mã đơn hàng trên KiotViet (ví dụ: `HDTTS_260628ABEF6676` hoặc `HDSPE_260628ABEF6676`) được sử dụng làm **Mã Code duy nhất** để đổi quà trên hệ thống.
* Khách hàng sử dụng chính Mã Code này làm token định danh mà không cần đăng ký tài khoản (dữ liệu được lưu trữ tại `localStorage` của trình duyệt phía Client).
* **Tìm kiếm thông minh (Suffix Matching)**: Hệ thống cho phép khách hàng nhập phần đuôi của đơn hàng (ví dụ: `260628ABEF6676`). Backend sẽ tự động quét danh sách tiền tố trong cơ sở dữ liệu (`HDTTS`, `HDSPE`, `DHTTS`, `DHSPE`...) để tìm kiếm đơn hàng trùng khớp trên KiotViet.

### 1.2. Xếp hạng độ tin cậy của Khách hàng (Trust System)
Mỗi khách hàng được theo dõi dựa trên mã khách hàng KiotViet (`customerCode`) và được gán 1 trong các trạng thái sau:
* **`NEW`**: Khách hàng mới.
* **`TRUSTED_1`**: Khách hàng đã giao thành công $\ge$ 2 đơn hàng.
* **`TRUSTED_2`**: Khách hàng đã giao thành công $\ge$ 5 đơn hàng.
* **`WARNING`**: Khách hàng có 1 đơn hàng bị hoàn/trả (`returnStreak == 1`).
* **`BANNED`**: Khách hàng có $\ge$ 2 đơn hàng bị hoàn/trả liên tiếp (`returnStreak >= 2`). Khi bị BANNED, hệ thống lập tức khóa quyền đồng bộ hóa đơn và mở trứng của khách hàng này.

### 1.3. Cơ chế Ấp trứng & Mở trứng nhận quà
Khi khách hàng đồng bộ đơn hàng đã **Giao thành công** từ KiotViet, hệ thống sẽ ánh xạ danh sách sản phẩm trong hóa đơn sang các **Bể Quà (Gift Pool)** được cấu hình trước để sinh ra trứng tương ứng:
* **Trứng Loại 1 (Nhận ngay - Egg Type 1)**:
  * *Đối với khách hàng AN TOÀN (`returnStreak == 0`)*: Không có thời gian chờ, trạng thái là `READY_TO_CLAIM`, có thể mở trứng để nhận quà ngay lập tức.
  * *Đối với khách hàng CẢNH CÁO (`returnStreak == 1`)*: Bắt buộc phải ấp trứng trong **15 ngày** (trạng thái `HATCHING`).
* **Trứng Loại 2 (Cần ấp - Egg Type 2)**:
  * Bất kể trạng thái khách hàng nào, trứng này luôn có thời gian đếm ngược (cooldown) là **15 ngày** (trạng thái `HATCHING`).
* **Điều kiện nhận quà (Claim Reward)**:
  * Trứng loại 1 (an toàn) được mở ngay lập tức nếu đơn hàng không bị hoàn/trả.
  * Trứng loại 2 (hoặc trứng loại 1 của khách hàng bị WARNING) chỉ có thể mở khi:
    1. Đã hết thời gian ấp trứng (vượt qua `hatchAt`).
    2. Đơn hàng đạt trạng thái **Thành công tuyệt đối (Absolute Success)**: Đơn hàng đã giao thành công và đã trôi qua **15 ngày** (để đảm bảo khách hàng không còn quyền yêu cầu hoàn trả hoặc chuyển hoàn hàng hóa theo chính sách sàn).

### 1.4. Cơ chế Chống gian lận (Anticheat Engine)
* Hệ thống tự động theo dõi trạng thái đơn hàng khi người dùng thao tác. Nếu phát hiện đơn bị cập nhật trạng thái `"Đang chuyển hoàn"` hoặc `"Đã chuyển hoàn"`, hệ thống lập tức chuyển toàn bộ trứng liên kết với đơn đó sang trạng thái **`CANCELLED`** vĩnh viễn, ngăn không cho mở lấy quà.
* Tài khoản quà tặng sau khi được gán thành công cho trứng của khách hàng sẽ được đánh dấu là `ASSIGNED` và thông tin tài khoản/mật khẩu chỉ trả về client **duy nhất một lần** để bảo mật kho quà.

### 1.5. Cơ chế Ân xá (Amnesty System)
* Khách hàng đang ở trạng thái `WARNING` (`returnStreak == 1`) có cơ hội phục hồi trạng thái tin cậy ban đầu.
* **Quy tắc**: Nếu khách hàng phát sinh các đơn hàng mới sau thời điểm bị hoàn trả và thực hiện **mở thành công tuyệt đối tất cả các trứng của ít nhất 2 đơn hàng liên tiếp**, hệ thống sẽ tự động xóa streak vi phạm (`returnStreak` reset về 0) và khôi phục trạng thái tin cậy (`NEW`, `TRUSTED_1`, hoặc `TRUSTED_2` tùy thuộc vào tổng số đơn thành công).

---

## 2. Công nghệ sử dụng (Tech Stack)

* **Backend Framework**: Spring Boot 4.1.x / Java 21
* **Build Tool**: Maven 3.9+
* **Database**: MySQL 8.x / MariaDB
* **Thư viện chính**:
  * `Spring WebMVC`: Xây dựng các REST APIs.
  * `Spring Data JPA`: Quản lý thực thể và kết nối CSDL qua Hibernate (sử dụng MariaDBDialect).
  * `Spring Security`: Quản lý cấu hình phân quyền và mã hóa mật khẩu.
  * `JJWT`: Tạo và xác thực JWT Tokens phục vụ đăng nhập Admin.
  * `SpringDoc OpenAPI`: Tài liệu hóa APIs dưới dạng Swagger UI.
  * `Thymeleaf`: Xây dựng giao diện web cho trang quản trị Admin.
  * `Apache POI (poi-ooxml)`: Hỗ trợ đọc file Excel `.xlsx` để import tài khoản quà tặng số lượng lớn.
  * `Bucket4j` & `Caffeine`: Quản lý giới hạn tần suất yêu cầu (Rate Limiting) trên bộ nhớ cache.
  * `MapStruct`: Tự động ánh xạ (mapping) giữa các Entity và DTO.

---

## 3. Kiến trúc mã nguồn (Hexagonal Architecture)

Dự án áp dụng cấu trúc **Kiến trúc Lục giác (Ports and Adapters)** nhằm tách biệt lõi nghiệp vụ khỏi sự phụ thuộc vào các thư viện bên ngoài và cơ sở dữ liệu:

```text
src/main/java/com/project/anhgagifcode/
├── domain/                         # Lõi nghiệp vụ tinh khiết (Pure Domain)
│   ├── model/                      # Thực thể nghiệp vụ (Egg, Customer, KiotvietOrder...)
│   └── exception/                  # Các ngoại lệ nghiệp vụ (Business exceptions)
├── application/                    # Tầng Ports chứa Use Case logic kết nối
│   ├── port/
│   │   ├── in/                     # Inbound Ports (Interfaces định nghĩa cho Web gọi vào)
│   │   │   └── dto/                # Data Transfer Objects trao đổi dữ liệu
│   │   └── out/                    # Outbound Ports (Interfaces kết nối Database/External APIs)
│   └── service/                    # Cài đặt Use Cases (SyncKiotvietOrderService, ClaimEggService...)
├── infrastructure/                 # Tầng Adapters (Chi tiết triển khai công nghệ)
│   ├── adapter/
│   │   ├── in/                     # Inbound Adapters (Thymeleaf Controllers, REST API Controllers)
│   │   └── out/                    # Outbound Adapters (JPA Repositories, KiotViet API Client)
│   ├── config/                     # Cấu hình Spring Boot (OpenAPI, MVC, Security, DataInitializer)
│   └── security/                   # Triển khai bảo mật (JWT Filter, Rate Limiter Interceptor)
└── AnhgagifcodeApplication.java     # Lớp khởi chạy ứng dụng
```

---

## 4. Các tính năng của Trang Quản trị (Admin Dashboard)

Hệ thống cung cấp một trang quản trị toàn diện dành cho Admin tại đường dẫn `/admin` (Sử dụng Spring Security bảo vệ, lưu thông tin phiên làm việc qua JWT Token):
1. **Tổng quan (Dashboard)**: Thống kê tổng số lượng đơn hàng, số trứng đã phát sinh/đã mở/bị hủy, số lượng khách hàng vi phạm (WARNING, BANNED), số tài khoản còn lại trong kho quà.
2. **Quản lý Trứng (Eggs Management)**: Danh sách toàn bộ trứng trong hệ thống kèm trạng thái chi tiết, thời gian ấp và tài khoản quà tặng tương ứng.
3. **Quản lý Khách hàng (Customers Management)**: Theo dõi danh sách khách hàng, số đơn thành công, streak hoàn đơn và cho phép Admin can thiệp mở khóa (Unban) hoặc chỉnh sửa trạng thái trực tiếp.
4. **Quản lý Kho Quà (Gift Accounts)**:
   * Xem và chỉnh sửa chi tiết tài khoản quà tặng.
   * Thêm thủ công từng tài khoản.
   * **Import hàng loạt từ Excel**: Tải lên tệp Excel (`.xlsx`) chứa danh sách tài khoản (Username, Password, Platform, Token) để tự động nạp vào cơ sở dữ liệu.
   * Xóa hàng loạt tài khoản quà tặng.
5. **Quản lý Bể Quà (Gift Pools)**: Tạo các bể chứa tài khoản phân loại theo Tier (Tier A, Tier B, Tier C, Tier D...) tương ứng với giá trị hoặc độ hiếm của quà tặng. Liên kết tài khoản quà tặng vào các bể.
6. **Bản đồ Sản phẩm (Products Mapping)**: 
   * Đồng bộ hóa danh mục sản phẩm từ KiotViet API về hệ thống.
   * Tạo liên kết ánh xạ (Mapping) từ một sản phẩm KiotViet sang một bể quà chỉ định và chọn loại trứng phát sinh (Loại 1 hay Loại 2).
7. **Cấu hình hệ thống (Settings)**: Cho phép thay đổi thông tin đăng nhập của Admin (Username, Password).

---

## 5. Hướng dẫn thiết lập ban đầu (Setup & Installation)

### 5.1. Yêu cầu hệ thống
* **Java JDK 21** trở lên.
* **MySQL 8.x** hoặc **MariaDB**.
* **Maven 3.9+**.

### 5.2. Cấu hình tệp ứng dụng
Tạo hoặc cập nhật tệp cấu hình `src/main/resources/application.properties` tại thư mục dự án:

```properties
spring.application.name=anhgagifcode

# 1. Cấu hình kết nối Cơ sở dữ liệu MySQL/MariaDB
spring.datasource.url=jdbc:mysql://localhost:3306/anhgagiftcode?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=CONVERT_TO_NULL
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 2. Cấu hình JPA & Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
spring.jpa.open-in-view=false

# 3. Khóa bí mật JWT dùng cho tài khoản Admin
jwt.secret=9810283027109371273812703890112312839719742

# 4. Cấu hình tích hợp API KiotViet (Lấy thông tin từ trang quản trị KiotViet)
kiotviet.api.url=https://public.kiotapi.com
kiotviet.api.retailer=YOUR_KIOTVIET_RETAILER_NAME
kiotviet.api.client-id=YOUR_KIOTVIET_CLIENT_ID
kiotviet.api.client-secret=YOUR_KIOTVIET_CLIENT_SECRET
```

### 5.3. Khởi tạo tài khoản mặc định
Khi khởi chạy ứng dụng lần đầu tiên, hệ thống sẽ kiểm tra bảng quản trị viên. Nếu chưa có tài khoản nào, `DataInitializer` sẽ tự động tạo một tài khoản **Super Admin** mặc định:
* **Username**: `admin`
* **Password**: `admin123`

*(Quản trị viên nên đăng nhập vào đường dẫn `/admin/login` và thay đổi mật khẩu ngay lập tức tại mục Settings).*

### 5.4. Biên dịch và khởi chạy
Thực hiện lệnh sau tại thư mục gốc của dự án để tải dependencies, biên dịch và chạy ứng dụng:
```bash
mvn clean spring-boot:run
```

---

## 6. Tài liệu API & Tối ưu hóa kỹ thuật

### 6.1. Tài liệu API (Swagger UI)
Sau khi ứng dụng khởi chạy, tài liệu đặc tả API và giao diện tương tác thử nghiệm được cung cấp công khai tại:
* **Đường dẫn**: `http://localhost:8080/swagger-ui/index.html`

### 6.2. Chống Spam & Brute-force (Rate Limiting)
* Bộ lọc `RateLimitInterceptor` được cấu hình sử dụng `Bucket4j` để giới hạn tần suất gửi yêu cầu lên các API Client nhạy cảm (`/api/eggs/sync` và `/api/eggs/claim`).
* **Giới hạn**: Tối đa **3 yêu cầu trong vòng 1 phút** trên mỗi địa chỉ IP. Mọi yêu cầu vượt hạn mức sẽ nhận phản hồi lỗi `429 Too Many Requests`.

### 6.3. Tối ưu hóa hiệu năng đồng thời (High Concurrency & Lock Handling)
* Khi hàng loạt khách hàng tiến hành mở trứng nhận quà cùng lúc, hệ thống bốc thăm tài khoản quà tặng từ Bể Quà dễ gặp tình trạng tranh chấp dữ liệu (deadlock).
* Hệ thống giải quyết triệt để bài toán này bằng cách sử dụng native query tích hợp từ khóa **`SKIP LOCKED`** (qua hàm `pickAvailableAccountForUpdateSkipLocked` của tầng Persistence):
  ```sql
  SELECT * FROM gift_accounts 
  WHERE gift_pool_id = :poolId AND status = 'AVAILABLE' 
  LIMIT 1 FOR UPDATE SKIP LOCKED
  ```
* Cơ chế này giúp khóa bản ghi quà tặng được chọn và tự động bỏ qua các bản ghi đang bị luồng khác khóa, giúp hệ thống hoạt động mượt mà với hiệu suất tối đa dưới tải trọng lớn.