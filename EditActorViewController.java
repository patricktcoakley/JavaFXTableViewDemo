import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditActorViewController implements Initializable {

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
    public static String[] currentItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseConnection = new DatabaseConnection();
        firstNameTextField.setText(currentItem[0]);
        lastNameTextField.setText(currentItem[1]);
        titleTextField.setText(currentItem[2]);
        releaseYearTextField.setText(currentItem[3]);

    }

    public void saveButtonHandler() {
        boolean isActorUpdated = updateActor(databaseConnection.getConnection());
        boolean isFilmUpdated = updateFilm(databaseConnection.getConnection());
        if (isActorUpdated && isFilmUpdated) {
            cancelButtonHandler();
        }
    }

    public void cancelButtonHandler() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean updateActor(Connection connection) {
        PreparedStatement statement;
        String query = "UPDATE sakila.actor SET first_name = ?, last_name = ? WHERE actor_id = ?";
        int actorId = Integer.parseInt(currentItem[4]);

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, firstNameTextField.getText());
            statement.setString(2, lastNameTextField.getText());
            statement.setInt(3, actorId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateFilm(Connection connection) {
        PreparedStatement statement;
        String query = "UPDATE sakila.film SET title = ?, release_year = ? WHERE film_id = ?";
        int filmId = Integer.parseInt(currentItem[5]);
        int releaseYear = Integer.parseInt(releaseYearTextField.getText());
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, titleTextField.getText());
            statement.setInt(2, releaseYear);
            statement.setInt(3, filmId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
