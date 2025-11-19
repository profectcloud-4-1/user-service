package profect.group1.goormdotcom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import profect.group1.goormdotcom.common.security.JwtTokenProvider;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
class GoormdotcomApplicationTests {
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        System.out.println("==== ACTIVE PROFILES ====");
        System.out.println(Arrays.toString(environment.getActiveProfiles()));
        System.out.println("==========================");
    }

}
