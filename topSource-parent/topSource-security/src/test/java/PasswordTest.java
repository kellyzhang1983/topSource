import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class PasswordTest {
    @Test
    public void TestPasswordEncoder(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode_1 = passwordEncoder.encode("123456");
        String encode_2 = passwordEncoder.encode("kelly");
        String encode_3 = passwordEncoder.encode("kelly19831017");
        System.out.println(encode_1);
        System.out.println(encode_2);
        System.out.println(encode_3);
    }
}
