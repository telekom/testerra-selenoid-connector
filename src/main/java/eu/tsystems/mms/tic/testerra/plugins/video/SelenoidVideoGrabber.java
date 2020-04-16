package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;
import eu.tsystems.mms.tic.testframework.execution.testng.worker.MethodWorker;
import eu.tsystems.mms.tic.testframework.execution.worker.finish.WebDriverSessionHandler;
import eu.tsystems.mms.tic.testframework.interop.VideoCollector;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.report.model.context.report.Report;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.desktop.WebDriverMode;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 15.04.2020
 * Time: 11:16
 *
 * @author Eric Kubenka
 */
public class SelenoidVideoGrabber extends MethodWorker implements WebDriverSessionHandler, VideoCollector, Loggable {

    private static final boolean VIDEO_ACTIVE = PropertyManager.getBooleanProperty(TesterraProperties.SCREENCASTER_ACTIVE, false);

    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();

    private static final ThreadLocal<List<VideoRequest>> closedWebDriverSessions = new ThreadLocal<>();

    /**
     * This method will be called once per failed testcase by {@link eu.tsystems.mms.tic.testframework.execution.worker.finish.AbstractEvidencesWorker}
     * And after all related {@link WebDriver} sessions where closed
     */
    @Override
    public List<Video> getVideos() {

        final List<Video> videoList = new ArrayList<>();

        if (closedWebDriverSessions.get() != null) {
            log().info("Grabbing Selenoid videos.");

            // Iterate over all closed WebDriver sessions during last teardown and get their videos.
            for (final VideoRequest videoRequest : closedWebDriverSessions.get()) {

                if (selenoidHelper.isSelenoidUsed(videoRequest.webDriverRequest)) {

                    final Path tempVideoFile = selenoidHelper.getRemoveVideoFile(videoRequest);

                    if (tempVideoFile != null) {
                        try {
                            final Video video = Report.provideVideo(tempVideoFile.toFile(), Report.Mode.MOVE);
                            videoList.add(video);
                        } catch (IOException e) {
                            log().error("Error providing video to report.", e);
                        }
                    }


                    selenoidHelper.deleteRemoteVideoFile(videoRequest);
                }
            }
        }

        closedWebDriverSessions.remove();
        return videoList;
    }

    /**
     * Remove all closed video requests.
     * Runs after {@link #getVideos()}
     */
    @Override
    public void run() {
        // Already remove all our video requests here, becuase we do not them anymor ein the global list.
        videoRequestStorage.remove(closedWebDriverSessions.get());
        closedWebDriverSessions.remove();
    }

    /**
     * This method will be called for each WebDriver that was closed during a teardown.
     * Probably there is a good chance, that we can append the video file to the method details.
     *
     * @param webDriver @{@link WebDriver}
     */
    @Override
    public void run(WebDriver webDriver) {

        if (closedWebDriverSessions.get() == null) {
            closedWebDriverSessions.set(new LinkedList<>());
        }

        final WebDriverRequest relatedWebDriverRequest = WebDriverManager.getRelatedWebDriverRequest(webDriver);

        if (relatedWebDriverRequest instanceof DesktopWebDriverRequest) {
            final DesktopWebDriverRequest r = (DesktopWebDriverRequest) relatedWebDriverRequest;
            if (r.webDriverMode == WebDriverMode.remote && selenoidHelper.isSelenoidUsed(r)) {
                for (final VideoRequest videoRequest : videoRequestStorage.getList()) {
                    if (videoRequest.webDriverRequest == r) {
                        closedWebDriverSessions.get().add(videoRequest);
                    }
                }
            }
        }
    }
}

