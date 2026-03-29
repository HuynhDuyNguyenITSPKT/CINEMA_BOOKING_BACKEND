# First Phase — Implementation Log
> **Cinema Booking Backend | Phase 1: Foundation CRUD**
> Ngày thực hiện: 2026-03-29

---

## Tổng Quan Phase 1

Phase 1 tập trung vào việc dựng hạ tầng CRUD cơ bản **không có pattern**. Mục tiêu là tạo đủ entity/service/controller để data flow hoạt động và có thể test qua Postman. Các design pattern sẽ được đưa vào từ Phase 2 trở đi.

---

## 1. Thao Tác Đã Thực Hiện

### 1.1 Tạo `AuditoriumStatus.java` (Enum mới)

**File:** `src/main/java/.../enums/AuditoriumStatus.java`

```java
public enum AuditoriumStatus {
    ACTIVE,
    UNDER_MAINTENANCE,
    INACTIVE
}
```

**Giải thích:**
- `ACTIVE`: Phòng đang hoạt động, có thể tạo Showtime.
- `UNDER_MAINTENANCE`: Đang bảo trì tạm thời.
- `INACTIVE`: Ngừng hoạt động dài hạn.

> **Tại sao cần enum này ngay ở Phase 1?**
> Vì entity `Auditorium` cần field `status` khi insert vào DB. Nếu để Phase 4 mới tạo, sẽ phải migration lại schema. Tiện tạo luôn để field `@Enumerated(STRING)` hoạt động ngay.
> Phase 4 sẽ dùng enum này với **State Pattern** để guard việc tạo Showtime.

---

### 1.2 Cập Nhật Entity `Auditorium.java`

**Thay đổi:** Thêm field `status` kiểu `AuditoriumStatus`.

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
@Builder.Default
private AuditoriumStatus status = AuditoriumStatus.ACTIVE;
```

**Giải thích:**
- `@Enumerated(EnumType.STRING)`: lưu giá trị dạng chuỗi (`"ACTIVE"`) thay vì số thứ tự (0, 1, 2) — dễ đọc trong DB, tránh bug khi thêm/sắp xếp lại enum.
- `@Builder.Default`: cần thiết vì class dùng Lombok `@Builder`. Nếu không có, Lombok Builder sẽ khởi tạo field = `null` thay vì `ACTIVE`.
- `nullable = false`: đảm bảo DB constraint.

---

### 1.3 Tạo `AuditoriumRepository.java`

**File:** `src/main/java/.../repository/AuditoriumRepository.java`

```java
boolean existsByName(String name);
boolean existsByNameAndIdNot(String name, String id);
List<Auditorium> findAllByStatus(AuditoriumStatus status);
```

**Giải thích:**
- `existsByName`: kiểm tra trùng tên khi **tạo mới**.
- `existsByNameAndIdNot`: kiểm tra trùng tên khi **cập nhật** (loại trừ chính record đang sửa).
- `findAllByStatus`: lọc phòng chiếu theo trạng thái, chuẩn bị cho Phase 4 State Pattern.

---

### 1.4 Tạo `SeatTypeRepository.java`

```java
boolean existsByName(String name);
boolean existsByNameAndIdNot(String name, String id);
```

Tương tự Auditorium — validation tên trùng lặp.

---

### 1.5 Tạo `SeatRepository.java`

```java
List<Seat> findByAuditoriumId(String auditoriumId);

