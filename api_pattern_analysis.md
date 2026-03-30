# Phân Tích API & Design Pattern — Cinema Booking Backend

## 1. Tổng Quan Data Model Bạn Sở Hữu

Dựa trên review code, đây là các entity bạn **chịu trách nhiệm vận hành**:

```
Auditorium (phòng chiếu)
  └── Seat (ghế, thuộc SeatType)
       └── Ticket (vé = Seat × Showtime)

Showtime ← (do bạn bè tạo, bạn chỉ READ)
  └── Ticket

Booking (đơn đặt vé của User)
  ├── List<Ticket>       ← ghế đã chọn
  ├── List<BookingExtra> ← combo/đồ ăn
  └── Payment            ← (do bạn bè xử lý)
```

**Enums hiện tại:**
- `BookingStatus`: `PENDING → SUCCESS | CANCELLED | REFUNDED`
- `TicketStatus`: `PROCESSING → BOOKED | CANCELLED | USED`

---

## 2. API Chính Bạn Cần Xây Dựng

### 2.1 Auditorium & Seat Management

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 1 | `GET` | `/api/auditoriums` | Danh sách phòng chiếu | ADMIN |
| 2 | `POST` | `/api/auditoriums` | Tạo phòng chiếu | ADMIN |
| 3 | `PUT` | `/api/auditoriums/{id}` | Cập nhật phòng chiếu | ADMIN |
| 4 | `DELETE` | `/api/auditoriums/{id}` | Xoá phòng chiếu | ADMIN |
| 5 | `GET` | `/api/seat-types` | Danh sách loại ghế (STANDARD, VIP, COUPLE…) | ADMIN |
| 6 | `POST` | `/api/seat-types` | Tạo loại ghế | ADMIN |
| 7 | `GET` | `/api/auditoriums/{id}/seats` | Lấy sơ đồ ghế của phòng | PUBLIC |

### 2.2 Seat Map — Lõi Quan Trọng Nhất

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 8 | `GET` | `/api/showtimes/{id}/seat-map` | **Load toàn bộ ghế + trạng thái** (AVAILABLE / LOCKED / BOOKED) | USER |
| 9 | `POST` | `/api/showtimes/{id}/seats/lock` | **Lock ghế tạm** (giữ chỗ trong X giây) | USER |
| 10 | `DELETE` | `/api/showtimes/{id}/seats/unlock` | Unlock ghế nếu user thoát | USER |

> ⚠️ API #8 và #9 là **trái tim** của toàn bộ luồng. Hai endpoint này phải xử lý **concurrency** cực kỳ cẩn thận.

### 2.3 Booking Flow — Luồng Chính

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 11 | `POST` | `/api/bookings/draft` | **Tạo booking tạm** từ danh sách ghế đã lock | USER |
| 12 | `GET` | `/api/bookings/{id}` | Lấy chi tiết booking (cho trang xác nhận) | USER |
| 13 | `POST` | `/api/bookings/{id}/confirm` | **Xác nhận booking** (sau khi thanh toán thành công) | USER |
| 14 | `POST` | `/api/bookings/{id}/cancel` | Huỷ booking (tự huỷ hoặc timeout) | USER |
| 15 | `GET` | `/api/bookings/my` | Lịch sử booking của user hiện tại | USER |

### 2.4 Booking Facade — API Tổng Hợp

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 16 | `POST` | `/api/booking-facade/initiate` | **Một lệnh duy nhất**: validate ghế → lock → tạo draft → trả về thông tin thanh toán | USER |
| 17 | `POST` | `/api/booking-facade/complete` | **Một lệnh duy nhất**: xác nhận payment → cập nhật booking → sinh vé → gửi email | USER |

