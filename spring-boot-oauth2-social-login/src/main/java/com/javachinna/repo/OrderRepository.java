package com.javachinna.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.javachinna.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Order findByRazorpayOrderId(String orderId);
}
