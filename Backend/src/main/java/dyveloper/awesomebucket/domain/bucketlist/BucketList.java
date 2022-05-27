package dyveloper.awesomebucket.domain.bucketlist;

import dyveloper.awesomebucket.domain.BaseEntity;
import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.user.User;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
public class BucketList extends BaseEntity {  // 버킷리스트

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bucket_list_id")
    private Long id;  // 버킷리스트 ID

    private String title;  // 버킷리스트명
    private String memo;  // 메모

    private int importance;  // 중요도
    private int achievementRate;  // 달성률

    private LocalDate targetDate;  // 목표일
    private LocalDate achievementDate;  // 달성일

    private boolean isDeleted;  // 삭제 여부

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 회원

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;  // 카테고리


    //==비즈니스 로직==//

    // 카테고리 삭제시 기타 카테고리로 변경
    public void setCategoryDefault(Category defaultCategory) {
        this.category = defaultCategory;
    }
}
