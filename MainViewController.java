import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<TableEntry> actorTableView;
    @FXML
    private TableColumn<TableEntry, String> filmTitleColumn;
    @FXML
    private TableColumn<TableEntry, String> filmReleaseYearColumn;
    @FXML
    private TableColumn<TableEntry, String> actorFirstNameColumn;
    @FXML
    private TableColumn<TableEntry, String> actorLastNameColumn;

    private Stage stage;
    private DatabaseConnection databaseConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseConnection = new DatabaseConnection();
        actorFirstNameColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFirstName()));
        actorLastNameColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getLastName()));
        filmTitleColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTitle()));
        filmReleaseYearColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getReleaseYear()));
        initializeTable(databaseConnection.getConnection());
    }

    private class TableEntry {
        private final String firstName;
        private final String lastName;
        private final String title;
        private final String releaseYear;

        TableEntry(String firstName, String lastName, String title, String releaseYear) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.title = title;
            this.releaseYear = releaseYear;
        }

        String getFirstName() {
            return firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getTitle() {
            return title;
        }

        String getReleaseYear() {
            return releaseYear;
        }
    }

    private ArrayList<TableEntry> createFilmsList(ResultSet rs) throws SQLException {
        ArrayList<TableEntry> films = new ArrayList<>();
        rs.beforeFirst();
        while (rs.next()) {
            String firstName = rs.getString("first_name").toUpperCase();
            String last_name = rs.getString("last_name").toUpperCase();
            String title = rs.getString("title").toUpperCase();
            String releaseYear = rs.getString("release_year").substring(0, 4);
            films.add(new TableEntry(firstName, last_name, title, releaseYear));
        }
        return films;
    }

    private ResultSet queryActorNamesAndFilms(Connection connection) {
        Statement statement;
        ResultSet rs = null;
        String query =
                "select a.first_name, a.last_name, f.title, f.release_year " +
                        "from sakila.film f " +
                        "join sakila.film_actor as fa on f.film_id = fa.film_id " +
                        "join sakila.actor as a on fa.actor_id = a.actor_id " +
                        "order by f.title, a.first_name";
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    @FXML
    private boolean addButtonHandler() throws Exception {
        stage = new Stage();
        Pane pane = FXMLLoader.load(getClass().getResource("AddActorView.fxml"));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setOnHiding(event -> initializeTable(databaseConnection.getConnection()));
        return true;
    }

    @FXML
    private boolean editButtonHandler() throws Exception {
        EditActorViewController.currentItem = getCurrentItem();
        stage = new Stage();
        Pane pane = FXMLLoader.load(getClass().getResource("EditActorView.fxml"));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setOnHiding(event -> {
            EditActorViewController.currentItem = null;
            initializeTable(databaseConnection.getConnection());
        });
        return true;
    }

    @FXML
    private boolean deleteButtonHandler() {
        actorTableView.refresh();
        boolean isDeleted = deleteActor(databaseConnection.getConnection());
        initializeTable(databaseConnection.getConnection());
        return isDeleted;
    }

    private boolean deleteActor(Connection connection) {
        PreparedStatement statement;
        int actorId = getCurrentActorId(connection);
        if (actorId < 0) {
            return false;
        }
        String query = "DELETE FROM sakila.film_actor WHERE actor_id = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, actorId);
            actorTableView.refresh();
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getCurrentActorId(Connection connection) {
        TableEntry currentEntry = actorTableView.getSelectionModel().getSelectedItem();
        PreparedStatement statement;
        int actorId = -1;
        ResultSet rs;
        String query = "SELECT actor_id FROM sakila.actor WHERE first_name = ? AND last_name = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, currentEntry.getFirstName());
            statement.setString(2, currentEntry.getLastName());
            rs = statement.executeQuery();
            while (rs.next()) {
                actorId = rs.getInt("actor_id");
            }
            return actorId;
        } catch (SQLException e) {
            e.printStackTrace();
            return actorId;
        }
    }

    private int getCurrentFilmId(Connection connection) {
        TableEntry currentEntry = actorTableView.getSelectionModel().getSelectedItem();
        PreparedStatement statement;
        int filmId = -1;
        ResultSet rs;
        String query = "SELECT film_id FROM sakila.film WHERE title = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, currentEntry.getTitle());
            rs = statement.executeQuery();
            while (rs.next()) {
                filmId = rs.getInt("film_id");
            }
            return filmId;
        } catch (SQLException e) {
            e.printStackTrace();
            return filmId;
        }
    }

    private void initializeTable(Connection connection) {
        ResultSet rs;
        ObservableList<TableEntry> films;
        try {
            rs = queryActorNamesAndFilms(connection);
            films = FXCollections.observableArrayList(createFilmsList(rs));
            actorTableView.setItems(films);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getCurrentItem() {

        TableEntry currentEntry = actorTableView.getSelectionModel().getSelectedItem();
        String firstName = currentEntry.getFirstName();
        String lastName = currentEntry.getLastName();
        String title = currentEntry.getTitle();
        String releaseYear = currentEntry.getReleaseYear();
        int actorId = getCurrentActorId(databaseConnection.getConnection());
        int filmId = getCurrentFilmId(databaseConnection.getConnection());
        return new String[]{firstName, lastName, title, releaseYear, Integer.toString(actorId), Integer.toString(filmId)};

    }
}
