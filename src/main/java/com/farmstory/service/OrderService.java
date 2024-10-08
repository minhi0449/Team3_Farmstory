package com.farmstory.service;

import com.farmstory.dto.ProductDTO;
import com.farmstory.dto.order.*;
import com.farmstory.dto.PageResponseDTO;
import com.farmstory.dto.user.UserDTO;
import com.farmstory.entity.Order;
import com.farmstory.entity.OrderItem;
import com.farmstory.entity.Product;
import com.farmstory.entity.User;
import com.farmstory.repository.OrderItemRepository;
import com.farmstory.repository.OrderRepository;
import com.farmstory.repository.ProductRepository;
import com.farmstory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderResponseDTO getOrderById(int id) {
        Optional<Order> findOrder = orderRepository.findById(id);
        Order order = findOrder.orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
        return OrderResponseDTO.fromEntity(order);
    }

    public OrderWithTotalResponseDTO getOrderByIdWithPrice(int id) {
        Optional<Order> findOrder = orderRepository.findByIdWithPrice(id);
        Order order = findOrder.orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
        int totalPrice = order.getOrderItems().stream()
                .mapToInt(orderItem -> orderItem.getPrice() * orderItem.getCount())
                .sum();

        return OrderWithTotalResponseDTO.fromEntity(order, totalPrice);
    }

    public PageResponseDTO<OrderItemWithOrderWithProductResponseDTO> getOrderItems(Pageable pageable) {
        Page<OrderItem> orderItems = orderItemRepository.findAllWithOrderAndProduct(pageable);
        Page<OrderItemWithOrderWithProductResponseDTO> orderItemDto = orderItems.map(orderItem -> {
            OrderItemWithOrderWithProductResponseDTO orderItemD = OrderItemWithOrderWithProductResponseDTO.fromEntity(orderItem);
            orderItemD.setProduct(ProductDTO.fromEntity(orderItem.getProduct()));
            OrderWithUserResponseDTO order = OrderWithUserResponseDTO.fromEntity(orderItem.getOrder());
            order.setUser(UserDTO.fromEntity(orderItem.getOrder().getUser()));
            orderItemD.setOrder(order);
            return orderItemD;
        });
        return PageResponseDTO.fromPage(orderItemDto);
    }

    public PageResponseDTO<OrderItemResponseDTO> getOrderItemsByOrderNo(int orderNo, Pageable pageable) {
        Page<OrderItem> orderItems = orderItemRepository.findByOrderNo(orderNo, pageable);
        Page<OrderItemResponseDTO> orderItemDto = orderItems.map(OrderItemResponseDTO::fromEntity);
        return PageResponseDTO.fromPage(orderItemDto);
    }

    public PageResponseDTO<OrderItemWithOrderWithProductResponseDTO> getOrderItemsByUid(String uid, Pageable pageable) {
        System.out.println("uid = " + uid);
        System.out.println("pageable = " + pageable);
        Page<OrderItem> orderItems = orderItemRepository.findByUidWithOrderAndProduct(uid, pageable);
        System.out.println("orderItems = " + orderItems.getContent());
        Page<OrderItemWithOrderWithProductResponseDTO> orderItemDto = orderItems.map(orderItem -> {
            OrderItemWithOrderWithProductResponseDTO orderItemD = OrderItemWithOrderWithProductResponseDTO.fromEntity(orderItem);
            orderItemD.setProduct(ProductDTO.fromEntity(orderItem.getProduct()));
            OrderWithUserResponseDTO order = OrderWithUserResponseDTO.fromEntity(orderItem.getOrder());
            order.setUser(UserDTO.fromEntity(orderItem.getOrder().getUser()));
            orderItemD.setOrder(order);
            return orderItemD;
        });
        return PageResponseDTO.fromPage(orderItemDto);
    }

    @Transactional
    public int createOrder(OrderCreateDTO orderDTO) {
        System.out.println("orderDTO = " + orderDTO);
         User user = userRepository.findById(orderDTO.getUid())
                 .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 1. Order 엔티티 생성 및 저장
        Order order = orderDTO.toEntity();
         order.changeUser(user); // User와 연결
        orderRepository.save(order);



        // 2. 주문의 OrderItem을 반복
        orderDTO.getOrderItems().forEach(orderItemDto -> {
            // orderItemDto의 productId로 제품을 가져온다\
            Product product = productRepository.findById(Integer.valueOf(orderItemDto.getProductId()))
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 product가 없습니다."));

            // 제품의 재고를 줄인다
            product.decreaseStock(orderItemDto.getCount());

            // 할인된 가격 계산
            int discountedPrice = product.getPrice() - (product.getPrice() * product.getDiscount() / 100);

            // 주문 아이템을 엔티티로 변환 후 주문과 제품의 관계를 연결한다.
            OrderItem orderItem = orderItemDto.toEntity();
            orderItem.registerOrder(order);
            orderItem.registerProduct(product);

            // 주문 아이템을 생성
            orderItemRepository.save(orderItem);

            // 주문 아이템 소계 계산 (할인된 가격 * 수량)
            int itemSubtotal = discountedPrice * orderItem.getCount();

            // 주문 아이템의 소계가 30,000원 이상이면 배송비를 0으로 설정
            if (itemSubtotal >= 30000) {
                orderItem.changeDeliveryfee(0); // 배송비를 0으로 설정
            }
        });


        // TODO: 사용자의 포인트도 수정

        return order.getOrderNo();
    }

    public int getOrderItemCountByUid(String uid) {
        return orderItemRepository.countByUid(uid);
    }

    public PageResponseDTO<OrderResponseDTO> getOrdersByUid(String uid, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAllByUid(uid, pageable);
        Page<OrderResponseDTO> map = ordersPage.map(OrderResponseDTO::fromEntity);
        return PageResponseDTO.fromPage(map);
    }
}
