package dyveloper.awesomebucket.domain.category;

import dyveloper.awesomebucket.domain.BaseEntity;
import dyveloper.awesomebucket.domain.bucketlist.BucketList;
import dyveloper.awesomebucket.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@NoArgsConstructor
@Getter
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

    @Builder
    public Category(String name, boolean isDefault, User user) {
        this.name = name;
        this.isDefault = isDefault;
        this.user = user;
    }


    //== 비즈니스 로직 ==//

    // 카테고리 수정
    public void changeName(String name) {
        this.name = name;
    }


    //== 연관관계 메서드 ==//
    public void setUser(User user) {
        this.user = user;
        user.getCategories().add(this);
    }


    //== 생성 메서드 ==//
    public static Category createCategory(String name, User user) {
        Category category = Category.builder()
                .name(name)
                .isDefault(false)
                .user(user).build();
        category.setUser(user);
        return category;
    }
}
