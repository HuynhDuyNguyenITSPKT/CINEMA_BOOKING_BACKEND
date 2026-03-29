package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.SeatType;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.SeatTypeRepository;
import com.movie.cinema_booking_backend.request.SeatTypeRequest;
import com.movie.cinema_booking_backend.response.SeatTypeResponse;
import com.movie.cinema_booking_backend.service.ISeatTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatTypeServiceImpl implements ISeatTypeService {

    private final SeatTypeRepository seatTypeRepository;

    public SeatTypeServiceImpl(SeatTypeRepository seatTypeRepository) {
        this.seatTypeRepository = seatTypeRepository;
    }

    @Override
    public List<SeatTypeResponse> getAllSeatTypes() {
        return seatTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeatTypeResponse getSeatTypeById(String id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public SeatTypeResponse createSeatType(SeatTypeRequest request) {
        if (seatTypeRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SEAT_TYPE_NAME_EXISTS);
        }

        SeatType seatType = SeatType.builder()
                .name(request.getName())
                .surcharge(request.getSurcharge())
                .build();

        return toResponse(seatTypeRepository.save(seatType));
    }

    @Override
    @Transactional
    public SeatTypeResponse updateSeatType(String id, SeatTypeRequest request) {
        SeatType seatType = findByIdOrThrow(id);

        if (seatTypeRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_NAME_EXISTS);
        }

        seatType.setName(request.getName());
        seatType.setSurcharge(request.getSurcharge());

        return toResponse(seatTypeRepository.save(seatType));
    }

    @Override
    @Transactional
    public void deleteSeatType(String id) {
        SeatType seatType = findByIdOrThrow(id);
        seatTypeRepository.delete(seatType);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private SeatType findByIdOrThrow(String id) {
        return seatTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
    }

    private SeatTypeResponse toResponse(SeatType seatType) {
        return SeatTypeResponse.builder()
                .id(seatType.getId())
                .name(seatType.getName())
                .surcharge(seatType.getSurcharge())
                .build();
    }
}
