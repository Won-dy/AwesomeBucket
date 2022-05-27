package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.category.CategoryRepository;
import dyveloper.awesomebucket.domain.category.CategoryService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.exception.DuplicateResourceException;
import dyveloper.awesomebucket.exception.UnauthorizedAccessException;
import dyveloper.awesomebucket.web.dto.CategoryDto;
import dyveloper.awesomebucket.web.dto.ErrorResultDto;
import dyveloper.awesomebucket.web.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    /**
     * 카테고리 목록 조회 API
     */
    @GetMapping("/users/{userId}/categories")
    public ResponseEntity categories(@PathVariable Long userId) {

        try {
            List<Category> categories = categoryRepository.findByUserOrIsDefault(userService.findLoginUserById(userId), true);

            List<CategoryDto.FindResponseDto> data = new ArrayList<>();

            for (Category category : categories) {
                data.add(new CategoryDto.FindResponseDto(category.getId(), category.getName(), category.isDefault()));
            }

            return new ResponseEntity<>(new ResultDto(200, "Load Category", data), HttpStatus.OK);

        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 카테고리 등록 API
     */
    @PostMapping("/categories")
    public ResponseEntity createCategory(@RequestBody CategoryDto.CreateUpdateRequestDto createUpdateRequestDto) {
        try {
            User user = userService.findLoginUserById(createUpdateRequestDto.getUserId());  // 로그인 유저 확인
            CategoryDto.IdResponseDto data = categoryService.createCategory(user, createUpdateRequestDto.getName().trim());
            return new ResponseEntity<>(new ResultDto(200, "Add Category Success", data), HttpStatus.OK);
        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (DuplicateResourceException dre) {  // 이미 존재하는 카테고리
            return new ResponseEntity<>(new ErrorResultDto(400, "Category Already Exists", dre.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