@Query("SELECT s FROM Seat s JOIN FETCH s.seatType WHERE s.auditorium.id = :auditoriumId")
List<Seat> findByAuditoriumIdWithSeatType(@Param("auditoriumId") String auditoriumId);
```

**Giải thích quan trọng — `JOIN FETCH`:**
- Nếu chỉ dùng `findByAuditoriumId()` thông thường, khi map sang `SeatResponse` gọi `seat.getSeatType().getName()` → Hibernate sẽ bắn thêm N query (1 query/ghế) để lazy-load SeatType. Gọi là **N+1 problem**.
- `JOIN FETCH` solve vấn đề này bằng cách load SeatType cùng 1 query.
- Phase 2 Proxy sẽ dùng method này thay vì method thô.

---

### 1.6 Tạo DTOs

#### `AuditoriumRequest.java`
Fields: `name` (NotBlank), `seatCount` (Min=1), `status` (NotNull, default ACTIVE).

#### `AuditoriumResponse.java`
Fields: `id`, `name`, `seatCount`, `status`.

#### `SeatTypeRequest.java`
Fields: `name` (NotBlank), `surcharge` (Min=0, float).

#### `SeatTypeResponse.java`
Fields: `id`, `name`, `surcharge`.

#### `SeatResponse.java`
Fields: `id`, `name`, `rowIndex`, `columnIndex`, `seatTypeId`, `seatTypeName`, `seatTypeSurcharge`, `status`.

**Giải thích `status = null` trong Phase 1:**
Field `status` trong `SeatResponse` được giữ nguyên `null` ở Phase 1 vì:
- Phase 1 chỉ query DB, chưa biết ghế có bị lock trong RAM không.
- Phase 2 `SeatValidationProxy` sẽ nhận list này, kiểm tra `SeatLockRegistry` (Singleton), rồi set status = `"AVAILABLE"` / `"LOCKED"` / `"BOOKED"`.
- Frontend biết trước field tồn tại → không thay đổi contract API giữa Phase 1 và 2.

---

### 1.7 Tạo Service Interfaces

#### `IAuditoriumService.java`
```java
List<AuditoriumResponse> getAllAuditoriums();
AuditoriumResponse getAuditoriumById(String id);
AuditoriumResponse createAuditorium(AuditoriumRequest request);
AuditoriumResponse updateAuditorium(String id, AuditoriumRequest request);
void deleteAuditorium(String id);
```

#### `ISeatTypeService.java`
CRUD tương tự.

#### `ISeatService.java`
```java
List<SeatResponse> getSeatsByAuditorium(String auditoriumId);
```

**Giải thích thiết kế `ISeatService`:**
Interface này được thiết kế để **tái sử dụng ở Phase 2 với Proxy Pattern**.
- `SeatServiceImpl` implements `ISeatService` → "Real Subject".
- `SeatValidationProxy` cũng implements `ISeatService` + `@Primary` → Spring inject proxy thay cho real.
- `SeatController` inject `ISeatService` → auto-switch sang Proxy trong Phase 2 **không cần sửa controller**.

---

### 1.8 Tạo Service Implementations

#### `AuditoriumServiceImpl.java`
- `createAuditorium`: check `existsByName` → throw `AUDITORIUM_NAME_EXISTS` nếu trùng.
- `updateAuditorium`: check `existsByNameAndIdNot` → tránh false positive khi update cùng tên.
- `deleteAuditorium`: hard delete. Phase 5 có thể đổi thành soft delete (thêm `isDeleted` field).
- Map Entity → Response bằng private `toResponse()` helper — tránh lặp code.

#### `SeatTypeServiceImpl.java`
Tương tự Auditorium.

#### `SeatServiceImpl.java`
- Validate auditorium tồn tại trước khi query seats.
- Dùng `findByAuditoriumIdWithSeatType()` (JOIN FETCH) để tránh N+1.
- Đánh dấu `status = null` trong toResponse() — comment rõ "Phase 2 Proxy sẽ enrich".
- **Không đặt `@Primary`** — để Phase 2 Proxy có thể inject trực tiếp `SeatServiceImpl` mà không bị circular dependency.

---

### 1.9 Tạo Controllers

#### `AuditoriumController.java`
| Endpoint | Access |
|---|---|
| `GET /api/auditoriums` | Public |
| `GET /api/auditoriums/{id}` | Public |
| `POST /api/admin/auditoriums` | Admin only |
| `PUT /api/admin/auditoriums/{id}` | Admin only |
| `DELETE /api/admin/auditoriums/{id}` | Admin only |

#### `SeatTypeController.java`
| Endpoint | Access |
|---|---|
| `GET /api/seat-types` | Public |
| `GET /api/seat-types/{id}` | Public |
| `POST /api/admin/seat-types` | Admin only |
| `PUT /api/admin/seat-types/{id}` | Admin only |
| `DELETE /api/admin/seat-types/{id}` | Admin only |

#### `SeatController.java`
| Endpoint | Access |
|---|---|
| `GET /api/auditoriums/{id}/seats` | Public |

**Giải thích pattern phân quyền:**
- URL `/api/admin/*` → chỉ admin truy cập (Security config đã có sẵn trong project).
- URL `/api/*` (không có admin) → public hoặc user đã xác thực.
- Thống nhất với pattern của `ExtraServiceController` hiện có.

---

### 1.10 Cập Nhật `ErrorCode.java`

Thêm 12 error code mới:

| Code | Enum | Mô tả | HTTP Status |
|---|---|---|---|
| 1028 | `AUDITORIUM_NOT_FOUND` | Phòng chiếu không tồn tại | 404 |
| 1029 | `AUDITORIUM_NAME_EXISTS` | Tên phòng chiếu đã tồn tại | 400 |
| 1030 | `AUDITORIUM_NOT_ACTIVE` | Phòng chiếu không active | 400 |
| 1031 | `SEAT_TYPE_NOT_FOUND` | Loại ghế không tồn tại | 404 |
| 1032 | `SEAT_TYPE_NAME_EXISTS` | Tên loại ghế đã tồn tại | 400 |
| 1033 | `SEAT_NOT_FOUND` | Ghế không tồn tại | 404 |
| 1034 | `SEAT_ALREADY_TAKEN` | Ghế đã bị đặt/lock | 409 |
| 1035 | `BOOKING_NOT_FOUND` | Đơn đặt vé không tồn tại | 404 |
| 1036 | `TICKET_NOT_FOUND` | Vé không tồn tại | 404 |
| 1037 | `TICKET_ALREADY_USED` | Vé đã dùng | 409 |
| 1038 | `TICKET_CANCELLED` | Vé đã huỷ | 409 |
| 1039 | `TICKET_NOT_PAID` | Vé chưa thanh toán | 400 |

**Giải thích HTTP 409 Conflict cho SEAT_ALREADY_TAKEN, TICKET_*:**
- 409 Conflict phù hợp hơn 400 BadRequest vì không phải request sai format — mà là **xung đột trạng thái tài nguyên**.
- VD: Client muốn check-in vé đã dùng → vé tồn tại, request hợp lệ, nhưng trạng thái hiện tại xung đột với hành động.

---

## 2. Cấu Trúc File Đã Tạo

```
src/main/java/com/movie/cinema_booking_backend/
│
├── enums/
│   └── AuditoriumStatus.java           [NEW]
│
├── entity/
│   └── Auditorium.java                 [MODIFIED - thêm field status]
│
├── repository/
│   ├── AuditoriumRepository.java       [NEW]
│   ├── SeatTypeRepository.java         [NEW]
│   └── SeatRepository.java             [NEW]
│
├── request/
│   ├── AuditoriumRequest.java          [NEW]
│   └── SeatTypeRequest.java            [NEW]
│
├── response/
│   ├── AuditoriumResponse.java         [NEW]
│   ├── SeatTypeResponse.java           [NEW]
│   └── SeatResponse.java               [NEW]
│
├── service/
│   ├── IAuditoriumService.java         [NEW]
│   ├── ISeatTypeService.java           [NEW]
│   ├── ISeatService.java               [NEW]
│   └── impl/
│       ├── AuditoriumServiceImpl.java  [NEW]
│       ├── SeatTypeServiceImpl.java    [NEW]
│       └── SeatServiceImpl.java        [NEW]
│
├── controller/
│   ├── AuditoriumController.java       [NEW]
│   ├── SeatTypeController.java         [NEW]
│   └── SeatController.java             [NEW]
│
└── exception/
    └── ErrorCode.java                  [MODIFIED - thêm 12 error codes]
```

---

## 3. Luồng Dữ Liệu Phase 1

```
Request → Controller → Service Interface → ServiceImpl → Repository → DB
                                ↓
                          Response DTO ←
```

Ví dụ tạo phòng chiếu:
```
POST /api/admin/auditoriums
  → AuditoriumController.createAuditorium(request)
  → IAuditoriumService.createAuditorium(request)
  → AuditoriumServiceImpl: check name duplicate
  → AuditoriumRepository.save(entity)
  → AuditoriumResponse { id, name, seatCount, status }
```

---

## 4. Chuẩn Bị Cho Phase 2 (Điểm Giao Tiếp)

Các điểm trong Phase 1 đã được chuẩn bị sẵn để Phase 2 plug in:

| Thành phần | Phase 1 | Phase 2 sẽ làm |
|---|---|---|
| `ISeatService` | Interface | `SeatValidationProxy` implement `@Primary` |
| `SeatServiceImpl` | `@Service` (không Primary) | Được inject vào Proxy làm "real" |
| `SeatResponse.status` | `null` | Proxy enrich `AVAILABLE/LOCKED/BOOKED` |
| `SeatController` | Inject `ISeatService` | Tự động dùng Proxy |
| `ErrorCode.SEAT_ALREADY_TAKEN` | Đã khai báo | Phase 2 Proxy throw |

---

## 5. Ghi Chú Kỹ Thuật Quan Trọng

### Tại sao không dùng `@Autowired`?
Project đang dùng **Constructor Injection** (xem `ExtraServiceController`). Đây là best practice vì:
- Immutable dependencies (final field).
- Dễ unit test (mock inject qua constructor).
- Spring khuyến khích từ version 4.3+.

### Tại sao `@Transactional` chỉ trên write operations?
- `create`, `update`, `delete` → `@Transactional` để rollback nếu có exception giữa chừng.
- `get`, `getAll` → không cần, read-only không cần transaction overhead.

### Tại sao không Pagination cho Auditorium/SeatType?
- Số lượng phòng chiếu và loại ghế thường nhỏ (< 50 records).
- Pagination thêm complexity không cần thiết ở Phase 1.
- Có thể thêm sau nếu cần, không breaking change.
