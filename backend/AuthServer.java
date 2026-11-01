package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
public class AuthServer {

    static final String URL = "jdbc:mysql://localhost:3306/musiov";
    static final String USER = "root";
    static final String PASSWORD = "anusiyak";

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register", AuthServer::register);
        server.createContext("/login", AuthServer::login);

        server.start();

        System.out.println("Musiov Backend Started!");
        System.out.println("Server running at http://localhost:8080");
    }

    static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void register(HttpExchange exchange) throws IOException {

        addCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String data = readBody(exchange);
            Map<String, String> form = parseForm(data);

            String name = form.get("name");
            String email = form.get("email");
            String password = form.get("password");

            Connection con = getConnection();

            String checkSql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement check = con.prepareStatement(checkSql);
            check.setString(1, email);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                sendResponse(exchange, "EXISTS");
                con.close();
                return;
            }

            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();

            con.close();

            sendResponse(exchange, "SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "ERROR");
        }
    }

    static void login(HttpExchange exchange) throws IOException {

        addCors(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String data = readBody(exchange);
            Map<String, String> form = parseForm(data);

            String email = form.get("email");
            String password = form.get("password");

            Connection con = getConnection();

            String sql = "SELECT id, name, email FROM users WHERE email = ? AND password = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                sendResponse(exchange, "SUCCESS");
            } else {
                sendResponse(exchange, "INVALID");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "ERROR");
        }
    }

    static String readBody(HttpExchange exchange) throws IOException {

        InputStream input = exchange.getRequestBody();

        return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    static Map<String, String> parseForm(String data) {

        Map<String, String> map = new HashMap<>();

        if (data == null || data.isEmpty()) {
            return map;
        }

        String[] pairs = data.split("&");

        for (String pair : pairs) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                String key = URLDecoder.decode(
                        parts[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );

                map.put(key, value);
            }
        }

        return map;
    }

    static void addCors(HttpExchange exchange) {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS"
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

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        OutputStream output = exchange.getResponseBody();

        output.write(bytes);
        output.close();
    }
}