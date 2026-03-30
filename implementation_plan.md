# Kế Hoạch Triển Khai — Cinema Booking Backend

## Cấu Trúc Package

```
com.movie.cinema_booking_backend/
│
├── controller/
│   ├── AuditoriumController.java
│   ├── SeatTypeController.java
│   ├── SeatMapController.java          ← seat-map + lock/unlock
│   ├── BookingController.java
│   ├── BookingFacadeController.java    ← Facade entry point
│   ├── PaymentController.java          ← vnpay-ipn + return
│   └── TicketController.java
│
├── entity/  (đã có sẵn, không sửa)
│
├── enums/
│   ├── BookingStatus.java              (đã có)
│   ├── TicketStatus.java               (đã có)
│   └── AuditoriumStatus.java           [NEW] ACTIVE | UNDER_MAINTENANCE | INACTIVE
│
├── pattern/                            [NEW] — tách biệt code demo pattern
│   ├── singleton/
│   │   └── SeatLockRegistry.java       ← @Component, ConcurrentHashMap
│   ├── proxy/
│   │   ├── ISeatService.java           ← interface chung
│   │   └── SeatValidationProxy.java    ← @Primary @Service
│   ├── template/
│   │   ├── BookingFlowTemplate.java    ← abstract class
│   │   ├── StandardBookingFlow.java    ← @Service
│   │   └── GroupBookingFlow.java       ← @Service
│   ├── state/
│   │   ├── TicketState.java            ← interface
│   │   ├── BookedState.java
│   │   ├── UsedState.java
│   │   ├── CancelledState.java
│   │   ├── ProcessingState.java
│   │   └── AuditoriumState.java        ← interface (optional)
│   └── builder/
│       ├── BookingDraftBuilder.java    ← tự code (không Lombok)
│       └── BookingExtraItem.java       ← inner DTO dùng trong builder
│
├── service/
│   ├── IAuditoriumService.java
│   ├── ISeatTypeService.java
│   ├── ISeatMapService.java
│   ├── IBookingService.java
│   ├── ITicketService.java
│   └── impl/
│       ├── AuditoriumServiceImpl.java
│       ├── SeatTypeServiceImpl.java
│       ├── SeatMapServiceImpl.java     ← implements ISeatService (real)
│       ├── BookingServiceImpl.java
│       ├── TicketServiceImpl.java
│       └── BookingFacade.java          ← @Service, orchestrates patterns
│
├── repository/
│   ├── AuditoriumRepository.java
│   ├── SeatRepository.java
│   ├── SeatTypeRepository.java
│   ├── BookingRepository.java
│   ├── TicketRepository.java
│   └── BookingExtraRepository.java
│
├── request/
│   ├── AuditoriumRequest.java
│   ├── SeatTypeRequest.java
│   ├── SeatLockRequest.java            ← { showtimeId, seatIds[] }
│   ├── BookingInitiateRequest.java     ← { showtimeId, seatIds[], type, extras[] }
│   └── GroupBookingRequest.java        ← extends BookingInitiateRequest
│
├── response/
│   ├── AuditoriumResponse.java
│   ├── SeatMapResponse.java            ← { seats: [{ id, name, status, seatType }] }
│   ├── BookingResponse.java
│   ├── BookingInitiateResponse.java    ← { bookingId, paymentUrl, expiresAt }
│   └── TicketResponse.java             ← { id, seatName, qrCodeUrl, status }
│
└── exception/
    └── ErrorCode.java                  ← thêm: SEAT_ALREADY_TAKEN, TICKET_ALREADY_USED,
                                                  TICKET_CANCELLED, TICKET_NOT_PAID,
                                                  AUDITORIUM_NOT_ACTIVE, BOOKING_NOT_FOUND
```

---

## Phase Triển Khai

### Phase 1 — Foundation (Không có Pattern)
> **Mục tiêu:** Dựng hạ tầng CRUD cơ bản. Chưa implement pattern, chỉ tạo đủ entity/service/controller để data flow hoạt động.

**Thứ tự:**

