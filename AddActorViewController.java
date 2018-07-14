import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;

public class AddActorViewController implements Initializable {
    @FXML
    private Button cancelButton;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField releaseYearTextField;

    private DatabaseConnection databaseConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseConnection = new DatabaseConnection();
    }

    private boolean saveActor(String firstName, String lastName, Connection connection) {
        PreparedStatement statement;
        String query = "INSERT INTO sakila.actor(first_name, last_name, last_update)" +
                "VALUES(?, ?, ?)";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            statement.setTimestamp(3, timestamp);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveFilm(String title, int year, Connection connection) {
        PreparedStatement statement;
        String query = "INSERT INTO sakila.film(title, release_year, language_id, last_update)" +
                "VALUES(?, ?, ?, ?)";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setInt(2, year);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            statement.setInt(3, 1);
            statement.setTimestamp(4, timestamp);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveFilmActor(String firstName, String lastName, String title, Connection connection) {
        int actorId = getActorId(firstName, lastName, connection);
        int filmId = getFilmId(title, connection);
        if (actorId < 0 || filmId < 0) {
            System.out.println(actorId);
            System.out.println(filmId);
            return false;
        }
        PreparedStatement statement;
        String query = "INSERT INTO sakila.film_actor(actor_id, film_id, last_update)" +
                "VALUES(?, ?, ?)";
        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, actorId);
            statement.setInt(2, filmId);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            statement.setTimestamp(3, timestamp);

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void cancelButtonHandler() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveButtonHandler() {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String title = titleTextField.getText();
        int year = 1901;
        try {
            year = Integer.parseInt(releaseYearTextField.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        boolean isActorSaved = saveActor(firstName, lastName, databaseConnection.getConnection());
        boolean isFilmSaved = saveFilm(title, year, databaseConnection.getConnection());
        boolean isFilmActorSaved = saveFilmActor(firstName, lastName, title, databaseConnection.getConnection());
        if (isActorSaved && isFilmSaved && isFilmActorSaved) {
            cancelButtonHandler();
        }
    }

    private int getFilmId(String filmTitle, Connection connection) {
        PreparedStatement statement;
        int filmId = -1;
        ResultSet rs;
        String query = "SELECT film_id FROM sakila.film WHERE title = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, filmTitle);
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

    private int getActorId(String firstName, String lastName, Connection connection) {
        PreparedStatement statement;
        int actorId = -1;
        ResultSet rs;
        String query = "SELECT actor_id FROM sakila.actor WHERE first_name = ? AND last_name = ?";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
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
}
