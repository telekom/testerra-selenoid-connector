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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.transfer.ThrowablePackedResponse;
import eu.tsystems.mms.tic.testframework.utils.FileDownloader;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Updates the selenoid node info
     *
     * @return TRUE if this node is a Selenoid node, otherwise FALSE
     */
    public boolean updateNodeInfo(URL seleniumUrl, String remoteSessionId, SessionContext sessionContext) {
        String url = seleniumUrl.toString();

        url = url.replace("/wd/hub", "");

        /**
         * See https://aerokube.com/ggr/latest/#_getting_host_by_session_id for getting Selenoid node information via Selenoid GGR
         */
        try {
            SelenoidRestClient client = new SelenoidRestClient(url);
            Optional<String> response = client.getHost(remoteSessionId);

            if (!response.isPresent()) {
                throw new Exception("There was no response from Selenium server.");
            }

            String nodeResponse = response.get();

            // A standalone Selenoid returns something like 'You are using Selenoid 1.10.1!'
            if (StringUtils.isNotBlank(nodeResponse) && nodeResponse.toLowerCase().contains("selenoid")) {
                return true;
            }

            // A GGR with Selenoid returns a valid JSON response
            Gson gson = new GsonBuilder().create();
            Map map = gson.fromJson(nodeResponse, Map.class);
            double port = Double.parseDouble(map.get("Port").toString());
            sessionContext.setNodeInfo(new NodeInfo(map.get("Name").toString(), (int) port));
            return true;

        } catch (Exception e) {
            log().warn("It seems you are not using Selenoid - could not get node info: " + e.getMessage());
            return false;
        }
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
        Optional<String> url = getSelenoidUrl(sessionContext.getNodeInfo());
        if (!url.isPresent()) {
            return false;
        }

        SelenoidRestClient client = new SelenoidRestClient(url.get());
        Optional<String> response = client.getPing();
        if (!response.isPresent()) {
            log().warn("Empty response from " + url.get());
            return false;
        }

        try {
            String ping = response.get();
            Gson gson = new Gson();
            Map result = gson.fromJson(ping, Map.class);
            if (result != null && result.containsKey("version")) {
                log().debug("Selenoid ping from " + url.get() + ": " + result);
                return true;
            } else {
                log().warn("No Selenoid response from " + url.get());
            }
        } catch (JsonSyntaxException e) {
            log().error("Error pinging selenoid", e);
        }

        return false;
    }

    /**
     * Returns concrete VNC streaming url for the given {@link VideoRequest} by using the value of "tt.selenoid.vnc.host.address"
     *
     * @param videoRequest {@link VideoRequest }
     * @return String
     */
    public String getRemoteVncUrl(VideoRequest videoRequest, String sessionId) {
        String selenoidSessionId = getSelenoidSessionId(Optional.ofNullable(sessionId));
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
        Optional<String> url = getSelenoidUrl(videoRequest.sessionContext.getNodeInfo());
        if (!url.isPresent()) {
            log().error("Cannot delete Selenoid video because there is no host.");
        }
        SelenoidRestClient client = new SelenoidRestClient(url.get());
        Optional<String> response = client.deleteVideofile(videoRequest.selenoidVideoName);
        if (!response.isPresent()) {
            log().error("Deleting remote video was not successful.");
        }
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
        Optional<String> url = getSelenoidUrl(sessionContext.getNodeInfo());
        if (!url.isPresent()) {
            log().error("Cannot read clipboard because there is no host.");
        }
        SelenoidRestClient client = new SelenoidRestClient(url.get());
        Optional<String> clipboard = client.getClipboard(selenoidSessionId);
        return clipboard.orElse(null);
//        return sessionContext.getNodeInfo()
//                .map(nodeInfo -> {
//                    final String url = String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), selenoidSessionId);
//                    try (CloseableHttpClient client = HttpClients.createDefault()) {
//                        HttpGet request = new HttpGet(url);
//                        CloseableHttpResponse response = client.execute(request);
//                        return IOUtils.toString(response.getEntity().getContent(), response.getEntity().getContentEncoding().getValue());
//                    } catch (IOException e) {
//                        log().error("Error getting clipboard value", e);
//                        return null;
//                    }
//                })
//                .orElse(null);
    }

    /**
     * Sets the clipboard value
     */
    public void setClipboard(SessionContext sessionContext, String value) {
        String selenoidSessionId = getSelenoidSessionId(sessionContext.getRemoteSessionId());
        sessionContext.getNodeInfo()
                .ifPresent(nodeInfo -> {
                    final String url = String.format("http://%s:%s/clipboard/%s", nodeInfo.getHost(), nodeInfo.getPort(), selenoidSessionId);
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost request = new HttpPost(url);
                        request.setEntity(new StringEntity(value));
                        CloseableHttpResponse response = client.execute(request);
                        log().info("Set clipboard value with status code: " + response.getStatusLine().getStatusCode());
                    } catch (IOException e) {
                        log().error("Error setting clipboard value", e);
                    }
                });
    }

    private Optional<String> getSelenoidUrl(Optional<NodeInfo> nodeInfo) {
        if (nodeInfo.isPresent()) {
            return Optional.of(String.format("http://%s:%s/", nodeInfo.get().getHost(), nodeInfo.get().getPort()));
        }
        return Optional.empty();
    }

    private String getSelenoidSessionId(Optional<String> optionalremoteSessionId) {
        return optionalremoteSessionId.map(remoteSessionId -> {
            if (remoteSessionId.length() >= 64) {
                // It's a GGR session id, so cut the first 32 chars
                remoteSessionId = remoteSessionId.substring(32);
            }
            return remoteSessionId;
        }).orElse(null);
    }
}
