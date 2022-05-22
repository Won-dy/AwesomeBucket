package dyveloper.awesomebucket.domain.bucketlist;

import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BucketListRepository extends JpaRepository<BucketList, Long> {

    List<BucketList> findByUserAndIsDeleted(User user, boolean isDeleted, Sort sort);

    List<BucketList> findByUserAndIsDeletedAndCategory(User user, boolean isDeleted, Category category, Sort sort);

}
