package sample;

import com.mobimore.CelebritiesUrl;
import com.mobimore.model.*;
import javafx.application.Platform;

public class SearchByNameThread extends Thread {
    private ISearchByNameCallback callback;
    private Actor actor;

    @Override
    public void run() {
        super.run();
        CelebritiesUrl client = CelebritiesUrl.builder().build();
        GetCelebrityRecentPhotosRequest request = new GetCelebrityRecentPhotosRequest();
        request.setActor(actor);

        try {
            GetCelebrityRecentPhotosResult result = client.getCelebrityRecentPhotos(request);
            if (callback != null) {
                Platform.runLater(() -> callback.onInfoLoaded(result));
            }
        } catch (Exception e) {
            if (callback != null) {
                Platform.runLater(() -> callback.onError(e));
            }
        }
    }

    public void loadInfoAsync(Actor actor, ISearchByNameCallback callback) {
        this.callback = callback;
        this.actor = actor;
        start();
    }

    public interface ISearchByNameCallback {
        void onInfoLoaded(GetCelebrityRecentPhotosResult result);
        void onError(Exception e);
    }
}
