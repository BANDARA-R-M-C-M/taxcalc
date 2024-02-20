package com.example.taxcalc;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    String url = "jdbc:sqlserver://my-sqldb-server.database.windows.net:1433;" +
            "database=mysqldb;" +
            "user=sql_admin@my-sqldb-server;" +
            "password=#6316980@DB;" +
            "encrypt=true;" +
            "trustServerCertificate=false;" +
            "hostNameInCertificate=*.database.windows.net;" +
            "loginTimeout=30;";

    String url2 = "jdbc:sqlserver://my-sqldb-server.database.windows.net:1433;encrypt=false;trustServerCertificate=false;loginTimeout=30;database=mysqldb;";
    String user = "sql_admin@my-sqldb-server";//pma
    String password = "#6316980@DB";//""
    double Salary = 0;
    double Tax = 0;
    double EPF = 0;
    double ETF = 0;
    double net_salary = 0;

    public void init() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Salary = Double.parseDouble(request.getParameter("salary"));
        Tax = new taxCalculatorImplent().taxCalculation(Salary);
        EPF = new taxCalculatorImplent().epfCalculation(Salary);
        ETF = new taxCalculatorImplent().etfCalculation(Salary);
        net_salary = Salary - (Tax + EPF + ETF);

        TaxCalculationResult result = new TaxCalculationResult(Salary, Tax, EPF, ETF, net_salary);

        // Convert data object to JSON
        String jsonData = new Gson().toJson(result);

        // Write JSON data to the response
        response.getWriter().write(jsonData);

        String query = "INSERT INTO taxcalculations(salary, tax, epf, etf, net_salary) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url2, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, Salary);
            preparedStatement.setDouble(2, Tax);
            preparedStatement.setDouble(3, EPF);
            preparedStatement.setDouble(4, ETF);
            preparedStatement.setDouble(5, net_salary);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in a production environment
            response.getWriter().println("Error saving salary to the database.");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");

        List<TaxCalculationResult> taxCalcEntries = new ArrayList<>();

        String query = "SELECT salary, tax, epf, etf, net_salary FROM taxcalculations";

        try (Connection connection = DriverManager.getConnection(url2, user, password);
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Salary = resultSet.getDouble("salary");
                Tax = resultSet.getDouble("tax");
                EPF = resultSet.getDouble("epf");
                ETF = resultSet.getDouble("etf");
                net_salary = resultSet.getDouble("net_salary");

                TaxCalculationResult taxCalcEntry = new TaxCalculationResult(Salary, Tax, EPF, ETF, net_salary);
                taxCalcEntries.add(taxCalcEntry);
            }

            request.setAttribute("taxCalcEntries", taxCalcEntries);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/history.jsp");
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in a production environment
            response.getWriter().println("Error retrieving salary from the database.");
        }

    }

    public void destroy() {
    }
}