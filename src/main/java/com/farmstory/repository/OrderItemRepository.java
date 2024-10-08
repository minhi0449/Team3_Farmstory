package com.farmstory.repository;

import com.farmstory.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @EntityGraph(attributePaths = {"product", "order"})
    @Query("SELECT oi FROM OrderItem oi  WHERE oi.order.orderNo = :orderNo")
    Page<OrderItem> findByOrderNo(@Param("orderNo") int orderNo, Pageable pageable);

    @Query("SELECT oi FROM OrderItem oi join fetch oi.product join fetch oi.order WHERE oi.order.orderNo = :orderNo")
    List<OrderItem> findByOrderNo(@Param("orderNo") int orderNo);


    @EntityGraph(attributePaths = {"product", "order", "order.user"})
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.uid = :uid")
    Page<OrderItem> findByUidWithOrderAndProduct(@Param("uid") String uid, Pageable pageable);

    @Query("SELECT COUNT(*) FROM OrderItem oi WHERE oi.order.user.uid =:uid")
    int countByUid(@Param("uid") String uid);

    @EntityGraph(attributePaths = {"product", "order", "order.user"})
    @Query("SELECT oi FROM OrderItem oi")
    Page<OrderItem> findAllWithOrderAndProduct(Pageable pageable);
}
