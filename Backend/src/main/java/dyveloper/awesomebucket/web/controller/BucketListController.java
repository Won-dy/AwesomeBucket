package dyveloper.awesomebucket.web.controller;

import dyveloper.awesomebucket.domain.bucketlist.BucketList;
import dyveloper.awesomebucket.domain.bucketlist.BucketListRepository;
import dyveloper.awesomebucket.domain.user.UserService;
import dyveloper.awesomebucket.exception.UnauthorizedAccessException;
import dyveloper.awesomebucket.web.dto.BucketListDto;
import dyveloper.awesomebucket.web.dto.ErrorResultDto;
import dyveloper.awesomebucket.web.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BucketListController {

    private final BucketListRepository bucketListRepository;
    private final UserService userService;

    /**
     * 버킷리스트 목록 조회 API
     */
    @GetMapping("/users/{userId}/buckets")
    public ResponseEntity bucketLists(@PathVariable Long userId) {
        try {
            List<BucketList> bucketList = bucketListRepository.findByUserAndIsDeleted(userService.findLoginUserById(userId), false);

            List<BucketListDto.FindResponseDto> data = new ArrayList<>();

            for (BucketList bucket : bucketList)
                data.add(new BucketListDto.FindResponseDto(bucket.getId(), bucket.getTitle(), bucket.getImportance(), bucket.getAchievementRate(), bucket.getTargetDate(), bucket.getCategory().getName()));

            return new ResponseEntity<>(new ResultDto(200, "Load BucketList", data), HttpStatus.OK);

        } catch (UnauthorizedAccessException e) {  // DB에 없는 사용자가 임의 접근
            return new ResponseEntity<>(new ErrorResultDto(401, "Unauthorized Access", "로그인이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }
    }

}
