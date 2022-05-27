package dyveloper.awesomebucket.domain.category;

import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.exception.DuplicateResourceException;
import dyveloper.awesomebucket.web.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findByUserAndName(User user, String name) {
        return categoryRepository.findByUserAndName(user, name).orElse(null);
    }

    public Category findByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    // 카테고리 등록
    public CategoryDto.IdResponseDto createCategory(User user, String name) {
        Category userCategory = findByUserAndName(user, name);  // 유저 카테고리 조회
        Category defaultCategory = categoryRepository.findByNameAndIsDefault(name, true).orElse(null);  // 디폴트 카테고리 조회
        if (userCategory != null || defaultCategory != null) {
            throw new DuplicateResourceException("이미 존재하는 카테고리입니다.");
        } else {  // 카테고리 등록
            Category savedCategory = categoryRepository.save(Category.createCategory(name, user));
            return new CategoryDto.IdResponseDto(savedCategory.getId());
        }
    }
}
