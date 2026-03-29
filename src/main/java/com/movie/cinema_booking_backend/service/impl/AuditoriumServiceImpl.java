package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.entity.SeatType;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.SeatRepository;
import com.movie.cinema_booking_backend.repository.SeatTypeRepository;
import com.movie.cinema_booking_backend.request.AuditoriumRequest;
import com.movie.cinema_booking_backend.response.AuditoriumResponse;
import com.movie.cinema_booking_backend.service.IAuditoriumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditoriumServiceImpl implements IAuditoriumService {

    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatTemplateGenerator seatTemplateGenerator;

    public AuditoriumServiceImpl(AuditoriumRepository auditoriumRepository,
                                 SeatRepository seatRepository,
                                 SeatTypeRepository seatTypeRepository,
                                 SeatTemplateGenerator seatTemplateGenerator) {
        this.auditoriumRepository    = auditoriumRepository;
        this.seatRepository          = seatRepository;
        this.seatTypeRepository      = seatTypeRepository;
        this.seatTemplateGenerator   = seatTemplateGenerator;
    }

    @Override
    public List<AuditoriumResponse> getAllAuditoriums() {
        return auditoriumRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AuditoriumResponse getAuditoriumById(String id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public AuditoriumResponse createAuditorium(AuditoriumRequest request) {
        if (auditoriumRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AUDITORIUM_NAME_EXISTS);
        }

        // 1. Tạo và lưu Auditorium (chưa có seatCount thực)
        Auditorium auditorium = Auditorium.builder()
                .name(request.getName())
                .status(request.getStatus())
                .seatCount(0) // 임시; cập nhật sau khi generate
                .build();
        auditoriumRepository.save(auditorium);

        // 2. Load SeatType map (STANDARD bắt buộc phải có)
        Map<String, SeatType> seatTypeMap = loadSeatTypeMap();

        // 3. Generate ghế từ SeatLayoutConfig
        List<Seat> seats = seatTemplateGenerator.generate(
                auditorium, request.getSeatLayout(), seatTypeMap);

        // 4. Lưu tất cả ghế vào DB
        seatRepository.saveAll(seats);

        // 5. Cập nhật seatCount = số ghế thực tế đã generate
        auditorium.setSeatCount(seats.size());
        auditoriumRepository.save(auditorium);

        return toResponse(auditorium);
    }

    @Override
    @Transactional
    public AuditoriumResponse updateAuditorium(String id, AuditoriumRequest request) {
        Auditorium auditorium = findByIdOrThrow(id);

        if (auditoriumRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.AUDITORIUM_NAME_EXISTS);
        }

        // Chỉ update tên và status — layout/ghế dùng endpoint regenerate-seats
        auditorium.setName(request.getName());
        auditorium.setStatus(request.getStatus());

        return toResponse(auditoriumRepository.save(auditorium));
    }

    @Override
    @Transactional
    public void deleteAuditorium(String id) {
        auditoriumRepository.delete(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public AuditoriumResponse regenerateSeats(String id, AuditoriumRequest request) {
        Auditorium auditorium = findByIdOrThrow(id);

        // 1. Xoá toàn bộ ghế cũ (mô phỏng sửa phòng chiếu vật lý)
        List<Seat> existingSeats = seatRepository.findByAuditoriumId(id);
        seatRepository.deleteAll(existingSeats);

        // 2. Load SeatType map
        Map<String, SeatType> seatTypeMap = loadSeatTypeMap();

        // 3. Generate ghế mới
        List<Seat> newSeats = seatTemplateGenerator.generate(
                auditorium, request.getSeatLayout(), seatTypeMap);

        seatRepository.saveAll(newSeats);

        // 4. Cập nhật seatCount
        auditorium.setSeatCount(newSeats.size());
        auditoriumRepository.save(auditorium);

        return toResponse(auditorium);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Auditorium findByIdOrThrow(String id) {
        return auditoriumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUDITORIUM_NOT_FOUND));
    }

    /**
     * Load tất cả SeatType từ DB, đưa vào Map<tên_uppercase → SeatType>.
     * Generator dùng: seatTypeMap.get("STANDARD"), .get("VIP"), .get("PREMIUM").
     *
     * Nếu không tìm thấy "STANDARD" → throw SEAT_TYPE_NOT_FOUND.
     * Admin phải tạo SeatType "STANDARD" trước khi tạo Auditorium.
     */
    private Map<String, SeatType> loadSeatTypeMap() {
        Map<String, SeatType> map = seatTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        st -> st.getName().toUpperCase(),
                        st -> st,
                        (a, b) -> a // nếu trùng tên (impossible với unique constraint) lấy cái đầu
                ));

        if (!map.containsKey("STANDARD")) {
            throw new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND);
        }
        return map;
    }

    private AuditoriumResponse toResponse(Auditorium auditorium) {
        return AuditoriumResponse.builder()
                .id(auditorium.getId())
                .name(auditorium.getName())
                .totalSeats(auditorium.getSeatCount())
                .status(auditorium.getStatus())
                .build();
    }
}
