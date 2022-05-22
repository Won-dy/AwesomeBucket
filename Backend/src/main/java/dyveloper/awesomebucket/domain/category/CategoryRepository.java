package dyveloper.awesomebucket.domain.category;

import dyveloper.awesomebucket.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserOrIsDefault(User user, boolean isDefault);

    Optional<Category> findByUserAndName(User user, String name);

    Optional<Category> findByName(String name);

}
