package dyveloper.awesomebucket.domain.bucketlist;

import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.category.CategoryRepository;
import dyveloper.awesomebucket.domain.category.CategoryService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.exception.BadRequestURIException;
import dyveloper.awesomebucket.exception.NotFoundResourceException;
import dyveloper.awesomebucket.web.dto.BucketListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BucketListService {

    private final BucketListRepository bucketListRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    // 버킷리스트 등록
    public void createBucketList(User user, BucketListDto.CreateUpdateRequestDto createUpdateRequestDto) {
        Category category = categoryService.findByUserAndName(user, createUpdateRequestDto.getCategoryName());  // 카테고리 조회
        if (category != null) {  // 유저 카테고리의 버킷 저장
            bucketListRepository.save(BucketList.createBucketList(createUpdateRequestDto.getTitle(), createUpdateRequestDto.getMemo(), createUpdateRequestDto.getImportance(),
                    createUpdateRequestDto.getAchievementRate(), createUpdateRequestDto.getTargetDate(), createUpdateRequestDto.getAchievementDate(), user, category));
        } else {
            Category defaultCategory = categoryRepository.findByNameAndIsDefault(createUpdateRequestDto.getCategoryName(), true).orElse(null);  // 디폴트 카테고리 조회
            if (defaultCategory != null) {  // 디폴트 카테고리의 버킷 저장
                bucketListRepository.save(BucketList.createBucketList(createUpdateRequestDto.getTitle(), createUpdateRequestDto.getMemo(), createUpdateRequestDto.getImportance(),
                        createUpdateRequestDto.getAchievementRate(), createUpdateRequestDto.getTargetDate(), createUpdateRequestDto.getAchievementDate(), user, defaultCategory));
            } else {  // 저장 실패
                throw new NotFoundResourceException("존재하지 않는 카테고리입니다");
            }
        }
    }

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
