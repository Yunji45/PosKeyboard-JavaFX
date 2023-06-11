import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
// import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.control.Alert;

public class App extends Application {
    // Komponen GUI
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;

    // Koneksi ke database
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        //komponen GUI
        Label titleLabel = new Label("Jee Keyboard Store");
        titleLabel.setFont(Font.font("Arial",FontWeight.BOLD, 30));
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        usernameField = new TextField();
        passwordField = new PasswordField();
        loginButton = new Button("Login");
        registerButton = new Button("Register");

        // Membuat layout grid untuk field username dan password
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setVgap(5);
        grid.setHgap(5);        
        // Menambahkan label-label dan field-field ke dalam grid
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 0, 2);
        grid.add(registerButton, 0, 3);

        // Mengatur warna latar belakang tombol menjadi hitam
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        registerButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        // Menambahkan jarak antara judul label dengan field username dan password
        GridPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Membuat layout HBox untuk tombol login dan register
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Membuat layout VBox untuk menggabungkan grid dan buttonBox
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(grid, buttonBox);
        vbox.setAlignment(Pos.CENTER);

        // Menambahkan aksi saat tombol login ditekan
        loginButton.setOnAction(e -> login());

        // Menambahkan aksi saat tombol register ditekan
        registerButton.setOnAction(event -> {
            Register register = new Register(); // Membuat objek Register
            Stage registerStage = new Stage(); // Membuat stage baru untuk register
            register.start(registerStage); // Memanggil metode start() dari objek Register
            primaryStage.close(); // Menutup stage login (App.java)
        });
            
        // Membuat scene dan menampilkan stage
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Inisialisasi objek DatabaseManager
        databaseManager = new DatabaseManager();
    }

    @Override
    public void stop() {
        // Menutup koneksi database saat aplikasi berhenti
        databaseManager.closeConnection();
    }

    // Metode untuk memeriksa login
    private void login() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    try {
        Connection connection = databaseManager.getConnection();

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        statement.setString(2, password);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            // Jika login berhasil
            String role = resultSet.getString("role");
            if ("admin".equalsIgnoreCase(role)) {
                System.out.println("Login berhasil sebagai admin");
                showAdminDashboard(); // Panggil metode untuk menampilkan dashboard admin
            } else if ("user".equalsIgnoreCase(role)) {
                System.out.println("Login berhasil sebagai user");
                showUserDashboard();
            } else {
                System.out.println("Login berhasil dengan role: " + role);
            }
        } else {
            // Jika login gagal
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("Login Gagal");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Username atau password salah.");

            errorAlert.setOnHidden(evt -> {
                pesan(); // Panggil metode logout() untuk kembali ke halaman login
            });

            errorAlert.showAndWait();
        }

            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //admin
    private void showAdminDashboard() {
        AdminDashboard adminDashboard = new AdminDashboard(); // Membuat objek AdminDashboard
        Stage dashboardStage = new Stage(); // Membuat stage baru untuk dashboard admin
        adminDashboard.start(dashboardStage); // Memanggil metode start() dari objek AdminDashboard
    
        // Menutup stage login (App.java)
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
    
    //User
    private void showUserDashboard() {
        UserDashboard userDashboard = new UserDashboard(); // membuat objek user
        Stage dashboardStage = new Stage(); // Membuat stage baru untuk dashboard admin
        userDashboard.start(dashboardStage); // Memanggil metode start() dari objek 
    
        // Menutup stage login (App.java)
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    //pesan error ketika username dan password salah
    private void pesan() {
        // Contoh: Tampilkan dialog informasi dan tutup aplikasi
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Silahkan Login dengan Username dan password yang benar.");
        alert.show();
    }

}
