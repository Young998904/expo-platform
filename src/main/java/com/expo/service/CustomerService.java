package com.expo.service;

import com.expo.domain.Customer;
import com.expo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 고객 경량 인증. 전화번호가 처음이면 가입, 이후에는 PIN을 검증한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인 또는 가입. 최초 전화번호면 이름이 필요하다.
     * @return 고객 id
     */
    public Long loginOrRegister(String phone, String pin, String name) {
        String normalized = normalize(phone);
        if (normalized.length() < 10) {
            throw new IllegalArgumentException("전화번호를 정확히 입력하세요.");
        }
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN은 숫자 4~6자리입니다.");
        }
        Optional<Customer> found = customerRepository.findByPhone(normalized);
        if (found.isPresent()) {
            Customer customer = found.get();
            if (!passwordEncoder.matches(pin, customer.getPin())) {
                throw new IllegalArgumentException("PIN이 올바르지 않습니다.");
            }
            return customer.getId();
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("최초 로그인은 이름이 필요합니다.");
        }
        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setPhone(normalized);
        customer.setPin(passwordEncoder.encode(pin));
        return customerRepository.save(customer).getId();
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객 정보를 확인할 수 없습니다."));
    }

    private String normalize(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }
}
