package fxdemo;

import com.mybank.domain.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;

public class FXDemo extends Application {

    private Text title;
    private Text details;
    private ComboBox<String> clients;
    private TextArea reportArea;

    @Override
    public void start(Stage primaryStage) {

        loadData();

        BorderPane border = new BorderPane();

        HBox top = addHBox();
        border.setTop(top);

        border.setLeft(addVBox());

        addStackPane(top);

        Scene scene = new Scene(border, 600, 400);

        primaryStage.setTitle("MyBank Clients");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // left panel
    public VBox addVBox() {

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        title = new Text("Client Name");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Line separator = new Line(10, 10, 280, 10);

        details = new Text("Account:\nAcc Type:\nBalance:");
        details.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        reportArea = new TextArea();
        reportArea.setPrefHeight(200);
        reportArea.setPrefWidth(300);
        reportArea.setEditable(false);

        vbox.getChildren().addAll(title, separator, details, reportArea);

        return vbox;
    }

    // top bar
    public HBox addHBox() {

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        ObservableList<String> items = FXCollections.observableArrayList();

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer c = Bank.getCustomer(i);
            items.add(c.getLastName() + ", " + c.getFirstName());
        }

        clients = new ComboBox<>(items);
        clients.setPrefWidth(200);
        clients.setPromptText("Choose client");

        // show btn
        Button buttonShow = new Button("Show");

        buttonShow.setOnAction(e -> {

            int index = clients.getSelectionModel().getSelectedIndex();

            if (index < 0) return;

            Customer c = Bank.getCustomer(index);

            title.setText(c.getLastName() + ", " + c.getFirstName());

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < c.getNumberOfAccounts(); i++) {

                Account a = c.getAccount(i);

                String type = (a instanceof SavingsAccount)
                        ? "Savings"
                        : "Checking";

                sb.append("Account #").append(i)
                        .append("\nType: ").append(type)
                        .append("\nBalance: $").append(a.getBalance())
                        .append("\n\n");
            }

            details.setText(sb.toString());
        });

        // report btn
        Button buttonReport = new Button("Report");

        buttonReport.setOnAction(e -> {

            StringBuilder sb = new StringBuilder();

            sb.append("CUSTOMERS REPORT\n================\n\n");

            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {

                Customer c = Bank.getCustomer(i);

                sb.append("Customer: ")
                        .append(c.getLastName())
                        .append(", ")
                        .append(c.getFirstName())
                        .append("\n");

                for (int j = 0; j < c.getNumberOfAccounts(); j++) {

                    Account a = c.getAccount(j);

                    String type = (a instanceof SavingsAccount)
                            ? "Savings Account"
                            : "Checking Account";

                    sb.append("  ")
                            .append(type)
                            .append(": current balance is ")
                            .append(a.getBalance())
                            .append("\n");
                }

                sb.append("\n");
            }

            reportArea.setText(sb.toString());
        });

        hbox.getChildren().addAll(clients, buttonShow, buttonReport);

        return hbox;
    }

    // help
    public void addStackPane(HBox hb) {

        StackPane stack = new StackPane();

        Rectangle helpIcon = new Rectangle(30, 25);
        helpIcon.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop[]{
                        new Stop(0, Color.web("#4977A3")),
                        new Stop(0.5, Color.web("#B0C6DA")),
                        new Stop(1, Color.web("#9CB6CF"))
                }
        ));

        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        helpText.setFill(Color.WHITE);

        helpText.setOnMouseClicked(e -> showAbout());
        helpIcon.setOnMouseClicked(e -> showAbout());

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0));

        hb.getChildren().add(stack);
        HBox.setHgrow(stack, Priority.ALWAYS);
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("MyBank FX Demo\n Yevhenii Hirich, 34");
        alert.showAndWait();
    }

    // load data
    private void loadData() {

        try (BufferedReader br = new BufferedReader(new FileReader("test.dat"))) {

            String line;
            Customer current = null;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split("\t");

                if (p.length == 3 && !p[0].equals("S") && !p[0].equals("C")) {

                    Bank.addCustomer(p[0], p[1]);
                    current = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);
                }

                else if (p[0].equals("S")) {

                    if (current != null) {
                        current.addAccount(new SavingsAccount(
                                Double.parseDouble(p[1]),
                                Double.parseDouble(p[2])
                        ));
                    }
                }

                else if (p[0].equals("C")) {

                    if (current != null) {
                        current.addAccount(new CheckingAccount(
                                Double.parseDouble(p[1]),
                                Double.parseDouble(p[2])
                        ));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // main
    public static void main(String[] args) {
        launch(args);
    }
}