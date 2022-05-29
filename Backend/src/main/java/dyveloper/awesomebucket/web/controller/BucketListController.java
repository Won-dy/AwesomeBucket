package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.bucketlist.BucketList;
import dyveloper.awesomebucket.domain.bucketlist.BucketListService;
import dyveloper.awesomebucket.domain.category.Category;
import dyveloper.awesomebucket.domain.category.CategoryService;
import dyveloper.awesomebucket.domain.user.User;
import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.exception.BadRequestURIException;
import dyveloper.awesomebucket.exception.NotFoundResourceException;
import dyveloper.awesomebucket.exception.UnauthorizedAccessException;
import dyveloper.awesomebucket.web.dto.BucketListDto;
import dyveloper.awesomebucket.web.dto.ErrorResultDto;
import dyveloper.awesomebucket.web.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BucketListController {

    private final BucketListService bucketListService;
    private final UserService userService;
    private final CategoryService categoryService;

    /**
     * 버킷리스트 목록 조회 및 정렬 API
     */
    @GetMapping("/users/{userId}/buckets")
    public ResponseEntity sortedBucketLists(@PathVariable Long userId,
                                            @RequestParam("sort") String sort,
                                            @RequestParam("direction") String direction,
                                            @RequestParam(value = "category", required = false) String categoryName) {
        try {
            User user = userService.findLoginUserById(userId);  // 로그인 유저 확인

            List<BucketListDto.FindResponseDto> data = new ArrayList<>();
            List<BucketList> bucketLists;

            // 로그인 유저의 전체 버킷리스트를 조회하여 정렬
            if (categoryName == null) {
                bucketLists = bucketListService.getByUserAndIsDeleted(user, false, sort, direction);
                for (BucketList bucketList : bucketLists)
                    data.add(new BucketListDto.FindResponseDto(bucketList.getId(), bucketList.getTitle(), bucketList.getImportance(), bucketList.getAchievementRate(), bucketList.getTargetDate(), bucketList.getCategory().getName()));

                return new ResponseEntity<>(new ResultDto(200, "Load BucketList", data), HttpStatus.OK);
            }
            // 로그인 유저의 카테고리별 버킷리스트 조회하여 정렬
            else {
                Category userCategory = categoryService.findByUserAndName(user, categoryName);
                // user의 개인 카테고리
                if (userCategory != null) {
                    bucketLists = bucketListService.getByUserAndIsDeletedAndCategory(user, false, userCategory, sort, direction);
                    for (BucketList bucketList : bucketLists)
                        data.add(new BucketListDto.FindResponseDto(bucketList.getId(), bucketList.getTitle(), bucketList.getImportance(), bucketList.getAchievementRate(), bucketList.getTargetDate(), bucketList.getCategory().getName()));

                    return new ResponseEntity<>(new ResultDto(200, "Load BucketList", data), HttpStatus.OK);
                } else {
                    Category defaultCategory = categoryService.findByName(categoryName);
                    // 디폴트 카테고리
                    if (defaultCategory != null) {
                        bucketLists = bucketListService.getByUserAndIsDeletedAndCategory(user, false, defaultCategory, sort, direction);
                        for (BucketList bucketList : bucketLists)
                            data.add(new BucketListDto.FindResponseDto(bucketList.getId(), bucketList.getTitle(), bucketList.getImportance(), bucketList.getAchievementRate(), bucketList.getTargetDate(), bucketList.getCategory().getName()));

                        return new ResponseEntity<>(new ResultDto(200, "Load BucketList", data), HttpStatus.OK);
                    } else {
                        throw new NotFoundResourceException("존재하지 않는 카테고리입니다");
                    }
                }
            }
        } catch (UnauthorizedAccessException uae) {
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (BadRequestURIException | PropertyReferenceException e) {
            return new ResponseEntity<>(new ErrorResultDto(400, "Bad Request: " + e.getMessage(), "요청 파라미터가 잘못 사용되었습니다."), HttpStatus.BAD_REQUEST);
        } catch (NotFoundResourceException nfre) {
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found Category", nfre.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 버킷리스트 상세 조회 API
     */
    @GetMapping("/users/{userId}/buckets/{bucketId}")
    public ResponseEntity sortedBucketLists(@PathVariable Long userId, @PathVariable Long bucketId) {
        try {
            User user = userService.findLoginUserById(userId);  // 로그인 유저 확인
            BucketListDto.FindDetailResponseDto data = bucketListService.getBucketListDetail(user, bucketId);

            return new ResponseEntity<>(new ResultDto(200, "Load BucketList Detail", data), HttpStatus.OK);
        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (NotFoundResourceException nfre) {
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found BucketList", "존재하지 않는 버킷리스트입니다다"), HttpStatus.NOT_FOUND);
        }
    }


    /**
     * 버킷리스트 등록 API
     */
    @PostMapping("/buckets")
    public ResponseEntity createBucketList(@RequestBody BucketListDto.CreateUpdateRequestDto createUpdateRequestDto) {
        try {
            User user = userService.findLoginUserById(createUpdateRequestDto.getUserId());  // 로그인 유저 확인
            bucketListService.createBucketList(user, createUpdateRequestDto);
            return new ResponseEntity<>(new ResultDto(200, "Add BucketList Success"), HttpStatus.OK);
        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        } catch (NotFoundResourceException nfre) {
            return new ResponseEntity<>(new ErrorResultDto(404, "Not Found Category", nfre.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

}
