package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BusServer {

    static final String URL = "jdbc:mysql://localhost:3306/musiov";
    static final String USER = "root";
    static final String PASSWORD = "anusiyak";

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081), 0
        );

        server.createContext("/buses", BusServer::getBuses);

        server.start();

        System.out.println("Bus Backend Started!");
        System.out.println("Server running at http://localhost:8081");
    }

   static Connection getConnection() throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(URL, USER, PASSWORD);
}

    static void getBuses(HttpExchange exchange) throws IOException {

        addCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {

            Connection con = getConnection();

            String sql = """
                SELECT id, bus_name, bus_number, bus_type,
                source, destination, contact
                FROM buses """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"name\":\"").append(rs.getString("bus_name")).append("\",")
                    .append("\"busNumber\":\"").append(rs.getString("bus_number")).append("\",")
                    .append("\"type\":\"").append(rs.getString("bus_type")).append("\",")
                    .append("\"source\":\"").append(rs.getString("source")).append("\",")
                    .append("\"destination\":\"").append(rs.getString("destination")).append("\",")
                    .append("\"contact\":\"").append(rs.getString("contact")).append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            con.close();

            sendResponse(exchange, json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, "ERROR");
        }
    }

    static void addCors(HttpExchange exchange) {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }

    static void sendResponse(
            HttpExchange exchange,
            String response
    ) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        OutputStream output = exchange.getResponseBody();

        output.write(bytes);
        output.close();
    }
}