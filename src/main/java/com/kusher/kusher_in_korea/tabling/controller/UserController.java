package com.kusher.kusher_in_korea.tabling.controller;

import com.kusher.kusher_in_korea.tabling.dto.response.ReservationDto;
import com.kusher.kusher_in_korea.tabling.dto.response.UserDto;
import com.kusher.kusher_in_korea.tabling.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // 유저 추가
    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserDto userDto) {
        Long userId = userService.createUser(userDto);
        return ResponseEntity.ok(userId);
    }

    // 유저 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        UserDto userDto = userService.getUser(userId);
        return ResponseEntity.ok(userDto);
    }

    // 유저 정보 수정
    @PutMapping("/{userId}")
    public ResponseEntity<Long> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        Long updatedUserId = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUserId);
    }

    // 유저의 예약 조회
    @GetMapping("/{userId}/reservations")
    public ResponseEntity<List<ReservationDto>> getUserReservations(@PathVariable Long userId) {
        List<ReservationDto> reservationDtos = userService.getUserReservation(userId);
        return ResponseEntity.ok(reservationDtos);
    }
}
