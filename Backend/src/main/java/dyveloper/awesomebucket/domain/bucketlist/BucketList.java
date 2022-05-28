package dyveloper.awesomebucket.domain.bucketlist;

import dyveloper.awesomebucket.domain.BaseEntity;
import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@NoArgsConstructor
@DynamicInsert
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

    @Builder
    public BucketList(String title, String memo, int importance, int achievementRate, LocalDate targetDate, LocalDate achievementDate, boolean isDeleted, User user, Category category) {
        this.title = title;
        this.memo = memo;
        this.importance = importance;
        this.achievementRate = achievementRate;
        this.targetDate = targetDate;
        this.achievementDate = achievementDate;
        this.isDeleted = isDeleted;
        this.user = user;
        this.category = category;
    }

    //==비즈니스 로직==//

    // 카테고리 삭제시 기타 카테고리로 변경
    public void setCategoryDefault(Category defaultCategory) {
        this.category = defaultCategory;
    }


    //== 연관관계 메서드 ==//
    public void setUser(User user) {
        this.user = user;
        user.getBucketLists().add(this);
    }

    public void setCategory(Category category) {
        this.category = category;
        category.getBucketLists().add(this);
    }


    //== 생성 메서드 ==//
    public static BucketList createBucketList(String title, String memo, int importance, int achievementRate, LocalDate targetDate, LocalDate achievementDate, User user, Category category) {
        BucketList bucketList = BucketList.builder()
                .title(title)
                .memo(memo)
                .importance(importance)
                .achievementRate(achievementRate)
                .targetDate(targetDate)
                .achievementDate(achievementDate)
                .user(user)
                .category(category).build();
        bucketList.setUser(user);
        bucketList.setCategory(category);
        return bucketList;
    }
}
