package org.example.be17pickcook.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;


}
