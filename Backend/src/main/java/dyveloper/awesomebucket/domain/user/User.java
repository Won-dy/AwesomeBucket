package dyveloper.awesomebucket.domain.user;

import dyveloper.awesomebucket.domain.BaseEntity;
import dyveloper.awesomebucket.domain.bucketlist.BucketList;
import dyveloper.awesomebucket.domain.category.Category;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class User extends BaseEntity {  // 회원

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;  // 회원 ID

    private String email;  // 이메일
    private String password;  // 비밀번호

    private String name;  // 이름
    private String tel;  // 전화번호

    @OneToMany(mappedBy = "user")
    private List<Category> categories = new ArrayList<>();  // 카테고리

    @OneToMany(mappedBy = "user")
    private List<BucketList> bucketLists = new ArrayList<>();  // 버킷 리스트

}
