package dyveloper.awesomebucket.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registeredDate;  // 등록일시

    @UpdateTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDate;  // 최종 수정일시

}
