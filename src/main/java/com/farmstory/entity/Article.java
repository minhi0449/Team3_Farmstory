package com.farmstory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ano;



    @Column(name = "`group`")
    private String group;
    private String cate;

    private String title;
    private String content;
    private int comment;
    private int file;
    private int hit;
    private String regip;

    @CreationTimestamp
    private LocalDateTime rdate;

    @ManyToOne
    @JoinColumn(name = "writer")
    private User user;


    // 추가필드
    @Transient // 엔티티 속성에서 제외시키는 어노테이션, 테이블의 컬럼 생성 안함
    private String nick;

    @ToString.Exclude
    @OneToMany(mappedBy = "ano") // mappedBy는 매핑되는 엔티티(테이블)의 FK 컬럼
    @Builder.Default
    private List<FileEntity> fileList = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Comment> commentList = new ArrayList<>();

    public void addNick(String nick) {
        this.nick = nick;
    }
    public void addUser(User user) {
        this.user = user;
    }


    /*
    DTO 변환 메서드 대신 ModelMapper 사용

    public ArticleDTO toDTO(){
        return ArticleDTO.builder()
                .no(no)
                .cate(cate)
                .title(title)
                .build();
    }
    */

}
