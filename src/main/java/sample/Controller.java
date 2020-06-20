package sample;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.mobimore.model.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.List;

public class Controller {
    @FXML
    public ProgressIndicator progressBar;

    @FXML
    public Button loadImgButton;

    @FXML
    public TextArea textArea;

    @FXML
    public Label celebritySearchedTimesLabel;

    @FXML
    public ImageView imageFoundByName;

    @FXML
    public TextField celebrityNameField;

    @FXML
    public Button findBtn;

    @FXML
    private ImageView img;

    @FXML
    private ProgressIndicator progressBar1;

    public void onBtnPress(ActionEvent actionEvent) {
        Stage stage;
        stage = (Stage) img.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            setImageFromFile(file);
            getData(file);
        }
    }

    private void getData(File file) {
        try {
            textArea.setText("");
            byte[] fileContent = FileUtils.readFileToByteArray(file);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            Actor actor = new Actor();
            ImageBase64 imageBase64 = new ImageBase64();
            imageBase64.setData(encodedString);
            actor.setImageBase64(imageBase64);

            setFirstTabDisabled(true);

            new LoadInfoThread().loadInfoAsync(actor, new LoadInfoThread.ILoadInfoCallback() {
                @Override
                public void onInfoLoaded(PostCelebrityUrlsResult result) {
                    StringBuilder stringBuilder = new StringBuilder();
                    List<ActorInfo> actorsInfoList = result.getActorsInfo();
                    for (int i = 0; i < actorsInfoList.size(); i++) {
                        ActorInfo actorInfo = actorsInfoList.get(i);
                        stringBuilder.append(i + 1).append(". ");
                        stringBuilder.append(actorInfoToString(actorInfo))
                                .append("\n");
                    }
                    textArea.setText(stringBuilder.toString());
                    setFirstTabDisabled(false);
                }

                @Override
                public void onError(Exception e) {
                    textArea.setText(e.getMessage());
                    setFirstTabDisabled(false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setImageFromFile(File file) {
        try {
            Image image = new Image(new FileInputStream(file));
            img.setImage(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String actorInfoToString(ActorInfo actorInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(actorInfo.getName())
                .append("\n")
                .append("URLs:")
                .append("\n");
        List<String> urls = actorInfo.getUrls();
        for (int i = 0; i < urls.size(); i++) {
            String st = urls.get(i);
            stringBuilder.append(st);
            if (i != urls.size() - 1) {
                stringBuilder.append("\n");

            }
        }
        return stringBuilder.toString();
    }

    private void setFirstTabDisabled(boolean disabled) {
        loadImgButton.setDisable(disabled);
        findBtn.setDisable(disabled);
        celebrityNameField.setDisable(disabled);
        progressBar.setVisible(disabled);
        progressBar1.setVisible(disabled);
    }

    public void onFindPressed(ActionEvent actionEvent) {
        if (StringUtils.isNullOrEmpty(celebrityNameField.getText())) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setContentText("Celebrity name can't be empty");
            a.showAndWait();
            return;
        }

        imageFoundByName.setImage(null);
        celebritySearchedTimesLabel.setText("");

        Actor actor = new Actor();
        actor.setName(celebrityNameField.getText());

        setFirstTabDisabled(true);
        new SearchByNameThread().loadInfoAsync(actor, new SearchByNameThread.ISearchByNameCallback() {
            @Override
            public void onInfoLoaded(GetCelebrityRecentPhotosResult result) {
                ActorStatistics statistics = result.getActorStatistics();
                List<ImageBase64> imagesBase64 = statistics.getImageBase64();
                if (!CollectionUtils.isNullOrEmpty(imagesBase64)) {
                    ImageBase64 imageBase64 = imagesBase64.get(0);
                    byte[] imageBytes = Base64.getDecoder().decode(imageBase64.getData());
                    Image image = new Image((new ByteArrayInputStream(imageBytes)));
                    imageFoundByName.setImage(image);
                }
                celebritySearchedTimesLabel.setText("Celebrity " + actor.getName() + " was searched " + statistics.getSearchCount().toString() + " times");
                setFirstTabDisabled(false);
            }

            @Override
            public void onError(Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setContentText("No information found for celebrity: " + actor.getName());
                a.showAndWait();
                setFirstTabDisabled(false);
            }
        });
    }
}
