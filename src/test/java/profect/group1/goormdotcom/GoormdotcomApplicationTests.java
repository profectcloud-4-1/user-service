package profect.group1.goormdotcom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import profect.group1.goormdotcom.common.security.JwtTokenProvider;

@SpringBootTest
@ActiveProfiles("test")
class GoormdotcomApplicationTests {
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

	@Test
	void contextLoads() {
	}

}
