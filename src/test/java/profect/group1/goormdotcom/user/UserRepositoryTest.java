package profect.group1.goormdotcom.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import profect.group1.goormdotcom.user.repository.UserRepository;
import profect.group1.goormdotcom.user.repository.entity.UserEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자를 찾을 수 있다.")
    void testFindByEmail_whenUserExists_returnsUser() {
        // given
        UserEntity user = userEntity();
        userRepository.save(user);

        // when
        Optional<UserEntity> foundUser = userRepository.findByEmail("testuser@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자를 찾을 수 없다.")
    void testFindByEmail_whenUserDoesNotExist_returnsEmpty() {
        // when
        Optional<UserEntity> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("사용자를 저장할 수 있다.")
    void testSave_returnsSavedUser() {
        // given
        UserEntity user = userEntity();

        // when
        UserEntity savedUser = userRepository.save(user);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
    }

    private UserEntity userEntity() {
        UserEntity user = UserEntity.builder()
                .email("testuser@example.com")
                .password("testPassword!")
                .name("testuser")
                .build();

        return user;
    }
}