### 2.5 Ticket Management

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 18 | `GET` | `/api/tickets/{id}` | Lấy chi tiết vé (bao gồm QR code) | USER |
| 19 | `GET` | `/api/bookings/{id}/tickets` | Tất cả vé trong một booking | USER |
| 20 | `POST` | `/api/tickets/{id}/check-in` | Check-in vé tại quầy (đổi status BOOKED→USED) | ADMIN |

### 2.6 Admin Reporting (Bonus — ghép nối với bạn bè)

| # | Method | Endpoint | Mô tả | Role |
|---|--------|----------|-------|------|
| 21 | `GET` | `/api/admin/bookings` | Danh sách tất cả booking | ADMIN |
| 22 | `GET` | `/api/admin/bookings?status=PENDING` | Filter theo status | ADMIN |

---

## 3. Design Pattern Mapping — Phân Tích Từng Pattern

### 3.1 Singleton ✅ — `SeatCacheService`

**Áp dụng vào:** Quản lý trạng thái ghế bị lock (in-memory).

**Vấn đề thực tế:** Khi 2 user cùng mở sơ đồ ghế và chọn ghế A5, cần có **một nguồn sự thật duy nhất** cho trạng thái ghế bị lock tạm. DB là quá chậm và không phù hợp cho lock tạm thời (TTL ngắn).

**Cách triển khai chuẩn senior:**
```java
@Component // Spring IoC = Singleton scope mặc định
public class SeatLockRegistry {
    // ConcurrentHashMap: thread-safe, không cần synchronized
    private final Map<String, SeatLockEntry> lockMap = new ConcurrentHashMap<>();
    
    public boolean tryLock(String seatId, String userId, Duration ttl) { ... }
    public void unlock(String seatId, String userId) { ... }
    public boolean isLocked(String seatId) { ... }
    public void evictExpired() { ... } // gọi bởi @Scheduled
}
```

> **Tại sao đây là Singleton pattern thực sự:** Bean này phải là **một instance duy nhất** trong suốt vòng đời app để đảm bảo tính nhất quán của `lockMap`. Nếu có 2 instance, mỗi instance có map riêng → race condition.

---

### 3.2 State ✅ — `BookingStateMachine`

**Áp dụng vào:** Quản lý chuyển đổi trạng thái booking (`BookingStatus`).

**Vấn đề thực tế:** Logic kiểu `if (booking.getStatus() == PENDING) { ... } else if (...)`  rải rác khắp `BookingService` → không kiểm soát được transition hợp lệ, dễ để booking nhảy từ `CANCELLED` sang `SUCCESS` do bug.

**Cách triển khai chuẩn senior:**
```
BookingStatus (State interface)
  ├── PendingState    → cho phép: confirm(), cancel()
  ├── SuccessState    → cho phép: refund()
  ├── CancelledState  → từ chối mọi thứ (terminal state)
  └── RefundedState   → từ chối mọi thứ (terminal state)
```
```java
public interface BookingState {
    void confirm(BookingContext ctx);
    void cancel(BookingContext ctx);
    void refund(BookingContext ctx);
}

public class PendingState implements BookingState {
    @Override
    public void confirm(BookingContext ctx) {
        ctx.setStatus(BookingStatus.SUCCESS);
    }
    @Override
    public void cancel(BookingContext ctx) {
        ctx.setStatus(BookingStatus.CANCELLED);
        // unlock ghế, hoàn trả slot
    }
    @Override
    public void refund(BookingContext ctx) {
        throw new AppException(ErrorCode.INVALID_BOOKING_TRANSITION);
    }
}
```

> **State quan trọng hơn enum check:** Khi thêm trạng thái mới (vd: `AWAITING_REFUND`), chỉ cần thêm 1 class, không sửa if-else ở nhiều chỗ. Đây là Open/Closed Principle.

---

### 3.3 Proxy ✅ — `SeatValidationProxy`

**Áp dụng vào:** Kiểm tra ghế trước khi thực sự tạo booking.

