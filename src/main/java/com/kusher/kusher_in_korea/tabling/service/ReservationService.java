package com.kusher.kusher_in_korea.tabling.service;

import com.kusher.kusher_in_korea.tabling.domain.Reservation;
import com.kusher.kusher_in_korea.tabling.domain.Restaurant;
import com.kusher.kusher_in_korea.tabling.dto.request.CreateReservationDto;
import com.kusher.kusher_in_korea.tabling.dto.request.CreateRestaurantDto;
import com.kusher.kusher_in_korea.tabling.dto.response.ReservationDto;
import com.kusher.kusher_in_korea.tabling.dto.request.UpdateReservationDto;
import com.kusher.kusher_in_korea.tabling.dto.response.RestaurantDto;
import com.kusher.kusher_in_korea.tabling.repository.ReservationRepository;
import com.kusher.kusher_in_korea.tabling.repository.RestaurantRepository;
import com.kusher.kusher_in_korea.tabling.repository.UserRepository;
import com.kusher.kusher_in_korea.util.exception.CustomException;
import com.kusher.kusher_in_korea.util.exception.ReserveFailException;
import com.kusher.kusher_in_korea.util.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 전체 예약 조회
    public List<ReservationDto> findAllReservation() {
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationDto> reservationDtos = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservationDtos.add(new ReservationDto(reservation));
        }
        return reservationDtos;
    }

    // 예약 생성
    public Long createReservation(CreateReservationDto createReservationDto) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(createReservationDto.getRestaurantId());
        Restaurant restaurant = optionalRestaurant.orElseThrow(() -> new CustomException(ResponseCode.RESTAURANT_NOT_FOUND));
        String time = createReservationDto.getReservationTime();
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        checkAvailableVisitorCount(restaurant, dateTime, createReservationDto.getNumberOfPeople().intValue());
        Reservation reservation = Reservation.createReservation(createReservationDto);
        reservation.setRestaurant(restaurantRepository.findById(createReservationDto.getRestaurantId()).orElseThrow(() -> new CustomException(ResponseCode.RESTAURANT_NOT_FOUND)));
        reservation.setUser(userRepository.findById(createReservationDto.getUserId()).orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND)));
        return reservationRepository.save(reservation).getId();
    }

    // 그 시간대의 최대 수용인원을 초과하는지 검증하는 메서드
    private void checkAvailableVisitorCount(Restaurant restaurant, LocalDateTime reservationTime, int numberOfPeople) {
        int totalVisitorCount = reservationRepository.countTotalVisitorCount(restaurant,
                reservationTime.toLocalDate(),
                reservationTime.toLocalTime()
        ).orElse(0);

        boolean isAvailableVisitorCount = restaurant.isAvailableVisitorCount(totalVisitorCount, numberOfPeople);
        if(!isAvailableVisitorCount) {
            throw new ReserveFailException(
                    String.format(
                            "Resevatoin for restaurant ID %d on %s failed. "
                                    + "Requested visitor count is %d, but maximum available visitor count is %d",
                            restaurant.getId(),
                            reservationTime,
                            numberOfPeople,
                            restaurant.getCapacity() - totalVisitorCount
                    )
            );
        }
    }

    // 예약 수정(시간, 인원수)
    public Long updateReservation(Long reservationId, UpdateReservationDto updateReservationDto) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ResponseCode.RESERVATION_NOT_FOUND));
        // 여기 isExceed 필요
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(updateReservationDto.getRestaurantId());
        Restaurant restaurant = optionalRestaurant.orElseThrow(() -> new CustomException(ResponseCode.RESTAURANT_NOT_FOUND));
        String time = updateReservationDto.getReservationTime();
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        checkAvailableVisitorCount(restaurant, dateTime, updateReservationDto.getNumberOfPeople().intValue());
        reservation.changeReservation(updateReservationDto.getReservationDate(), updateReservationDto.getReservationTime(), updateReservationDto.getNumberOfPeople());
        reservationRepository.save(reservation);
        return reservation.getId();
    }

    // 예약 취소(상태 변경)
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ResponseCode.RESERVATION_NOT_FOUND));
        reservation.cancelReservation();
        reservationRepository.save(reservation);
    }

}
