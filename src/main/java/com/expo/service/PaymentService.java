package com.expo.service;

import com.expo.domain.PaymentProvider;
import com.expo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 결제 처리. PortOne 키가 모두 있으면 실제 카카오페이 결제를 서버 검증하고,
 * 없으면 Mock 결제로 폴백한다. 결제 확정(상태 전이)은 ReservationService에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${portone.store-id:}")
    private String storeId;

    @Value("${portone.channel-key:}")
    private String channelKey;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    private final ReservationService reservationService;

    /** 3개 키가 모두 설정된 경우에만 실제 연동을 사용한다. */
    public boolean isPortOneEnabled() {
        return StringUtils.hasText(storeId) && StringUtils.hasText(channelKey) && StringUtils.hasText(apiSecret);
    }

    public String getStoreId() {
        return storeId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    /** Mock 결제: 즉시 성공 처리하고 예약을 확정한다. */
    public void payByMock(Long reservationId) {
        reservationService.confirmPayment(reservationId, PaymentProvider.MOCK, "MOCK-" + System.currentTimeMillis());
    }

    /**
     * PortOne 결제 검증 후 확정. paymentId로 결제 내역을 조회해
     * 상태(PAID)와 금액 일치를 서버에서 확인한다(위변조 방지).
     */
    @SuppressWarnings("unchecked")
    public void verifyAndConfirm(Long reservationId, String paymentId) {
        Reservation reservation = reservationService.getDetail(reservationId);
        int expected = reservation.getAmount();

        Map<String, Object> body = RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .body(Map.class);

        if (body == null || !"PAID".equals(body.get("status"))) {
            throw new IllegalStateException("결제가 완료되지 않았습니다.");
        }
        if (extractTotal(body) != expected) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }
        reservationService.confirmPayment(reservationId, PaymentProvider.PORTONE, paymentId);
    }

    private int extractTotal(Map<String, Object> body) {
        Object amount = body.get("amount");
        if (amount instanceof Map<?, ?> m && m.get("total") instanceof Number n) {
            return n.intValue();
        }
        return -1;
    }
}
