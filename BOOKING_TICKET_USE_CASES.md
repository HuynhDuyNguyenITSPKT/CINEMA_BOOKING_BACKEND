# Booking - Ticket - Auditorium - Seat Use Cases (As-Is)

Tai lieu nay tong hop cac use case nghiep vu dang ton tai trong code backend, map truc tiep tu endpoint hien co.

## 1) Pham vi endpoint da map

| # | Method | Endpoint | Controller |
|---|---|---|---|
| 1 | GET | `/api/public/cinema/movies/{movieId}/showtimes?date=yyyy-MM-dd` | `PublicCinemaController` |
| 2 | GET | `/api/auditoriums` | `AuditoriumController` |
| 3 | GET | `/api/auditoriums/{id}` | `AuditoriumController` |
| 4 | GET | `/api/auditoriums/{id}/seats` | `SeatController` |
| 5 | GET | `/api/showtimes/{id}/seat-map` | `SeatMapController` |
| 6 | POST | `/api/showtimes/{id}/seats/lock` | `SeatMapController` |
| 7 | DELETE | `/api/showtimes/{id}/seats/unlock` | `SeatMapController` |
| 8 | POST | `/api/bookings` | `BookingController` |
| 9 | GET | `/api/bookings/{id}` | `BookingController` |
| 10 | GET | `/api/bookings/my` | `BookingController` |
| 11 | POST | `/api/bookings/{id}/cancel` | `BookingController` |
| 12 | POST | `/api/tickets/{id}/check-in` | `TicketController` |
| 13 | POST | `/api/admin/auditoriums` | `AuditoriumController` |
| 14 | PUT | `/api/admin/auditoriums/{id}` | `AuditoriumController` |
| 15 | DELETE | `/api/admin/auditoriums/{id}` | `AuditoriumController` |
| 16 | PUT | `/api/admin/auditoriums/{id}/regenerate-seats` | `AuditoriumController` |

> Luu y quyen truy cap:
> - `POST /api/tickets/{id}/check-in` co `@PreAuthorize("hasAnyRole('ADMIN')")` tai method.
> - Cac endpoint khac trong tai lieu phu thuoc cau hinh quyen trong `SecurityConfig` (khong the hien truc tiep o method level).

---

## 2) Use case chi tiet

### UC-BK-01: Xem lich chieu de bat dau dat ve
- **Muc tieu**: Nguoi dung chon suat chieu theo phim va ngay.
- **Actor**: Khach/nguoi dung.
- **Pre-condition**: Phim ton tai, co suat chieu theo ngay.
- **Main flow**:
  1. Client goi endpoint lay showtime theo `movieId` va `date`.
  2. He thong tra danh sach suat chieu.
  3. Nguoi dung chon 1 suat de tiep tuc chon ghe.
- **Endpoint**: `GET /api/public/cinema/movies/{movieId}/showtimes?date=yyyy-MM-dd`

### UC-BK-02: Xem danh sach phong chieu
- **Muc tieu**: Lay thong tin tong quan cac phong.
- **Actor**: User/Admin.
- **Endpoint**: `GET /api/auditoriums`

### UC-BK-03: Xem chi tiet 1 phong chieu
- **Muc tieu**: Lay thong tin 1 auditorium cu the.
- **Actor**: User/Admin.
- **Endpoint**: `GET /api/auditoriums/{id}`

### UC-BK-04: Xem so do ghe theo phong (co ban)
- **Muc tieu**: Lay danh sach ghe cua phong.
- **Actor**: User/Admin.
- **Main flow**:
  1. Goi endpoint theo `auditoriumId`.
  2. He thong tra danh sach ghe + thong tin seatType.
- **Endpoint**: `GET /api/auditoriums/{id}/seats`

### UC-BK-05: Xem seat-map theo suat chieu (trang thai live)
- **Muc tieu**: Lay trang thai ghe theo showtime: `AVAILABLE/LOCKED/BOOKED`.
- **Actor**: User (co hoac khong dang nhap tuy theo security).
- **Main flow**:
  1. Goi endpoint voi `showtimeId`.
  2. He thong enrich trang thai ghe tu lock RAM va du lieu ticket DB.
- **Endpoint**: `GET /api/showtimes/{id}/seat-map`

### UC-BK-06: Tam giu ghe truoc khi thanh toan
- **Muc tieu**: Lock mot nhom ghe trong thoi gian TTL de tranh tranh chap.
- **Actor**: User dang dang nhap.
- **Pre-condition**: Ghe chua bi lock/khong thuoc user khac.
- **Main flow**:
  1. User gui danh sach `seatIds`.
  2. He thong lock all-or-nothing.
  3. Neu 1 ghe fail thi rollback lock trong batch.
- **Endpoint**: `POST /api/showtimes/{id}/seats/lock`
- **Body mau**:
```json
{
  "seatIds": ["seat-1", "seat-2"]
}
```

### UC-BK-07: Nha ghe khi thoat man hinh chon ghe
- **Muc tieu**: Unlock cac ghe user dang giu.
- **Actor**: User dang dang nhap.
- **Endpoint**: `DELETE /api/showtimes/{id}/seats/unlock`

### UC-BK-08: Khoi tao booking va lay payment URL
- **Muc tieu**: Tao booking draft + ticket draft + tra URL thanh toan.
- **Actor**: User dang dang nhap.
- **Pre-condition**: Showtime, seat hop le; paymentMethod hop le.
- **Main flow**:
  1. Validate ghe (ton tai, khong lock boi nguoi khac, chua BOOKED).
  2. Lock ghe TTL.
  3. Tao booking draft (co tinh gia, extra, promotion neu co).
  4. Dang ky payment va tao payment URL.
