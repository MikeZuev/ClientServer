package server.services.impl;

import server.services.AuthenticationService;

import java.sql.*;

public class SqlAuthenticationServiceImpl implements AuthenticationService {

    private static Connection connection;
    private static Statement stmt;

    public SqlAuthenticationServiceImpl() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {


        try {

            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/DB/users.db");
            stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format("SELECT * from users WHERE login = '%s'", login));

            if(password.equals(rs.getString("password"))) {
                return rs.getString("username");
            }





        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static void connection() {
        try {

            Class.forName("org.sqlite.JDBC");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {

            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/DB/users.db");
            stmt = connection.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }



    private static void findAllUsers () {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * from users");
            while(rs.next()) {
                System.out.printf("%s - %s -%s", rs.getString("login"), rs.getString("password"), rs.getString("username"));
                System.out.println();
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
