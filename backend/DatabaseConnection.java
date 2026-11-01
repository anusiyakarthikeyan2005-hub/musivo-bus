package backend;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    static final String URL = "jdbc:mysql://localhost:3306/musiov";
    static final String USER = "root";
    static final String PASSWORD = "anusiyak";

    public static void main(String[] args) {

        try {
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("MySQL Connected Successfully!");

            con.close();

        } catch (Exception e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
    }
}

