package com.farmstory.controller;


import com.farmstory.dto.BuyDTO;
import com.farmstory.dto.CartRequestDTO;
import com.farmstory.dto.CartResponseDTO;
import com.farmstory.dto.ProductDTO;
import com.farmstory.entity.Product;
import com.farmstory.entity.User;
import com.farmstory.repository.ProductRepository;
import com.farmstory.security.MyUserDetails;
import com.farmstory.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 지현님&상훈님 - market
@Log4j2
@RequiredArgsConstructor
@Controller
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
//    private final ProductService productService;

    // cart
    @GetMapping("/market/cart") //cart 화면 구현
    public String cart(@RequestParam("uid") String uid, Model model) {
        long count = cartService.count();
        model.addAttribute("count", count);

        log.info("uid :" + uid);
        List<CartResponseDTO> cartdto = cartService.selectAllCartByUid(uid);
        log.info("cartdto : " + cartdto);
        model.addAttribute("cart", cartdto);

        return "/market/cart";
    }

    @ResponseBody
    @PostMapping("/market/cart") //view -> ajax
    public ResponseEntity<CartRequestDTO> cart(@RequestBody CartRequestDTO cartRequestDTO) {
        String uid = cartRequestDTO.getUid();
        log.info("uid :" + uid);
        CartRequestDTO dto = cartService.insertCart(cartRequestDTO);
        dto.setUid(uid);
        log.info("dto : " + dto);

        return ResponseEntity
                .ok()
                .body(dto);
    }

    @GetMapping("/market/order") //바로 구매 type = right 입니다.
    public String order(@RequestParam(name = "type", required = false) String type, @AuthenticationPrincipal MyUserDetails userDetails, Model model, HttpSession session) {
        if (userDetails == null) {
            return "redirect:/user/login";
        }

        if (type != null && type.equals("right")) {
            if (session.getAttribute("rightBuy") == null) {
                return "redirect:/";
            }
            BuyDTO rightBuy = (BuyDTO) session.getAttribute("rightBuy");
            Product product = productRepository.findById(rightBuy.getProduct_id());
            ProductDTO productDTO = ProductDTO.fromEntity(product);
            User user = userDetails.getUser();

            model.addAttribute("user", user);
            model.addAttribute("product", productDTO);
            model.addAttribute("count", rightBuy.getCount());
            return "/market/order-buy";
        }else {
            if (session.getAttribute("carts") == null) {
                return "redirect:/market/cart?uid=" + userDetails.getUsername();
            }
            List<Integer> sessionCarts = (List<Integer>) session.getAttribute("carts");
            List<CartResponseDTO> carts = sessionCarts.stream().map(cartService::getWithProductById).toList();


            User user = userDetails.getUser();

            model.addAttribute("user", user);
            model.addAttribute("carts", carts);
            return "/market/order";
        }
    }

    @PostMapping("/market/order") // 주문하기 -> 카운트업데이트 후 세션저장
    public ResponseEntity<?> orders(@RequestParam("cartNo") List<Integer> cartNo, @RequestParam("count") List<Integer> count, HttpSession session) {
        cartService.UpdateCart(cartNo, count);
        session.setAttribute("carts", cartNo);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @DeleteMapping("/market/cart/delete")
    public ResponseEntity<?> delete(@RequestBody List<Integer> data) {
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 항목이 없습니다.");
        } else {
            cartService.deleteCart(data);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/market/buy")
    public ResponseEntity<?> buy(@RequestBody BuyDTO buyDTO, HttpSession session) {

        int productId = buyDTO.getProduct_id();
        int count = buyDTO.getCount();
        String uid = buyDTO.getUid();

        session.setAttribute("rightBuy", buyDTO);

        return ResponseEntity.ok().body(buyDTO);

    }

}
