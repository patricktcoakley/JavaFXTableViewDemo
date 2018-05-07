import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        ResultSet rs = queryActorNamesAndFilms(databaseConnection.getConnection());

        actorFirstNameColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFirstName()));
        actorLastNameColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getLastName()));
        filmTitleColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTitle()));
        filmReleaseYearColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getReleaseYear()));

        try {
            ObservableList<TableEntry> films = FXCollections.observableArrayList(createFilmsList(rs));
            ObservableList<TableEntry> tableEntries = FXCollections.observableArrayList();
            tableEntries.addAll(films);
            actorTableView.setItems(tableEntries);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private class TableEntry {
        private String firstName;
        private String lastName;
        private String title;
        private String releaseYear;

        public TableEntry(String firstName, String lastName, String title, String releaseYear) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.title = title;
            this.releaseYear = releaseYear;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getTitle() {
            return title;
        }

        public String getReleaseYear() {
            return releaseYear;
        }
    }

    private ArrayList<TableEntry> createFilmsList(ResultSet rs) throws SQLException {
        ArrayList<TableEntry> films = new ArrayList<>();
        rs.beforeFirst();
        while (rs.next()) {
            String firstName = rs.getString("first_name");
            String last_name = rs.getString("last_name");
            String title = rs.getString("title");
            String releaseYear = rs.getString("release_year");
            films.add(new TableEntry(firstName, last_name, title, releaseYear));
        }
        return films;
    }

    private ResultSet queryActorNamesAndFilms(Connection connection) {
        PreparedStatement statement;
        ResultSet rs = null;
        String query =
                "select a.first_name, a.last_name, f.title, f.release_year " +
                "from sakila.film f " +
                "join sakila.film_actor as fa on f.film_id = fa.film_id " +
                "join sakila.actor as a on fa.actor_id = a.actor_id " +
                "order by f.title, a.first_name";
        try {
            statement = connection.prepareStatement(query);
            rs = statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
}
