package eu.tsystems.mms.tic.testerra.plugins.video;

import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.transfer.ThrowablePackedResponse;
import eu.tsystems.mms.tic.testframework.utils.FileDownloader;
import eu.tsystems.mms.tic.testframework.utils.RESTUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;

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
        try {
            RESTUtils.requestDELETE(remoteVideoPath);
        } catch (Exception e) {
            log().warn("Deleting remote video was not successful", e);
        }
    }

    public String getRemoteVideoPath(VideoRequest r) {
        return "http://" + r.webDriverRequest.storedExecutingNode.getHost() + ":" + r.webDriverRequest.storedExecutingNode.getPort() + "/video/" + r.selenoidVideoName;
    }

    public String getRemoveVideoFile(VideoRequest r) {

        if (r.webDriverRequest.storedExecutingNode == null) {
            log().error("Executing Node is not stored. Cannot fetch Selenoid video.");
            return null;
        }

        final String remoteFile = getRemoteVideoPath(r);
        final String videoName = "video_" + r.selenoidVideoName;
        final FileDownloader downloader = new FileDownloader();

        final Timer timer = new Timer(5000, 20_000);
        final ThrowablePackedResponse<String> response = timer.executeSequence(new Timer.Sequence<String>() {
            @Override
            public void run() throws Throwable {
                setSkipThrowingException(true);
                final String download = downloader.download(null, remoteFile, videoName);
                setReturningObject(download);
            }
        });

        if (response.isSuccessful()) {
            return response.getResponse();
        } else {
            return null;
        }
    }
}
