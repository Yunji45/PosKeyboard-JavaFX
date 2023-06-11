import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;

public class UserDashboard extends Application {

    private ObservableList<Item> items;
    private ObservableList<CartItem> cartItems;
    private ListView<Item> itemListView;
    private ListView<CartItem> cartListView;
    private DatabaseManager databaseManager;
    private static final double ZOOM_FACTOR = 1.2;
    private boolean isLoggedIn = true; // Misalnya, diinisialisasi sebagai true saat login berhasil


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Dashboard");

        ComboBox<String> menuDropdown = new ComboBox<>();
        menuDropdown.getItems().addAll("Logout");
        menuDropdown.setPromptText("Menu");

        HBox hbox = new HBox(menuDropdown);
        hbox.setPadding(new Insets(10));

        menuDropdown.setOnAction(event -> {
            String selectedItem = menuDropdown.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (selectedItem.equals("Logout")) {
                    logout(primaryStage);
                }
            }
        });

        // Membuat array jalur gambar
        String[] imagePaths = {
                "assets/keyboard1.png",
                "assets/keyboard2.png",
                "assets/keyboard3.png",
                "assets/keyboard4.png"
        };

        databaseManager = new DatabaseManager();
        items = fetchItemsFromDatabase();
        cartItems = FXCollections.observableArrayList();

        itemListView = new ListView<>(items);
        itemListView.setPrefHeight(800);
        itemListView.setPrefWidth(700);
        itemListView.setCellFactory(param -> new ListCell<Item>() {
            private final VBox vbox = new VBox();
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final Label priceLabel = new Label();
            private final Button addToCartButton = new Button("Add to Cart");

            {
                vbox.getChildren().addAll(nameLabel, priceLabel, addToCartButton);
                vbox.setSpacing(5);
                addToCartButton.setOnAction(event -> {
                    Item item = getItem();
                    TextInputDialog dialog = new TextInputDialog("1");
                    dialog.setTitle("Quantity");
                    dialog.setHeaderText("Enter the quantity for " + item.getName());
                    dialog.setContentText("Quantity:");
                    dialog.showAndWait().ifPresent(quantity -> {
                        int qty = Integer.parseInt(quantity);
                        CartItem cartItem = new CartItem(item.getName(), qty);
                        if (!cartItems.contains(cartItem)) {
                            cartItems.add(cartItem);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String imagePath = imagePaths[getIndex()];
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

                    nameLabel.setText(item.getName());
                    priceLabel.setText("Rp." + item.getPrice());
                    //emanmpilkan image
                    vbox.getChildren().setAll(imageView, nameLabel, priceLabel, addToCartButton);
                    setGraphic(vbox);
                }
            }
        });

        cartListView = new ListView<>(cartItems);
        cartListView.setCellFactory(param -> new ListCell<CartItem>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (Qty: " + item.getQuantity() + ")");
                }
            }
        });

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> cartItems.clear());
        Button buyButton = new Button("Buy");
        buyButton.setOnAction(event -> {
            // Proses pembayaran
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Purchase successful!");
            alert.showAndWait();
            cartItems.clear();
        });

        HBox buttonBox = new HBox(clearButton, buyButton);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));

        VBox cartBox = new VBox(cartListView, buttonBox);
        cartBox.setSpacing(10);
        cartBox.setPadding(new Insets(10));
        cartBox.setPrefWidth(300); // Perbesar lebar VBox

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(itemListView);
        borderPane.setRight(cartBox);

        VBox root = new VBox(hbox, borderPane);
        root.setSpacing(0.7);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        //menampilkan popup
        showVideoPopup();
    }

    private ObservableList<Item> fetchItemsFromDatabase() {
        ObservableList<Item> itemList = FXCollections.observableArrayList();

        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT name, price FROM keyboard";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    itemList.add(new Item(name, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itemList;
    }

    private static class Item {
        private String name;
        private double price;

        public Item(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

    }

    private static class CartItem {
        private String name;
        private int quantity;

        public CartItem(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CartItem cartItem = (CartItem) obj;
            return name.equals(cartItem.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

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

        alert.show();    }

    private void showVideoPopup() {
        // Membuat media player dan media view untuk video
        Media media = new Media(getClass().getResource("assets/keyboard.mp4").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        // Membuat tampilan grup dan menambahkan media view
        Group root = new Group(mediaView);

        // Membuat scene dan menambahkan grup ke dalamnya
        Scene scene = new Scene(root);

        // Membuat stage untuk popup video
        Stage videoPopup = new Stage();
        videoPopup.setTitle("Video Iklan");
        videoPopup.setScene(scene);

        // Mengatur modality menjadi APPLICATION_MODAL untuk memblokir interaksi dengan jendela lain
        videoPopup.initModality(Modality.APPLICATION_MODAL);

        // Mengatur lebar dan tinggi jendela
        videoPopup.setWidth(800);
        videoPopup.setHeight(600);

        // Membuat tombol "Continue" untuk menutup popup video
        Button continueButton = new Button("Continue >>>");
        continueButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        continueButton.setOnAction(event -> videoPopup.close());
        VBox buttonBox = new VBox(continueButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));

        // Menambahkan tombol "Continue" di bawah media view
        root.getChildren().add(buttonBox);

        // Memutar video saat jendela video ditampilkan
        mediaPlayer.setAutoPlay(true);

        // Menampilkan jendela video
        videoPopup.showAndWait();

        // Mematikan media player setelah jendela video ditutup
        mediaPlayer.stop();
    }

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
