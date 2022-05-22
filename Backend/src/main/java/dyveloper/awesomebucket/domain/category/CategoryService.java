package dyveloper.awesomebucket.domain.category;

import dyveloper.awesomebucket.domain.user.User;
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

}
