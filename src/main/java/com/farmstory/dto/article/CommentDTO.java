package com.farmstory.dto.article;

import com.farmstory.dto.user.UserDTO;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {

    private int no;
    private int parent;
    private String content;
    private String writer;
    private String regip;
    private String rdate;

    private UserDTO user;
}
