package com.expo.controller.customer;

import com.expo.domain.Reservation;
import com.expo.security.CustomerSession;
import com.expo.service.QrGenerator;
import com.expo.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 입장 배지 QR 이미지. 본인 소유·확정된 예약만 발급한다.
 */
@RestController
@RequiredArgsConstructor
public class QrController {

    private final ReservationService reservationService;

    @GetMapping(value = "/customer/reservations/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable Long id, HttpSession session) {
        Reservation r = reservationService.getDetail(id);
        Long customerId = CustomerSession.getId(session);
        if (customerId == null || !r.getCustomer().getId().equals(customerId) || r.getCheckinToken() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] png = QrGenerator.png(r.getId() + ":" + r.getCheckinToken(), 220);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }
}
