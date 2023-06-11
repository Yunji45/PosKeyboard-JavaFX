import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Register extends Application {
    // Komponen GUI
    private TextField usernameField;
    private PasswordField passwordField;
    private Button registerButton;

    // Objek DatabaseManager
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Register");

        //komponen GUI
        Label titleLabel = new Label("Register");
        titleLabel.setFont(Font.font("Arial",FontWeight.BOLD, 30));
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        usernameField = new TextField();
        passwordField = new PasswordField();
        registerButton = new Button("Submit");

        // Membuat layout grid untuk field username dan password
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setVgap(5);
        grid.setHgap(5);
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(registerButton, 1, 3);

        // Mengatur warna latar belakang tombol menjadi hitam
        registerButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        // Menambahkan jarak antara judul label dengan field username dan password
        GridPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // Menambahkan aksi saat tombol register ditekan
        registerButton.setOnAction(event -> {
            try {
                register();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        // Membuat scene dan menampilkan stage
        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Membuat objek DatabaseManager
        databaseManager = new DatabaseManager();
    }

    @Override
    public void stop() {
        // Menutup koneksi database saat aplikasi berhenti
        databaseManager.closeConnection();
    }

    // Metode untuk mendaftar pengguna
    private void register() throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = "user";

        try {
            // Memanggil metode insertUser dari DatabaseManager
            databaseManager.insertUser(username, password, role);
            //ketika sukses 
            System.out.println("User registered successfully.");

            // Mengarahkan pengguna ke halaman login
            App login = new App(); // Membuat objek Login
            Stage loginStage = new Stage(); // Membuat stage baru untuk login
            login.start(loginStage); // Memanggil metode start() dari objek Login

            // Menutup stage register (Register.java)
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            System.out.println("Failed to register user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
