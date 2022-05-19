package dyveloper.awesomebucket.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class CategoryDto {

    /**
     * 카테고리 목록 조회 DTO
     */
    @ToString
    @Getter
    @AllArgsConstructor
    public static class FindResponseDto {

        private String name;  // 카테고리명
        private boolean isDefault;  // 디폴트 여부

        @JsonProperty(value = "isDefault")
        public boolean isDefault() {
            return isDefault;
        }
    }

}