**Vấn đề thực tế:** `BookingService.createDraft()` không nên trực tiếp gọi DB để kiểm tra từng ghế — vi phạm Single Responsibility, và nếu check logic thay đổi (thêm check VIP, check blacklist user) thì phải sửa service gốc.

**Cách triển khai chuẩn senior:**
```java
public interface ISeatService {
    SeatMapResponse getSeatMap(String showtimeId);
}

// Real implementation — chỉ lo query DB
@Service
public class SeatServiceImpl implements ISeatService { ... }

// Proxy — bao quanh real service, thêm validation layer
@Primary @Service
public class SeatValidationProxy implements ISeatService {
    private final SeatServiceImpl real;
    private final SeatLockRegistry lockRegistry;
    
    @Override
    public SeatMapResponse getSeatMap(String showtimeId) {
        // Enrichment: đánh dấu ghế nào đang bị lock
        SeatMapResponse response = real.getSeatMap(showtimeId);
        enrichWithLockStatus(response);
        return response;
    }
    
    public void validateSeatsForBooking(List<String> seatIds, String showtimeId) {
        // Check 1: ghế có tồn tại?
        // Check 2: ghế có đang bị lock bởi người khác?
        // Check 3: ghế đã BOOKED trong showtime chưa? (DB check)
        // → Nếu fail → throw AppException(SEAT_ALREADY_BOOKED)
    }
}
```

> **Proxy giải quyết double-booking:** Check DB dùng `SELECT ... FOR UPDATE` (pessimistic lock) hoặc kết hợp DB `UniqueConstraint(showtime_id, seat_id)` trên bảng `tickets` (đã có sẵn).

---

### 3.4 Template Method ✅ — `BookingFlowTemplate`

**Áp dụng vào:** Định nghĩa các bước của luồng booking, cho phép thay đổi từng bước mà không phá vỡ flow.

**Vấn đề thực tế:** Luồng booking luôn gồm: **validate → reserve → calculate → persist → notify**. Thứ tự không đổi nhưng nội dung từng bước có thể khác nhau (VIP booking, Group booking, v.v.).

**Cách triển khai chuẩn senior:**
```java
public abstract class BookingFlowTemplate {
    
    // Template method — KHÔNG override
    public final BookingResult execute(BookingRequest request) {
        validateSeats(request);          // Step 1
        reserveSeats(request);           // Step 2
        BigDecimal total = calcTotal(request); // Step 3
        Booking booking = persistBooking(request, total); // Step 4
        sendConfirmation(booking);       // Step 5 (optional hook)
        return buildResult(booking);
    }
    
    protected abstract void validateSeats(BookingRequest req);
    protected abstract void reserveSeats(BookingRequest req);
    protected abstract BigDecimal calcTotal(BookingRequest req);
    protected abstract Booking persistBooking(BookingRequest req, BigDecimal total);
    
    // Hook — subclass có thể override hoặc không
    protected void sendConfirmation(Booking booking) {
        // default: không gửi
    }
}

// Concrete: Booking thường
@Service
public class StandardBookingFlow extends BookingFlowTemplate {
    @Override
    protected BigDecimal calcTotal(BookingRequest req) {
        // giá ghế × số ghế + surcharge loại ghế
    }
}

// Concrete: Booking có promotion
@Service
public class PromotionBookingFlow extends BookingFlowTemplate {
    @Override
    protected BigDecimal calcTotal(BookingRequest req) {
        // giá gốc - discount
    }
    @Override
    protected void sendConfirmation(Booking booking) {
        // gửi email có kèm thông tin promotion
    }
}
```

---

### 3.5 Facade ✅ — `BookingFacade`

**Áp dụng vào:** `BookingFacadeController` — cổng API duy nhất cho Frontend, che giấu toàn bộ sự phức tạp của các subsystem bên trong.

**Vấn đề thực tế:** Frontend phải gọi 4–5 API liên tiếp (lock ghế → tạo draft → apply promotion → tính tổng → redirect payment) và tự quản lý state. Nếu 1 bước fail, frontend không biết rollback gì.

