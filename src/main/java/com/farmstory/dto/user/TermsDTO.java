package com.farmstory.dto.user;


import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TermsDTO {

    private int seq;
    private String terms;
    private String privacy;


}
