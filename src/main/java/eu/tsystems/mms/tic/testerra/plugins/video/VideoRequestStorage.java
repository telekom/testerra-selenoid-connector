package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testframework.logging.Loggable;

import java.util.LinkedList;
import java.util.List;

/**
 * Stores and provide access to WebDriver Requests.
 * <p>
 * Date: 15.04.2020
 * Time: 11:30
 *
 * @author Eric Kubenka
 */
public class VideoRequestStorage implements Loggable {

    private final static VideoRequestStorage INSTANCE = new VideoRequestStorage();

    private static final ThreadLocal<List<VideoRequest>> VIDEO_WEBDRIVER_REQUESTS = new ThreadLocal<>();

    private VideoRequestStorage() {

    }

    public static VideoRequestStorage get() {
        return INSTANCE;
    }

    public List<VideoRequest> getList() {
        return VIDEO_WEBDRIVER_REQUESTS.get();
    }

    public void store(VideoRequest request) {

        // creating storage when not created yet.
        if (VIDEO_WEBDRIVER_REQUESTS.get() == null) {
            VIDEO_WEBDRIVER_REQUESTS.set(new LinkedList<>());
        }

        // adding
        VIDEO_WEBDRIVER_REQUESTS.get().add(request);
    }

    public void remove(VideoRequest request) {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().remove(request);
        }
    }

    public void remove(List<VideoRequest> requests) {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().removeAll(requests);
        }
    }

    public void clear() {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().clear();
        }
    }
}
