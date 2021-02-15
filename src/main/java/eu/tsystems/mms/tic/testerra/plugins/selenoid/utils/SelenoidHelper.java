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
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.transfer.ThrowablePackedResponse;
import eu.tsystems.mms.tic.testframework.utils.FileDownloader;
import eu.tsystems.mms.tic.testframework.utils.RESTUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;

import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import java.util.Optional;
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
     * @return true, when seleniod is active.
     */
    public boolean isSelenoidUsed(SessionContext sessionContext) {
        Optional<NodeInfo> optionalNodeInfo = sessionContext.getNodeInfo();
        if (!optionalNodeInfo.isPresent()) {
            return false;
        }

        NodeInfo nodeInfo = optionalNodeInfo.get();
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
        String selenoidSessionId = getSelenoidSessionId(videoRequest.sessionContext.getRemoteSessionId());
        return videoRequest.sessionContext.getNodeInfo()
                .map(nodeInfo -> VNC_ADDRESS + "?host=" + nodeInfo.getHost() + "&port=" + nodeInfo.getPort() + "&path=vnc/" + selenoidSessionId + "&autoconnect=true&password=selenoid")
                .orElse(null);
    }

    /**
     * Calls /delete for given {@link VideoRequest} to delete remote video.
     *
     * @param videoRequest
     */
    public void deleteRemoteVideoFile(VideoRequest videoRequest) {
        getVideoUrlString(videoRequest).ifPresent(videoUrlString -> {
            try {
                log().info("Delete " + videoUrlString);
                RESTUtils.requestDELETE(videoUrlString);
            } catch (Exception e) {
                log().error("Deleting remote video was not successful", e);
            }
        });
    }

    /**
     * Returns video remote path for {@link VideoRequest}
     *
     * @param videoRequest {@link VideoRequest}
     * @return url
     */
    public Optional<String> getVideoUrlString(VideoRequest videoRequest) {
        return videoRequest.sessionContext.getNodeInfo()
                .map(nodeInfo -> "http://" + nodeInfo.getHost() + ":" + nodeInfo.getPort() + "/video/" + videoRequest.selenoidVideoName);
    }

    /**
     * Downloads the remote video file by using {@link FileDownloader}
     *
     * @param videoRequest {@link VideoRequest}
     * @return absolute path
     */
    public String getRemoteVideoFile(VideoRequest videoRequest) {

        Optional<String> optionalVideoUrlString = getVideoUrlString(videoRequest);
        if (!optionalVideoUrlString.isPresent()) {
            return null;
        }
        final String videoUrlString = optionalVideoUrlString.get();
        final String videoName = "video_" + videoRequest.selenoidVideoName;
        final FileDownloader downloader = new FileDownloader();

        final Timer timer = new Timer(5000, 20_000);
        final ThrowablePackedResponse<String> response = timer.executeSequence(new Timer.Sequence<String>() {
            @Override
            public void run() throws Throwable {
                setSkipThrowingException(true);
                setAddThrowableToMethodContext(false);
                final String download = downloader.download(null, videoUrlString, videoName);
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
     */
    public String getRemoteDownloadPath(SessionContext sessionContext, String filename) {
        String selenoidSessionId = getSelenoidSessionId(sessionContext.getRemoteSessionId());
        return sessionContext.getNodeInfo()
                .map(nodeInfo -> String.format("http://%s:%s/download/%s/%s", nodeInfo.getHost(), nodeInfo.getPort(), selenoidSessionId, filename))
                .orElse(null);
    }

    /**
     * Gets the clipboard value
     */
    public String getClipboard(SessionContext sessionContext) {
        String selenoidSessionId = getSelenoidSessionId(sessionContext.getRemoteSessionId());
        return sessionContext.getNodeInfo()
                .map(nodeInfo -> RESTUtils.requestGET(String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), selenoidSessionId)))
                .orElse(null);
    }

    /**
     * Sets the clipboard value
     */
    public void setClipboard(SessionContext sessionContext, String value) {
        String selenoidSessionId = getSelenoidSessionId(sessionContext.getRemoteSessionId());
        sessionContext.getNodeInfo()
                .ifPresent(nodeInfo -> {
                    log().info("Set session clipboard value: " + value);
                    final String url = String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), selenoidSessionId);
                    RESTUtils.requestPOST(url, value, MediaType.WILDCARD_TYPE, RESTUtils.DEFAULT_TIMEOUT, String.class);
                });
    }

    private String getSelenoidSessionId(String remoteSessionId) {
        if (remoteSessionId.length() >= 64) {
            // its a ggr session id, so cut first 32
            remoteSessionId = remoteSessionId.substring(32);
        }
        return remoteSessionId;
    }
}
