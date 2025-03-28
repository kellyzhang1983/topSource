import org.junit.jupiter.api.Test;

import java.time.Duration;


public class TestOauth {
    @Test
    public void Cover(){
        Duration pt1H = Duration.parse("PT1H");
        System.out.println(pt1H);
        Object b = "PT1H";
        Duration b1 = (Duration) b;
        System.out.println(b1);
    }
}
