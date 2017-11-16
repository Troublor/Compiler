
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class VisualCompilerController implements Initializable {

    private String path;
    @FXML
    private AnchorPane root;

    @FXML
    private TextField file_path_textField;

    @FXML
    private TextArea output_textArea;

    @FXML
    private CheckBox debug_checkBox;

    public void appendText(String valueOf) {
        Platform.runLater(() -> output_textArea.appendText(valueOf));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendText(String.valueOf((char)b));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    @FXML
    public void Compile(ActionEvent event) {
        path=output_textArea.getText();
        output_textArea.clear();
        if (debug_checkBox.isSelected()) {
            Compile.main(new String[]{"-d", "C:\\JavaProject\\Compiler\\src\\input.txt"});
        } else {
            Compile.main(new String[]{path});
        }
    }

    @FXML
    public void ChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) {
            return;
        }
        path = file.getAbsolutePath();
        file_path_textField.setText(path);
    }
}
