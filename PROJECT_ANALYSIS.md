# Phân Tích Chi Tiết Cinema Booking Backend

Báo cáo phân tích toàn diện về kiến trúc, luồng hoạt động, cấu trúc dự án và các design pattern được sử dụng trong hệ thống Backend đặt vé xem phim hiện tại.

## 1. Tổng Quan Kiến Trúc (Architecture Overview)
Dự án được xây dựng dựa trên framework **Spring Boot** (Java), áp dụng mô hình kiến trúc **N-Tier (N-Layer Architecture)** bao gồm các tầng chính:
- **Presentation Layer (Controllers):** Tiếp nhận các request HTTP từ client, routing và phản hồi kết quả (REST API).
- **Business Logic Layer (Services/Impl):** Chứa các logic nghiệp vụ phức tạp của ứng dụng.
- **Data Access Layer (Repositories):** Tương tác với cơ sở dữ liệu (sử dụng Spring Data JPA).
- **Model/Entity Layer:** Các class đại diện cho các bảng trong cơ sở dữ liệu.

## 2. Cấu Trúc Thư Mục (Directory Structure)
Mã nguồn đặt tại `src/main/java/com/movie/cinema_booking_backend/`:
- `config/`: Các thiết lập cấu hình của hệ thống, bao gồm `DataInitializer` và package `security/` (chứa cấu hình bảo mật, xử lý JWT).
- `controller/`: Các REST API Endpoint.
- `entity/`: Các Hibernate/JPA Entities mapping thành các bảng CSDL.
- `repository/`: Các Interface Controller Data (extends JpaRepository).
- `service/`: Bao gồm các interface dịch vụ (bắt đầu bằng `I...`) và thư mục `impl/` chứa class implement. Dùng để triển khai Dependency Inversion.
- `request/` & `response/`: Các class DTO (Data Transfer Objects), định nghĩa dữ liệu đầu vào/ra.
- `exception/`: Nơi xử lý các lỗi toàn cục (`GlobalExceptionHandler`, định nghĩa `ErrorCode`, `AppException`).
- `enums/`: Các Enumeration cho trạng thái như `BookingStatus`, `MovieStatus`, `Role`, v.v.

## 3. Luồng Hoạt Động Hệ Thống (Application Workflow)

### 3.1 Luồng Xử Lý Một Request
1. **Client / Frontend** gửi HTTP request (GET, POST, PUT, DELETE) kèm theo access token (JWT) (ngoại trừ các public endpoint).
2. **Security Filter Chain** (cấu hình trong `SecurityConfig` và `CustomJwtDecoder`) sẽ kiểm tra tính hợp lệ của JWT token. 
    - Nếu token sai hoặc lỗi: `JwtAuthenticationEntryPoint` được gọi -> trả về lỗi chuẩn `UNAUTHENTICATED`.
    - Nếu có token nhưng user đó không đủ quyền truy cập (vd User nhưng truy cập API của Admin): `CustomAccessDeniedHandler` được gọi -> trả về `ACCESS_DENIED`.
3. Nếu đi qua được filter, **Controller** tương ứng (`UserController`, `BookingController`, v.v.) sẽ validate input qua các file trong `request/` (thường có Annotation Validate của Jakarta).
4. **Service** lấy request đó, xử lý qua nghiệp vụ (có thể ném ra `AppException` nếu logic bị sai).
5. **Repository** tương tác đến Database để tạo, tìm hoặc sửa thông tin.
6. Kết quả từ database sẽ được wrap thành đối tượng DTO trong `response/` và bọc vào `ApiResponse<T>`.

### 3.2 Các Chức Năng Chính Phân Tích Được (Features)
- **Auth & Account (Xác thực và Quản lý Tài khoản):** Đăng nhập, đăng ký, OTP. Xử lý token JWT bằng khoá bảo mật. Hỗ trợ logout qua việc đẩy token lại vào `InvalidatedToken` ở CSDL (cơ chế Token Blacklist). Phân quyền (`Role`).
- **User (Người dùng):** Quản lý thông tin profile.
- **Genre (Thể loại):** Quản lý (CRUD) các thể loại phim.
- **ExtraService (Dịch vụ thêm):** Quản lý các dịch vụ đi kèm khi mua vé (bắp, nước, v.v).
- **Promotion (Khuyến mãi):** Quản lý bảng giảm giá, khuyến mãi cho hệ thống báo giá vé và thức ăn.
- *Các Entity chờ phát triển Service & Controller bao gồm:* `Movie`, `Showtime`, `Auditorium`, `Seat`, `Ticket`, `Booking`, `Payment`, `MovieReview`. (Dựa trên Entities đã có sẵn).

## 4. Design Pattern Trong Hệ Thống

Dự án áp dụng nhiều Design Pattern chuẩn để codebase được module hóa và dễ bảo trì:

### 4.1 Dependency Injection / Inversion Of Control (IoC)
- Spring Container quản lý lifecycle vòng đời của tất cả các Components (`@RestController`, `@Service`, `@Repository`, `@Component`). Không khởi tạo đối tượng bằng từ khóa `new` mà inject thông qua Constructor hoặc Field Injection.

### 4.2 Builder Pattern
- Nhìn thấy rõ nhất ở `ApiResponse`. Hệ thống sử dụng một inner Builder class (`new ApiResponse.Builder<>().success(false).message(...).build()`). Điều này hỗ trợ việc construct ra một đối tượng API thống nhất mà không cần truyền quá nhiều tham số không cần thiết.

### 4.3 DTO (Data Transfer Object)
- Dự án sử dụng rạch ròi giữa Entity và Payload trả về qua `request/` và `response/`. Nó giới hạn chỉ những trường dữ liệu nào cần trả vể từ phía Client mới được lộ ra để nhằm mục đích bảo mật.

### 4.4 Strategy Pattern (Tiềm năng)
- Hệ thống hỗ trợ nhiều Method xử lý như `PaymentMethod` hay `DiscountType`, ta có thể tạo các strategy con cho từng tính năng tính chiết khấu / cổng thanh toán (VNPay, Momo, Cash) thay vì đặt `if...else` rườm rà.

### 4.5 Data Access Object (DAO) Pattern / Repository Pattern
- Áp dụng qua Spring Data JPA `Repository`. Giấu cách implement gọi SQL Database của `Hibernate` bên dưới, mang lại cách giao tiếp thuần Java qua phương thức `save`, `findById`, ...

### 4.6 Singleton
- Tất cả các Bean được đăng ký trên IoC mặc định là Singleton Pattern, nó dùng chung trong suốt vòng đời web framework khởi chạy.

## 5. Hướng Dẫn Phát Triển Tiếp Theo (Next Steps)
Hiện tại kiến trúc của backend đã được setup sẵn các tầng cấu hình chuẩn mực: Logging, Exception Handling, Security Token JWT.
1. Cần tiếp tục mở rộng phát triển các CRUD cho `Movie`, `Showtime`, `Seat` setup.
2. Xây dựng Controller Logic của chức năng Booking và BookingExtra. Quản lý transactional khi xử lý đồng thời (Concurrent Request Handling cho việc đặt ghế tránh trùng lặp).
3. Đấu nối `PaymentController` thông qua VNPay/ZaloPay.
4. Viết Unit Test cho từng tầng nghiệp vụ và tự động hóa quy trình với CI/CD (nếu có).