- **Endpoint**: `POST /api/bookings`
- **Body mau toi thieu**:
```json
{
  "showtimeId": "showtime-uuid",
  "seatIds": ["seat-uuid-1", "seat-uuid-2"],
  "extras": {"1": 2},
  "promotionCode": "SUMMER2026",
  "paymentMethod": "MOMO",
  "note": "Nhom ban",
  "bookingType": "GROUP"
}
```

### UC-BK-09: Xem chi tiet booking
- **Muc tieu**: Xem booking theo id (bao gom ticket/extras).
- **Actor**: User dang dang nhap.
- **Endpoint**: `GET /api/bookings/{id}`

### UC-BK-10: Xem lich su booking cua toi
- **Muc tieu**: Lay danh sach booking cua user hien tai.
- **Actor**: User dang dang nhap.
- **Endpoint**: `GET /api/bookings/my`

### UC-BK-11: Huy booking
- **Muc tieu**: Huy booking o cac trang thai cho phep.
- **Actor**: User dang dang nhap.
- **Main flow**:
  1. Tim booking theo id.
  2. Kiem tra trang thai va cap nhat booking/ticket sang `CANCELLED`.
- **Endpoint**: `POST /api/bookings/{id}/cancel`

### UC-BK-12: Check-in ve tai rap
- **Muc tieu**: Nhan vien/Admin quet ve, chuyen trang thai su dung.
- **Actor**: Admin (theo annotation hien tai).
- **Main flow**:
  1. Tim ticket theo id.
  2. Goi State transition `ticket.checkIn()`.
  3. Luu trang thai moi.
- **Endpoint**: `POST /api/tickets/{id}/check-in`

### UC-ADM-01: Tao phong chieu (kem ghe)
- **Muc tieu**: Admin tao auditorium va cau hinh ghe ban dau.
- **Endpoint**: `POST /api/admin/auditoriums`

### UC-ADM-02: Cap nhat phong chieu
- **Muc tieu**: Admin cap nhat metadata phong.
- **Endpoint**: `PUT /api/admin/auditoriums/{id}`

### UC-ADM-03: Xoa phong chieu
- **Muc tieu**: Admin xoa phong neu dat dieu kien nghiep vu.
- **Endpoint**: `DELETE /api/admin/auditoriums/{id}`

### UC-ADM-04: Tai tao layout ghe
- **Muc tieu**: Admin tao lai toan bo seat layout khi phong thay doi vat ly.
- **Canh bao**: Co nguy co anh huong ticket/showtime dang hoat dong neu khong co guard nghiep vu.
- **Endpoint**: `PUT /api/admin/auditoriums/{id}/regenerate-seats`

---

## 3) De xuat de phu hop pattern tot hon voi nghiep vu

### A. Pattern co the bo sung ngay
1. **Strategy + Factory cho booking flow theo `bookingType`**
   - Hien tai chon flow bang `if ("GROUP")` trong `BookingServiceImpl`.
   - De xuat: map `bookingType -> BookingFlowTemplate` bang registry/factory de mo rong `CORPORATE`, `STUDENT`, `EVENT` ma khong sua service trung tam.

2. **Specification cho validate booking/promotion**
   - Tach rule thanh cac spec: `SeatBelongsToShowtimeSpec`, `SeatNotLockedSpec`, `PromotionActiveSpec`, `PromotionQuantitySpec`, `PromotionMinOrderSpec`.
   - Loi ich: rule ro rang, test doc lap, ket hop linh hoat.

3. **Chain of Responsibility cho pre-booking checks**
   - Chuoi check theo thu tu: showtime ton tai -> seat hop le -> lock status -> booked status -> promotion hop le.
   - De thay doi thu tu/chen rule ma khong lam `execute()` qua dai.

4. **Domain Event (hoac Outbox) cho hau xu ly**
   - Sau khi booking tao/cancel/payment success: phat event `BookingCreated`, `BookingCancelled`, `PaymentSucceeded`.
   - Side effects (email, analytics, thong bao) tach khoi transaction chinh.

5. **Saga/Process Manager cho booking-payment-cancel timeout**
   - Khi qua TTL ma chua thanh toan: tu dong cancel booking + unlock seat.
   - Phu hop khi nghiep vu co buoc bat dong bo va callback payment.

### B. Dieu chinh nghiep vu de pattern phat huy tac dung
1. **Chuan hoa vong doi booking/ticket thanh state machine day du**
   - Booking: `PENDING -> SUCCESS -> USED/CANCELLED/EXPIRED`.
   - Ticket: giu state hien co, nhung bat buoc moi transition di qua state handler thay vi set enum truc tiep.

2. **Them use case het han thanh toan (Expired Booking)**
   - Tao scheduler/job de xu ly booking qua han.
   - Ket hop State + Domain Event de dong bo lock va trang thai DB.

3. **Formal hoa policy hoan/huy ve**
   - Ap dung `Policy` (Strategy) cho tung loai ve: cho phep huy den truoc gio chieu X phut, phi huy Y%.

4. **Tach Pricing Engine thanh module rieng**
   - `BookingDraftBuilder` chi dung aggregate.
   - Tinh gia dua tren `PricingStrategy` + `DiscountPolicy` de tranh don qua nhieu logic vao Builder.

5. **Dat rule ownership vao use case cancel**
   - Use case huy booking phai co rule ownership ro rang (nguoi tao booking hoac admin).

---

## 4) Uu tien trien khai (goi y)
1. Lam **Factory/Strategy cho booking flow** + **Specification cho promotion** truoc (de mo rong nhanh, it risk).
2. Bo sung **Expired booking use case** (scheduler + unlock seat).
3. Chuan hoa **state transition** va **policy huy/hoan**.
4. Sau cung moi nang cap **Saga/Outbox** khi he thong can scale va tich hop nhieu service.

