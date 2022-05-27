package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.category.CategoryRepository;
import dyveloper.awesomebucket.domain.category.CategoryService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.exception.DuplicateResourceException;
import dyveloper.awesomebucket.exception.NotFoundResourceException;
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

    /**
     * 카테고리 수정 API
     */
    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity updateCategory(@PathVariable Long categoryId,
                                         @RequestBody CategoryDto.CreateUpdateRequestDto createUpdateRequestDto) {
        try {
            User user = userService.findLoginUserById(createUpdateRequestDto.getUserId());  // 로그인 유저 확인
            int result = categoryService.updateCategory(categoryId, user, createUpdateRequestDto.getName().trim());  // 카테고리 수정

            if (result == 0)   // 카테고리 수정 성공
                return new ResponseEntity<>(new ResultDto(200, "Update Category Success"), HttpStatus.OK);

            else if (result == 1)   // 디폴트 카테고리 수정 시도
                return new ResponseEntity<>(new ErrorResultDto(403, "Can't Update Default Category", "기본 카테고리는 수정할 수 없습니다"), HttpStatus.FORBIDDEN);

            else  // 남의 카테고리 수정 시도
                return new ResponseEntity(new ErrorResultDto(403, "Can't Update Category", "해당 카테고리의 수정 권한이 없습니다"), HttpStatus.FORBIDDEN);

        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (NotFoundResourceException nfre) {  // 존재하지 않는 카테고리
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found Category", "존재하지 않는 카테고리입니다"), HttpStatus.NOT_FOUND);
        } catch (DuplicateResourceException dre) {  // 이미 존재하는 카테고리
            return new ResponseEntity<>(new ErrorResultDto(400, "Category Already Exists", dre.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 카테고리 삭제 API
     */
    @DeleteMapping("/users/{userId}/categories/{categoryId}")
    public ResponseEntity deleteCategory(@PathVariable Long userId,
                                         @PathVariable Long categoryId) {
        try {
            User user = userService.findLoginUserById(userId);  // 로그인 유저 확인
            int result = categoryService.deleteCategory(user, categoryId);  // 카테고리 삭제

            if (result == 0)   // 카테고리 삭제 성공
                return new ResponseEntity<>(new ResultDto(200, "Delete Category Success"), HttpStatus.OK);

            else if (result == 1)   // 디폴트 카테고리 삭제 시도
                return new ResponseEntity<>(new ErrorResultDto(403, "Can't Delete Default Category", "기본 카테고리는 삭제할 수 없습니다"), HttpStatus.FORBIDDEN);

            else  // 남의 카테고리 삭제 시도
                return new ResponseEntity(new ErrorResultDto(403, "Can't Delete Category", "해당 카테고리의 삭제 권한이 없습니다"), HttpStatus.FORBIDDEN);

        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (NotFoundResourceException nfre) {  // 존재하지 않는 카테고리
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found Category", "존재하지 않는 카테고리입니다"), HttpStatus.NOT_FOUND);
        }
    }

}
