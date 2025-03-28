import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class TestTransaction {

    private static final Logger logger = LoggerFactory.getLogger(TestTransaction.class);
    @GlobalTransactional
    @Transactional
    @Test
    public void testTran(){
        try {
            // 获取全局事务XID
            String xid = RootContext.getXID();
            logger.info("Begin global transaction, XID: {}", xid);
            // 这里可以添加具体的业务逻辑操作，比如数据库操作等
            // 为了简单测试，我们只打印日志
            logger.info("Executing business logic in global transaction");
        } catch (Exception e) {
            logger.error("Error occurred during global transaction test: {}", e.getMessage(), e);
            throw e;
        }
    }
}