> **⚠️ Quan trọng — Facade là tầng API, không phải tầng thực thi:**
> Facade **không tự sinh ra vấn đề hiệu suất**. Vấn đề tải cao xuất phát từ những gì Facade gọi bên trong, không phải từ Facade. Facade hoàn toàn có thể bên trong gọi CommandBus để xử lý async — **Facade và Command không phải lựa chọn hoặc/hoặc mà là hai tầng khác nhau**:
> - **Facade** = Tầng API (trả lời câu hỏi: *"Frontend gọi endpoint nào?"*)
> - **Command** = Tầng thực thi bên trong (trả lời câu hỏi: *"Hệ thống thực thi action ra sao?"*)

**Cách triển khai chuẩn senior:**
```java
@Service
public class BookingFacade {
    private final SeatValidationProxy seatValidator;
    private final SeatLockRegistry lockRegistry;
    private final BookingFlowTemplate bookingFlow;
    private final BookingCommandBus commandBus;        // Facade gọi Command bên trong
    private final PaymentService paymentService;       // của bạn bè

    // initiate(): cần kết quả ngay (URL thanh toán) → chạy đồng bộ, KHÔNG dùng Command
    @Transactional
    public BookingInitiateResponse initiate(BookingInitiateRequest request) {
        seatValidator.validateSeatsForBooking(request.getSeatIds(), request.getShowtimeId());
        lockRegistry.lockAll(request.getSeatIds(), request.getUserId());
        Booking draft = bookingFlow.execute(request);  // Template Method
        String paymentUrl = paymentService.createPaymentUrl(draft);
        return new BookingInitiateResponse(draft.getId(), paymentUrl);
    }

    // complete(): VNPay webhook, không ai đợi kết quả → Facade gọi Command để xử lý
    public void complete(String bookingId, PaymentCallbackData data) {
        commandBus.dispatch(new ConfirmBookingCommand(bookingId, data));
        // Command xử lý: State transition + audit log + email
    }

    // cancel(): cần audit + có thể undo → Facade gọi Command
    public void cancel(String bookingId, String reason) {
        commandBus.dispatch(new CancelBookingCommand(bookingId, reason));
    }
}
```

---

### 3.6 Builder ✅ — `TicketBuilder` / `BookingBuilder`

