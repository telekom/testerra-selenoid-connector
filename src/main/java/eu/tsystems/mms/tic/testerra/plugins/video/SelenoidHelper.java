package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testframework.exceptions.TesterraRuntimeException;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.utils.RESTUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpResponse;
import org.toilelibre.libe.curl.Curl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Methods for communication with selenoid
 * <p>
 * Date: 15.04.2020
 * Time: 11:32
 *
 * @author Eric Kubenka
 */
public class SelenoidHelper implements Loggable {

    private static final SelenoidHelper INSTANCE = new SelenoidHelper();

    private SelenoidHelper() {

    }

    public static SelenoidHelper get() {
        return INSTANCE;
    }

    public boolean isSelenoidUsed(final DesktopWebDriverRequest webDriverRequest) {

        if (webDriverRequest.storedExecutingNode == null) {
            return false;
        }

        final NodeInfo nodeInfo = webDriverRequest.storedExecutingNode;
        try {
            String result = RESTUtils.requestGET(String.format("http://%s:%s/ping", nodeInfo.getHost(), nodeInfo.getPort()));
            if (result != null && result.contains("version")) {
                log().debug("Selenoid ping: " + nodeInfo + ": " + result);
                return true;
            }
        } catch (Exception e) {
            log().debug("Not selenoid.");
        }

        return false;
    }

    public String getRemoteVncUrl(VideoRequest videoRequest) {

        final NodeInfo n = videoRequest.webDriverRequest.storedExecutingNode;
        String sessionId = videoRequest.webDriverRequest.storedSessionId;

        if (sessionId.length() >= 64) {
            // its a ggr session id, so cut first 32
            sessionId = sessionId.substring(32);
        }

        return "http://192.168.45.30:6080/vnc.html?host=" + n.getHost() + "&port=" + n.getPort() + "&path=vnc/" + sessionId + "&autoconnect=true&password=selenoid";
    }

    public void deleteRemoteVideoFile(VideoRequest videoRequest) {
        final String remoteVideoPath = getRemoteVideoPath(videoRequest);
        HttpResponse httpResponse = Curl.curl("-X DELETE " + remoteVideoPath);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            log().warn("Deleting remote video not successful, got: " + httpResponse.getStatusLine());
        }
    }

    public String getRemoteVideoPath(VideoRequest r) {
        return "http://" + r.webDriverRequest.storedExecutingNode.getHost() + ":" + r.webDriverRequest.storedExecutingNode.getPort() + "/video/" + r.selenoidVideoName;
    }

    public Path getRemoveVideoFile(VideoRequest r) {

        if (r.webDriverRequest.storedExecutingNode == null) {
            log().error("Executing Node is not stored. Cannot fetch Selenoid video.");
            return null;
        }

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            final String remoteVideoPath = getRemoteVideoPath(r);
            final Path tempFile = Files.createTempFile("video_", r.selenoidVideoName);

            final Timer timer = new Timer(5000, 1000 * 60 * 5);
            timer.setErrorMessage("Could not download selenoid video");
            timer.executeSequence(new Timer.Sequence<Object>() {
                @Override
                public void run() throws Throwable {

                    log().info("Requesting video for " + r.webDriverRequest.sessionKey + " - " + remoteVideoPath + "...");
                    setPassState(false);
                    HttpResponse httpResponse = Curl.curl(remoteVideoPath);
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        log().info("Video is not ready, yet");
                        log().debug("Video download - got " + httpResponse.getStatusLine());
                    } else {
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile());
                            httpResponse.getEntity().writeTo(fileOutputStream);
                            fileOutputStream.close();
                        } catch (IOException e) {
                            throw new TesterraRuntimeException(e);
                        }

                        setPassState(true);
                    }
                }
            });

            stopWatch.stop();
            log().info("Selenoid video creation took " + stopWatch);
            return tempFile;

        } catch (IOException e) {
            log().error("Error creating temp file for video download", e);
        } finally {
            if (!stopWatch.isStopped()) {
                stopWatch.stop();
            }
        }

        return null;
    }
}
