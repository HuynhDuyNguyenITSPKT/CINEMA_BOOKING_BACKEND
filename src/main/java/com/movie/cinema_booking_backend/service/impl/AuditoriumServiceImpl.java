package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.entity.SeatType;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.SeatRepository;
import com.movie.cinema_booking_backend.repository.SeatTypeRepository;
import com.movie.cinema_booking_backend.repository.TicketRepository;
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
    private final TicketRepository ticketRepository;
    private final SeatTemplateGenerator seatTemplateGenerator;

    public AuditoriumServiceImpl(AuditoriumRepository auditoriumRepository,
                                 SeatRepository seatRepository,
                                 SeatTypeRepository seatTypeRepository,
                                 TicketRepository ticketRepository,
                                 SeatTemplateGenerator seatTemplateGenerator) {
        this.auditoriumRepository    = auditoriumRepository;
        this.seatRepository          = seatRepository;
        this.seatTypeRepository      = seatTypeRepository;
        this.ticketRepository        = ticketRepository;
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
        validateSeatLayoutRequired(request);

        if (auditoriumRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AUDITORIUM_NAME_EXISTS);
        }

        // 1. Tạo và lưu Auditorium (chưa có seatCount thực)
        Auditorium auditorium = Auditorium.builder()
                .name(request.getName())
                .status(request.getStatus())
                .seatCount(0)
                .totalRows(request.getSeatLayout().getTotalRows())
                .totalColumns(request.getSeatLayout().getTotalColumns())
                .build();
        auditoriumRepository.save(auditorium);

        // 2. Load SeatType map
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
        validateSeatLayoutRequired(request);

        Auditorium auditorium = findByIdOrThrow(id);

        // Không cho regenerate khi đã có ticket tham chiếu tới ghế của phòng này
        if (ticketRepository.existsAnyByAuditoriumId(id)) {
            throw new AppException(ErrorCode.AUDITORIUM_HAS_TICKETS);
        }

        // 1. Xoá toàn bộ ghế cũ (mô phỏng sửa phòng chiếu vật lý)
        List<Seat> existingSeats = seatRepository.findByAuditoriumId(id);
        seatRepository.deleteAll(existingSeats);

        // 2. Load SeatType map
        Map<String, SeatType> seatTypeMap = loadSeatTypeMap();

        // 3. Generate ghế mới
        List<Seat> newSeats = seatTemplateGenerator.generate(
                auditorium, request.getSeatLayout(), seatTypeMap);

        seatRepository.saveAll(newSeats);

        // 4. Cập nhật seatCount + kích thước lưới mới
        auditorium.setSeatCount(newSeats.size());
        auditorium.setTotalRows(request.getSeatLayout().getTotalRows());
        auditorium.setTotalColumns(request.getSeatLayout().getTotalColumns());
        auditoriumRepository.save(auditorium);

        return toResponse(auditorium);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Auditorium findByIdOrThrow(String id) {
        return auditoriumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUDITORIUM_NOT_FOUND));
    }

    private void validateSeatLayoutRequired(AuditoriumRequest request) {
        if (request == null || request.getSeatLayout() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * Load tất cả SeatType từ DB, đưa vào Map<id → SeatType>.
     * Generator dùng: seatTypeMap.get(id).
     */
    private Map<String, SeatType> loadSeatTypeMap() {
        return seatTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        SeatType::getId,
                        st -> st,
                        (a, b) -> a
                ));
    }

    private AuditoriumResponse toResponse(Auditorium auditorium) {
        return AuditoriumResponse.builder()
                .id(auditorium.getId())
                .name(auditorium.getName())
                .totalSeats(auditorium.getSeatCount())
                .status(auditorium.getStatus())
                .totalRows(auditorium.getTotalRows())
                .totalColumns(auditorium.getTotalColumns())
                .build();
    }
}
