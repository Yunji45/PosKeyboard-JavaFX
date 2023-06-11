import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AdminDashboard extends Application {

    private boolean isLoggedIn = true; // Misalnya, diinisialisasi sebagai true saat login berhasil

    private static final double ZOOM_FACTOR = 1.2;
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ADMIN");

        // ComboBox untuk menu dropdown
        ComboBox<String> menuDropdown = new ComboBox<>();
        menuDropdown.getItems().addAll("Logout");
        menuDropdown.setPromptText("Menu");

        // Membuat layout HBox untuk menempatkan ComboBox
        HBox hbox = new HBox(menuDropdown);
        // hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));

        // Menambahkan aksi saat pilihan pada ComboBox dipilih
        menuDropdown.setOnAction(event -> {
            String selectedItem = menuDropdown.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (selectedItem.equals("Logout")) {
                    logout(primaryStage);
                }
            }
        });

        // Membuat GridPane untuk form keyboard
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Membuat database manager (sebelumnya harus ada koneksi ke database)
        databaseManager = new DatabaseManager();

        // Membuat PreparedStatement untuk query SELECT
        String selectQuery = "SELECT name, price, stock, description FROM keyboard WHERE id = ?";
        PreparedStatement selectStatement;
        try {
            selectStatement = databaseManager.getConnection().prepareStatement(selectQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }


        // Membuat array jalur gambar
        String[] imagePaths = {
            "assets/keyboard1.png",
            "assets/keyboard2.png",
            "assets/keyboard3.png",
            "assets/keyboard4.png"
        };

        // Membuat 4 form keyboard
        for (int i = 0; i < 4; i++) {
            // Label
            Label nameLabel = new Label("Nama :");
            Label priceLabel = new Label("Price :");
            Label stockLabel = new Label("Stock :");
            Label descriptionLabel = new Label("Description :");

            // TextField
            TextField nameTextField = new TextField();
            nameTextField.setDisable(true); // Harus nonaktif
            TextField priceTextField = new TextField();
            TextField stockTextField = new TextField();
            TextField descriptionTextField = new TextField();

            // Set textAlignment untuk memposisikan label ke tengah
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            priceLabel.setTextAlignment(TextAlignment.CENTER);
            stockLabel.setTextAlignment(TextAlignment.CENTER);
            descriptionLabel.setTextAlignment(TextAlignment.CENTER);

            // Set textAlignment untuk memposisikan textField ke tengah
            nameTextField.setAlignment(Pos.CENTER);
            priceTextField.setAlignment(Pos.CENTER);
            stockTextField.setAlignment(Pos.CENTER);
            descriptionTextField.setAlignment(Pos.CENTER);

            // Membuat HBox untuk setiap pasangan Label dan TextField
            HBox nameBox = new HBox(nameLabel, nameTextField);
            HBox priceBox = new HBox(priceLabel, priceTextField);
            HBox stockBox = new HBox(stockLabel, stockTextField);
            HBox descriptionBox = new HBox(descriptionLabel, descriptionTextField);

            // Mengatur alignment dan spacing untuk HBox
            nameBox.setAlignment(Pos.CENTER);
            nameBox.setSpacing(10);
            priceBox.setAlignment(Pos.CENTER);
            priceBox.setSpacing(10);
            stockBox.setAlignment(Pos.CENTER);
            stockBox.setSpacing(10);
            descriptionBox.setAlignment(Pos.CENTER);
            descriptionBox.setSpacing(10);

            // ImageView
            ImageView imageView = new ImageView();
            // Mengatur sumber gambar sesuai dengan jalur gambar pada iterasi saat ini
            String imagePath = imagePaths[i];
            Image image = new Image(imagePath);
            imageView.setImage(image);
            imageView.setFitWidth(500);
            imageView.setFitHeight(300);

            // Menambahkan event handler saat gambar diklik
            imageView.setOnMouseClicked(event -> showImagePopup(image));

            // Menambahkan event handler saat mouse scroll
            imageView.setOnScroll((ScrollEvent event) -> {
                double deltaY = event.getDeltaY();
                if (deltaY < 0) {
                    imageView.setScaleX(imageView.getScaleX() / ZOOM_FACTOR);
                    imageView.setScaleY(imageView.getScaleY() / ZOOM_FACTOR);
                } else {
                    imageView.setScaleX(imageView.getScaleX() * ZOOM_FACTOR);
                    imageView.setScaleY(imageView.getScaleY() * ZOOM_FACTOR);
                }
            });

            // Menambahkan HBox ke dalam grid pane
            gridPane.add(imageView, 0, i * 2);
            gridPane.add(nameBox, 1, i * 2);
            gridPane.add(priceBox, 2, i * 2);
            gridPane.add(stockBox, 3, i * 2);
            gridPane.add(descriptionBox, 1, i * 2 + 1); // Menempatkan deskripsi di kolom kedua
            GridPane.setColumnSpan(descriptionBox, 1); // Mengatur lebar sel yang ditempati oleh deskripsi
            GridPane.setMargin(nameBox, new Insets(0, 0, 0, 0)); // Mengurangi margin bawah pada nameBox
            GridPane.setMargin(descriptionBox, new Insets(-30, 0, 0, 0)); // Mengurangi margin atas pada descriptionBox

            // Mengatur parameter ID sesuai dengan data yang ingin diambil
            try {
                selectStatement.setInt(1, i + 1); // Menggunakan variabel i yang merupakan iterasi saat ini
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            // Menjalankan query SELECT
            ResultSet resultSet;
            try {
                resultSet = selectStatement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            // Memeriksa apakah ada data yang ditemukan
            try {
                if (resultSet.next()) {
                    // Mengambil nilai kolom dari ResultSet
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    int stock = resultSet.getInt("stock");
                    String description = resultSet.getString("description");

                    // Mengisikan nilai kolom ke dalam TextField
                    nameTextField.setText(name);
                    priceTextField.setText(Double.toString(price));
                    stockTextField.setText(Integer.toString(stock));
                    descriptionTextField.setText(description);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            } finally {
                // Menutup ResultSet
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Tombol Update
            Button updateButton = new Button("Update");

            //final handler
            final int index = i; // Variabel index sebagai final variabel

            // Menambahkan event handler saat tombol Update diklik
            updateButton.setOnAction(event -> {
                String name = nameTextField.getText();
                double price = Double.parseDouble(priceTextField.getText());
                int stock = Integer.parseInt(stockTextField.getText());
                String description = descriptionTextField.getText();

                // Update data ke database
                try {
                    PreparedStatement statement = databaseManager.getConnection().prepareStatement("UPDATE keyboard SET name=?, price=?, stock=?, description=? WHERE id=?");
                    statement.setString(1, name);
                    statement.setDouble(2, price);
                    statement.setInt(3, stock);
                    statement.setString(4, description);
                    statement.setInt(5, index + 1); // Menyesuaikan dengan ID keyboard di database
                    statement.executeUpdate();

                    // Menampilkan pesan sukses
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Update Berhasil");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Data berhasil diupdate.");
                    successAlert.showAndWait();

                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            // Menambahkan tombol Update ke dalam grid pane
            gridPane.add(updateButton, 2, i * 2 + 1);
            GridPane.setColumnSpan(updateButton, 2);
            GridPane.setMargin(updateButton, new Insets(10, 0, 0, 0));

        }

        // Menutup PreparedStatement
        try {
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Membungkus GridPane dengan ScrollPane
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Membuat BorderPane sebagai root layout
        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);

        // Membuat layout HBox untuk ComboBox
        HBox menuBox = new HBox(hbox);
        menuBox.setPadding(new Insets(10));
        
        // Menempatkan layout HBox ComboBox di atas (top) BorderPane
        root.setTop(menuBox);

        // Membuat scene dan menampilkan stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Menampilkan alert
        showWelcomeAlert();
    }

    // Logika logout
    private void logout(Stage primaryStage) {
        // Menghapus sesi pengguna atau mengatur status logout
        isLoggedIn = false;

        // Contoh: Tampilkan dialog informasi dan tutup aplikasi
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Anda telah berhasil logout.");

        alert.setOnHidden(evt -> {
            // Buka halaman login
            App loginWindow = new App();
            Stage loginStage = new Stage();
            loginWindow.start(loginStage);

            primaryStage.close(); // Tutup aplikasi setelah dialog ditutup
        });

        alert.show();
    }

    // Menampilkan Alert selamat datang saat Admin Dashboard terbuka
    private void showWelcomeAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Selamat Datang");
        alert.setHeaderText("Halo, Admin!");
        alert.setContentText("Selamat datang di Dashboard Admin.");

        alert.showAndWait();
    }

    //zoomm
    private void showImagePopup(Image image) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Image Zoom");

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(600);
        imageView.setFitWidth(800);

        StackPane layout = new StackPane(imageView);
        layout.setStyle("-fx-background-color: black");

        Scene scene = new Scene(layout, 800, 600);
        popupStage.setScene(scene);
        popupStage.show();
    }
}
