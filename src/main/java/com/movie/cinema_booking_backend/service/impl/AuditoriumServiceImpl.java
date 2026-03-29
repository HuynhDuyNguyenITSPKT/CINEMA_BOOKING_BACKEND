package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.request.AuditoriumRequest;
import com.movie.cinema_booking_backend.response.AuditoriumResponse;
import com.movie.cinema_booking_backend.service.IAuditoriumService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriumServiceImpl implements IAuditoriumService {

    private final AuditoriumRepository auditoriumRepository;

    public AuditoriumServiceImpl(AuditoriumRepository auditoriumRepository) {
        this.auditoriumRepository = auditoriumRepository;
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
        Auditorium auditorium = findByIdOrThrow(id);
        return toResponse(auditorium);
    }

    @Override
    @Transactional
    public AuditoriumResponse createAuditorium(AuditoriumRequest request) {
        if (auditoriumRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AUDITORIUM_NAME_EXISTS);
        }

        Auditorium auditorium = Auditorium.builder()
                .name(request.getName())
                .seatCount(request.getSeatCount())
                .status(request.getStatus())
                .build();

        return toResponse(auditoriumRepository.save(auditorium));
    }

    @Override
    @Transactional
    public AuditoriumResponse updateAuditorium(String id, AuditoriumRequest request) {
        Auditorium auditorium = findByIdOrThrow(id);

        if (auditoriumRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.AUDITORIUM_NAME_EXISTS);
        }

        auditorium.setName(request.getName());
        auditorium.setSeatCount(request.getSeatCount());
        auditorium.setStatus(request.getStatus());

        return toResponse(auditoriumRepository.save(auditorium));
    }

    @Override
    @Transactional
    public void deleteAuditorium(String id) {
        Auditorium auditorium = findByIdOrThrow(id);
        auditoriumRepository.delete(auditorium);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Auditorium findByIdOrThrow(String id) {
        return auditoriumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUDITORIUM_NOT_FOUND));
    }

    private AuditoriumResponse toResponse(Auditorium auditorium) {
        return AuditoriumResponse.builder()
                .id(auditorium.getId())
                .name(auditorium.getName())
                .seatCount(auditorium.getSeatCount())
                .status(auditorium.getStatus())
                .build();
    }
}