**Áp dụng vào:** Tạo entity [Booking](file:///Z:/OOAD/cinema_booking_backend_team/CINEMA_BOOKING_BACKEND/src/main/java/com/movie/cinema_booking_backend/entity/Booking.java#14-81) và [Ticket](file:///Z:/OOAD/cinema_booking_backend_team/CINEMA_BOOKING_BACKEND/src/main/java/com/movie/cinema_booking_backend/entity/Ticket.java#13-65) phức tạp với nhiều optional field.

**Vấn đề thực tế:** [Booking](file:///Z:/OOAD/cinema_booking_backend_team/CINEMA_BOOKING_BACKEND/src/main/java/com/movie/cinema_booking_backend/entity/Booking.java#14-81) có nhiều trường tùy chọn (note, promotion, extras). Gọi constructor 8+ tham số hoặc nhiều setter rải rác → dễ thiếu trường, khó đọc.

**Cách triển khai chuẩn senior** (Lombok `@Builder` đã có sẵn, dùng Fluent API):
```java
// Bên trong BookingFlowTemplate.persistBooking()
Booking booking = Booking.builder()
    .id(UUID.randomUUID().toString())
    .user(currentUser)
    .status(BookingStatus.PENDING)
    .totalAmount(calculatedTotal)
    .createdAt(LocalDateTime.now())
    .note(request.getNote())     // optional
    .build();

// Tạo từng Ticket cho mỗi ghế
for (String seatId : request.getSeatIds()) {
    Ticket ticket = Ticket.builder()
        .id(UUID.randomUUID().toString())
        .showtime(showtime)
        .seat(seat)
        .price(seatPrice)
        .status(TicketStatus.PROCESSING)
        .build();
    booking.addTicket(ticket); // dùng helper method đã có sẵn
}
```

> **Builder ở đây demonstrable nhất là:** tạo một `BookingDraftBuilder` riêng biệt (không phải Lombok) có thể accumulate nhiều `addSeat()`, `addExtra()`, `applyPromotion()` trước khi `build()` ra Booking hoàn chỉnh. Đây là pattern cổ điển nhất, dễ present.

---

### 3.7 Command ✅ — `BookingCommandBus`

**Áp dụng vào:** Tầng thực thi nghiệp vụ bên trong `BookingFacade` — biến các hành động (`confirm`, `cancel`, `refund`) thành object độc lập có thể audit/undo.

**Điểm khác biệt sống còn với Facade:**
- **Facade** = Cổng vào (API Layer). Luôn đứng ở ngoài cùng, tiếp nhận request từ Frontend.
- **Command** = Cơ chế thực thi (Execution Layer). Nằm bên trong Facade, đảm nhiệm phần side-effect phức tạp.
- **Facade là Động từ** (method call, chạy xong biến mất). **Command là Danh từ** (object tồn tại, có thể lưu trữ, queue, undo).

**Vấn đề thực tế:** Khi Admin cần audit "ai đã cancel booking này? lúc nào? tại sao?", hoặc cần undo — một lệnh gọi `bookingService.cancel()` thẳng không để lại dấu vết gì.

**Cách triển khai chuẩn senior:**
```java
public interface BookingCommand {
    void execute();
    void undo();       // rollback nếu cần
    String describe(); // cho audit log
}

public class CancelBookingCommand implements BookingCommand {
    private final Booking booking;
    private final String reason;
    private BookingStatus previousStatus; // lưu để undo

    @Override
    public void execute() {
        this.previousStatus = booking.getStatus();  // snapshot state cũ
        bookingState.cancel(context);               // gọi State để validate + transition
        lockRegistry.unlockAll(booking);            // side-effect: trả ghế
    }

    @Override
    public void undo() {
        booking.setStatus(previousStatus);          // khôi phục state
    }

    @Override
    public String describe() {
        return "CANCEL booking " + booking.getId() + " reason: " + reason;
    }
}

// Invoker — BookingFacade inject và gọi cái này
@Service
public class BookingCommandBus {
    private final Deque<BookingCommand> history = new ArrayDeque<>();

    public void dispatch(BookingCommand cmd) {
        cmd.execute();
        history.push(cmd);
        auditLogService.log(cmd.describe()); // audit log tự động
    }

    public void undoLast() {
        if (!history.isEmpty()) history.pop().undo();
    }
}
```

> **Command phù hợp nhất với:** `complete()` (VNPay webhook, không ai đợi kết quả), `cancel()` (cần undo-able), `refund()` (cần audit trail). **KHÔNG phù hợp với** `initiate()` vì Frontend đang đứng đợi URL thanh toán ngay lập tức.

---

### 3.8 State vs Command — Dùng Cùng Nhau

| Concern | Pattern | Chịu trách nhiệm |
|---------|---------|-----------------|
| **Trạng thái hợp lệ** | State | "Booking `CANCELLED` có cho `refund` không?" |
| **Hành động cụ thể** | Command | "Thực hiện refund: update DB, gọi gateway, ghi log" |

→ `BookingCommandBus.dispatch(new RefundCommand(booking))` → `RefundCommand.execute()` gọi `bookingState.refund(ctx)` → State kiểm tra tính hợp lệ, Command xử lý side-effect.

---

## 4. Ma Trận Pattern ↔ Feature

> Facade luôn là **tầng API ngoài cùng** cho mọi endpoint. Command là **cơ chế bên trong** cho các hành động cần audit/undo. Hai pattern không loại trừ nhau.

| Feature | Singleton | State | Proxy | Template Method | Facade (API Layer) | Builder | Command (Exec Layer) |
|---------|:---------:|:-----:|:-----:|:---------------:|:------------------:|:-------:|:--------------------:|
| Seat Map Load | ✅ (Registry) | | ✅ (enrich) | | ✅ (cổng vào) | | |
| Seat Lock/Unlock | ✅ | | | | ✅ (cổng vào) | | |
| Seat Validation | | | ✅ (primary) | | | | |
| Booking Draft (initiate) | | ✅ (PENDING) | | ✅ (steps) | ✅ (primary) | ✅ (build) | ❌ (cần kết quả ngay) |
| Booking Complete (webhook) | | ✅ (→SUCCESS) | | | ✅ (cổng vào) | | ✅ (primary) |
| Booking Cancel | | ✅ (→CANCELLED) | | | ✅ (cổng vào) | | ✅ (undo-able) |
| Booking Refund | | ✅ (→REFUNDED) | | | ✅ (cổng vào) | | ✅ (audit trail) |
| Ticket Creation | | | | | | ✅ (primary) | |
| Admin Audit | | | | | | | ✅ (primary) |

---

## 5. Luồng Tích Hợp Với Bạn Bè

```
━━━ ENDPOINT 1: initiate() — Frontend đang đợi URL thanh toán ━━━

[Frontend]
    │ POST /api/booking-facade/initiate
    ▼
[BookingFacade]  ← API Layer (Facade)
    ├── [SeatValidationProxy]   (Proxy: check double-booking)
    ├── [SeatLockRegistry]      (Singleton: lock ghế tạm)
    ├── [BookingFlowTemplate]   (Template Method: validate→reserve→calc→persist)
    │       └── Ticket/Booking.builder().build()  (Builder)
    │           BookingStatus = PENDING            (State khởi tạo)
    └── [PaymentService]        (BẠN BÈ: tạo URL thanh toán)
    │
    ▼ return { bookingId, paymentUrl }  ← trả ngay cho Frontend

━━━ ENDPOINT 2: complete() — VNPay Webhook, không ai đang đợi ━━━

[VNPay/Momo]
    │ POST /api/booking-facade/complete
    ▼
[BookingFacade]  ← API Layer (Facade)
    └── [BookingCommandBus].dispatch(ConfirmBookingCommand)  ← Execution Layer (Command)
            ├── [BookingState].confirm()   (State: PENDING → SUCCESS)
            ├── TicketStatus → BOOKED
            ├── Audit log tự động
            └── EmailService (optional)

━━━ ENDPOINT 3: cancel() — Có thể cần undo ━━━

[Frontend/Admin]
    │ POST /api/bookings/{id}/cancel
    ▼
[BookingFacade]  ← API Layer (Facade)
    └── [BookingCommandBus].dispatch(CancelBookingCommand)  ← Execution Layer (Command)
            ├── snapshot previousState (để undo)
            ├── [BookingState].cancel()   (State: PENDING → CANCELLED)
            ├── [SeatLockRegistry].unlock()
            └── Audit log + undo-able
```

---

## 6. Ưu Tiên Thực Hiện

| Thứ tự | Feature | Pattern cốt lõi | Ghi chú |
|--------|---------|-----------------|---------|
| 1 | SeatLockRegistry | **Singleton** | Nền tảng cho mọi thứ |
| 2 | SeatValidationProxy | **Proxy** | Chặn double booking |
| 3 | BookingStateMachine | **State** | Quản lý transition |
| 4 | BookingFlowTemplate | **Template Method** | Luồng tạo booking |
| 5 | Ticket + Booking Builder | **Builder** | Tạo entity chuẩn |
| 6 | BookingFacade | **Facade** | Expose API tổng hợp |
| 7 | BookingCommandBus | **Command** | Audit + undo |
