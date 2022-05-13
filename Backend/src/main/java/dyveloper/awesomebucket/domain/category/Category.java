package dyveloper.awesomebucket.domain.category;

import dyveloper.awesomebucket.domain.BaseEntity;
import dyveloper.awesomebucket.domain.bucketlist.BucketList;
import dyveloper.awesomebucket.domain.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
public class Category extends BaseEntity {  // 카테고리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;  // 카테고리 ID

    private String name;  // 카테고리명

    private boolean isDefault;  // 디폴트 여부

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 회원

    @OneToMany(mappedBy = "category")
    private List<BucketList> bucketLists = new ArrayList<>();  // 버킷 리스트

}
