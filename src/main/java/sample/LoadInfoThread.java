package sample;

import com.mobimore.CelebritiesUrl;
import com.mobimore.model.Actor;
import com.mobimore.model.PostCelebrityUrlsRequest;
import com.mobimore.model.PostCelebrityUrlsResult;
import javafx.application.Platform;

public class LoadInfoThread extends Thread {
    private ILoadInfoCallback callback;
    private Actor actor;

    @Override
    public void run() {
        super.run();
        CelebritiesUrl client = CelebritiesUrl.builder().build();
        PostCelebrityUrlsRequest request = new PostCelebrityUrlsRequest();

        request.setActor(actor);
        try {
            PostCelebrityUrlsResult result = client.postCelebrityUrls(request);
            if (callback != null) {
                Platform.runLater(() -> callback.onInfoLoaded(result));
            }
        } catch (Exception e) {
            if (callback != null) {
                Platform.runLater(() -> callback.onError(e));
            }
        }
    }

    public void loadInfoAsync(Actor actor, ILoadInfoCallback callback) {
        this.callback = callback;
        this.actor = actor;
        start();
    }

    public interface ILoadInfoCallback {
        void onInfoLoaded(PostCelebrityUrlsResult result);
        void onError(Exception e);
    }
}
