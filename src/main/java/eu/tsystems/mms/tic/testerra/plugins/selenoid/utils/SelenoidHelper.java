/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Eric Kubenka <Eric.Kubenka@t-systems.com>
 */
package eu.tsystems.mms.tic.testerra.plugins.selenoid.utils;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.transfer.ThrowablePackedResponse;
import eu.tsystems.mms.tic.testframework.utils.FileDownloader;
import eu.tsystems.mms.tic.testframework.utils.RESTUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

/**
 * Methods for communication with Selenoid API
 * <p>
 * Date: 15.04.2020
 * Time: 11:32
 *
 * @author Eric Kubenka
 */
public class SelenoidHelper implements Loggable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelenoidHelper.class);

    private static final String VNC_ADDRESS = PropertyManager.getProperty(SelenoidProperties.VNC_ADDRESS, SelenoidProperties.Default.VNC_ADDRESS);

    private static final SelenoidHelper INSTANCE = new SelenoidHelper();

    private SelenoidHelper() {

    }

    public static SelenoidHelper get() {
        return INSTANCE;
    }

    /**
     * Detemines if Selenoid is used by checking the /ping address of executing node.
     *
     * @param webDriverRequest {@link DesktopWebDriverRequest}
     * @return true, when seleniod is active.
     */
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

    /**
     * Returns concrete VNC streaming url for the given {@link VideoRequest} by using the value of "tt.selenoid.vnc.host.address"
     *
     * @param videoRequest {@link VideoRequest }
     * @return String
     */
    public String getRemoteVncUrl(VideoRequest videoRequest) {

        final NodeInfo n = videoRequest.webDriverRequest.storedExecutingNode;
        String sessionId = getSelenoidSessionId(videoRequest.webDriverRequest);

        return VNC_ADDRESS + "?host=" + n.getHost() + "&port=" + n.getPort() + "&path=vnc/" + sessionId + "&autoconnect=true&password=selenoid";
    }

    /**
     * Calls /delete for given {@link VideoRequest} to delete remote video.
     *
     * @param videoRequest
     */
    public void deleteRemoteVideoFile(VideoRequest videoRequest) {
        final String remoteVideoPath = getRemoteVideoPath(videoRequest);
        try {
            RESTUtils.requestDELETE(remoteVideoPath);
        } catch (Exception e) {
            log().debug("Deleting remote video was not successful", e);
        }
    }

    /**
     * Returns video remote path for {@link VideoRequest}
     *
     * @param r {@link VideoRequest}
     * @return url
     */
    public String getRemoteVideoPath(VideoRequest r) {
        return "http://" + r.webDriverRequest.storedExecutingNode.getHost() + ":" + r.webDriverRequest.storedExecutingNode.getPort() + "/video/" + r.selenoidVideoName;
    }

    /**
     * Downloads the remote video file by using {@link FileDownloader}
     *
     * @param r {@link VideoRequest}
     * @return absolute path
     */
    public String getRemoteVideoFile(VideoRequest r) {

        if (r.webDriverRequest.storedExecutingNode == null) {
            log().debug("Executing Node is not stored. Cannot fetch Selenoid video.");
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

    /**
     * Returns the complete path for downloading files from a Selenoid browser container
     *
     * @param webDriverRequest {@link DesktopWebDriverRequest}
     * @param filename
     * @return absolute path
     */
    public String getRemoteDownloadPath(DesktopWebDriverRequest webDriverRequest, String filename) {
        NodeInfo nodeInfo = webDriverRequest.storedExecutingNode;
        String sessionId = getSelenoidSessionId(webDriverRequest);
        return String.format("http://%s:%s/download/%s/%s", nodeInfo.getHost(), nodeInfo.getPort(), sessionId, filename);
    }

    /**
     * Gets the clipboard value
     *
     * @param webDriverRequest {@link DesktopWebDriverRequest}
     *
     * @return clipboard value
     */
    public String getClipboard(DesktopWebDriverRequest webDriverRequest) {
        NodeInfo nodeInfo = webDriverRequest.storedExecutingNode;
        String sessionId = getSelenoidSessionId(webDriverRequest);
        LOGGER.info("Get session clipboard value");
        return RESTUtils.requestGET(String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), sessionId));
    }

    /**
     * Sets the clipboard value
     *
     * @param webDriverRequest {@link DesktopWebDriverRequest}
     * @param value value to set
     */
    public void setClipboard(DesktopWebDriverRequest webDriverRequest, String value) {
        NodeInfo nodeInfo = webDriverRequest.storedExecutingNode;
        String sessionId = getSelenoidSessionId(webDriverRequest);
        LOGGER.info("Set session clipboard value: " + value);
        final String url = String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), sessionId);
        RESTUtils.requestPOST(url, value, MediaType.WILDCARD_TYPE, RESTUtils.DEFAULT_TIMEOUT, String.class);
    }

    private String getSelenoidSessionId(DesktopWebDriverRequest webDriverRequest) {
        String sessionId = webDriverRequest.storedSessionId;
        if (sessionId.length() >= 64) {
            // its a ggr session id, so cut first 32
            sessionId = sessionId.substring(32);
        }
        return sessionId;
    }
}
