package com.geekbrains.server.db;

import java.sql.*;


public class AuthService {

    Connection connection;

    public AuthService() {
        connection = getConnection();
    }

    private Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:serverInfo.db");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        throw new NullPointerException("Not fount connection.");
    }

    public boolean addUser(String login, int pass) throws Exception{

        String query = "INSERT INTO users (login,password) VALUES (?,?)";
        try {
            if (isExistingUser(login)){
                throw new RuntimeException("Пользователь существует");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login.trim());
            preparedStatement.setInt(2, pass);

            return preparedStatement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException(throwables);
        }
    }

    public boolean isExistingUser(String login) {
        String query = "Select password from users where login = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int passwordHash = resultSet.getInt(1);
                return true;
            }
            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean isCorrectAuthorization(String login, int pass) {
        String query = "Select password from users where login = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, login.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int passwordHash = resultSet.getInt(1);
                return passwordHash == pass;
            }
            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
