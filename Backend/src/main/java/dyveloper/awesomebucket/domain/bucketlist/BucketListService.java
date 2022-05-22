package dyveloper.awesomebucket.domain.bucketlist;

import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.exception.BadRequestURIException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BucketListService {

    private final BucketListRepository bucketListRepository;

    // 전체 버킷리스트 정렬
    public List<BucketList> getByUserAndIsDeleted(User user, boolean isDeleted, String sort, String direction) {
        return bucketListRepository.findByUserAndIsDeleted(user, isDeleted, sortBy(direction, sort));
    }

    // 카테고리별 버킷리스트 정렬
    public List<BucketList> getByUserAndIsDeletedAndCategory(User user, boolean isDeleted, Category category, String sort, String direction) {
        return bucketListRepository.findByUserAndIsDeletedAndCategory(user, isDeleted, category, sortBy(direction, sort));
    }

    // 정렬 기준
    private Sort sortBy(String direction, String sort) {
        Sort.Direction sortDirection;
        direction = direction.toUpperCase();

        if (direction.equals("DESC")) {
            sortDirection = Sort.Direction.DESC;
        } else if (direction.equals("ASC")) {
            sortDirection = Sort.Direction.ASC;
        } else {
            throw new BadRequestURIException("Use 'DESC' or 'ASC' for parameter 'direction'");
        }

        return Sort.by(sortDirection, sort);
    }

}