**1.1 AuditoriumStatus enum + Auditorium CRUD**
- Thêm `AuditoriumStatus` vào entity [Auditorium](file:///Z:/OOAD/cinema_booking_backend_team/CINEMA_BOOKING_BACKEND/src/main/java/com/movie/cinema_booking_backend/entity/Auditorium.java#10-36)
- `AuditoriumRepository`, `IAuditoriumService`, `AuditoriumServiceImpl`
- `AuditoriumController`: `GET /api/auditoriums`, `POST`, `PUT /{id}`, `DELETE /{id}`

**1.2 SeatType CRUD**
- `SeatTypeRepository`, `ISeatTypeService`, `SeatTypeServiceImpl`
- `SeatTypeController`: `GET /api/seat-types`, `POST`

**1.3 Basic Seat Map Query**
- `SeatRepository` với query: `findByAuditoriumId` và `findByShowtimeId`
- `ISeatMapService`, `SeatMapServiceImpl` (implementation thô, chưa Proxy)
- `SeatMapController`: `GET /api/auditoriums/{id}/seats`

**1.4 Error Codes**
- Thêm các `ErrorCode` mới vào [ErrorCode.java](file:///Z:/OOAD/cinema_booking_backend_team/CINEMA_BOOKING_BACKEND/src/main/java/com/movie/cinema_booking_backend/exception/ErrorCode.java)

**Kết thúc Phase 1:** CRUD phòng chiếu, loại ghế hoạt động. Có thể test qua Postman.

---

### Phase 2 — Singleton + Proxy
> **Mục tiêu:** Implement hai pattern đầu tiên, đây là nền tảng cho toàn bộ luồng đặt vé.

**2.1 Singleton — `SeatLockRegistry`**
```
pattern/singleton/SeatLockRegistry.java
  - ConcurrentHashMap<String, SeatLockEntry>
  - SeatLockEntry { userId, expiresAt }
  - tryLock(), unlock(), isLocked(), lockAll(), unlockAll()
  - @Scheduled evictExpired() mỗi 30 giây
```

**2.2 Proxy — `SeatValidationProxy`**
```
pattern/proxy/ISeatService.java       ← interface
pattern/proxy/SeatValidationProxy.java ← @Primary @Service
  - getSeatMap(): gọi real + enrichWithLockStatus()
  - validateForBooking(): 3 checks (exist / locked / booked)
```

**2.3 Seat Map API (upgrade)**
- `GET /api/showtimes/{id}/seat-map` — gọi qua Proxy, trả về status AVAILABLE/LOCKED/BOOKED
- `POST /api/showtimes/{id}/seats/lock` — gọi `SeatLockRegistry.lockAll()`
- `DELETE /api/showtimes/{id}/seats/unlock` — gọi `SeatLockRegistry.unlockAll()`

**Kết thúc Phase 2:** Seat map live với lock status. Có thể mở 2 tab browser test double-lock.

---

### Phase 3 — Builder + Template Method
> **Mục tiêu:** Implement luồng tạo booking hoàn chỉnh.

**3.1 Builder — `BookingDraftBuilder`**
```
pattern/builder/BookingExtraItem.java
pattern/builder/BookingDraftBuilder.java
  - forUser(), forShowtime(), addSeat(), addExtra(), applyPromotion()
  - build() → tự tính totalAmount, tạo Ticket per seat
```

**3.2 Template Method — Booking Flow**
```
pattern/template/BookingFlowTemplate.java    ← abstract
pattern/template/StandardBookingFlow.java    ← @Service @Qualifier("standard")
pattern/template/GroupBookingFlow.java       ← @Service @Qualifier("group")
```
- `StandardBookingFlow.calcTotal()`: giá ghế + surcharge
- `GroupBookingFlow.calcTotal()`: discount theo số lượng
- `GroupBookingFlow.onPostPersist()`: gọi approval + invoice

**3.3 BookingService + Repository**
```
BookingRepository.java
TicketRepository.java
BookingExtraRepository.java
IBookingService.java
BookingServiceImpl.java
  - createDraft(request): dùng SeatValidationProxy + BookingDraftBuilder
  - getById(), cancel(), getMyBookings()
```

**3.4 Booking Endpoints**
- `POST /api/bookings/draft`
- `GET /api/bookings/{id}`
- `POST /api/bookings/{id}/cancel`
- `GET /api/bookings/my`

**Kết thúc Phase 3:** Có thể tạo booking draft hoàn chỉnh. Builder tính tổng đúng. Hai flow Standard vs Group hoạt động.

---

### Phase 4 — Facade + State
> **Mục tiêu:** Đóng gói toàn bộ thành một cổng API duy nhất và implement State cho Ticket.

**4.1 State — TicketStatus States**
```
pattern/state/TicketState.java         ← interface { checkIn(), cancel() }
pattern/state/BookedState.java         ← BOOKED → USED hoặc CANCELLED
pattern/state/UsedState.java           ← throw TICKET_ALREADY_USED
pattern/state/CancelledState.java      ← throw TICKET_CANCELLED
pattern/state/ProcessingState.java     ← throw TICKET_NOT_PAID
```

**4.1b State — AuditoriumStatus (optional)**
```
pattern/state/AuditoriumState.java     ← interface { createShowtime() }
  ActiveState, MaintenanceState, InactiveState
```

**4.2 Facade — `BookingFacade`**
```
service/impl/BookingFacade.java
  @Transactional initiate(BookingInitiateRequest):
    1. seatProxy.validateForBooking()
    2. lockRegistry.lockAll()
    3. bookingFlow.execute()  ← inject đúng flow theo request.type
    4. paymentService.createPaymentUrl()  ← bạn bè
    5. return BookingInitiateResponse
```

**4.3 Facade Controller**
```
controller/BookingFacadeController.java
  POST /api/booking-facade/initiate
```

**4.4 VNPay Callback (tách biệt)**
```
controller/PaymentController.java
  POST /api/payment/vnpay-ipn  ← verify + update Booking + dùng State cập nhật Ticket
  GET  /api/payment/return     ← đọc DB, trả response
```

**4.5 Ticket Check-in (State demo piece)**
```
controller/TicketController.java
  GET  /api/tickets/{id}
  GET  /api/bookings/{id}/tickets
  POST /api/tickets/{id}/check-in  ← TicketState.checkIn(ticket)
```

**Kết thúc Phase 4:** Toàn bộ luồng từ chọn ghế → đặt vé → check-in hoạt động end-to-end.

---

### Phase 5 — Polish & Integration Test
> **Mục tiêu:** Đảm bảo ghép nối với phần của bạn bè, test các edge case.

**5.1 Integration Points với bạn bè**
- Xác nhận interface `PaymentService.createPaymentUrl(Booking)` với team payment
- Xác nhận `PromotionService.validateCode(code, booking)` với team promotion
- Xác nhận JWT token flow cho các secured endpoint

**5.2 Edge Cases cần test**
- Double-lock: 2 user lock cùng ghế → user 2 nhận lỗi `SEAT_ALREADY_TAKEN`
- Lock TTL: ghế lock hết hạn tự động unlock sau 5 phút
- Check-in vé đã dùng: nhận đúng lỗi `TICKET_ALREADY_USED`
- Cancel booking PENDING: unlock ghế, State transition đúng
- IPN signature sai: từ chối, không update DB

**5.3 Security**
- Tất cả Booking/Ticket endpoint verify JWT user chỉ xem booking của mình
- Check-in chỉ cho `ROLE_ADMIN` hoặc `ROLE_STAFF`
- IPN endpoint whitelist IP VNPay (nếu có)

---

## Thứ Tự Dependency

```
Phase 1 (Foundation)
    ↓
Phase 2 (Singleton + Proxy) — cần Phase 1 có SeatRepository
    ↓
Phase 3 (Builder + Template) — cần Phase 2 có SeatValidationProxy
    ↓
Phase 4 (Facade + State) — cần Phase 3 có BookingFlow hoàn chỉnh
    ↓
Phase 5 (Polish) — cần toàn bộ Phase 1-4
```

---

## Ước Tính Thời Gian

| Phase | Nội dung | Ước tính |
|-------|---------|---------|
| Phase 1 | Foundation CRUD | 2-3 giờ |
| Phase 2 | Singleton + Proxy | 2-3 giờ |
| Phase 3 | Builder + Template | 3-4 giờ |
| Phase 4 | Facade + State | 3-4 giờ |
| Phase 5 | Polish + Integration | 2-3 giờ |
| **Tổng** | | **~12-17 giờ** |
