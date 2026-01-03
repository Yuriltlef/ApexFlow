import com.apex.util.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class DBCTest {
    public static void main(String[] args) throws SQLException {
        Connection conn = ConnectionPool.getConnection();
    }
}