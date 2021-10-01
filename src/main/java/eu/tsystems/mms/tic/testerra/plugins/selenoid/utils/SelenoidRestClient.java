/*
 * Testerra
 *
 * (C) 2021, Martin Großmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package eu.tsystems.mms.tic.testerra.plugins.selenoid.utils;

import eu.tsystems.mms.tic.testframework.logging.Loggable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Created on 01.07.2021
 *
 * @author mgn
 */
public class SelenoidRestClient implements Loggable {

    private final Client client;

    private final String url;

    public SelenoidRestClient(String url) {
        this.url = url;
        this.client = ClientBuilder.newClient();
    }

    public Optional<String> getHost(String remoteSessionid) {
        Response response = this.getBuilder("/host/" + remoteSessionid).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            log().debug("No Selenoid found. (" + response.getStatus() + ")");
            return Optional.empty();
        }
        return Optional.of(response.readEntity(String.class));
    }

    public Optional<String> getPing() {
        Response response = this.getBuilder("/ping").get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            log().debug("No Selenoid found. (" + response.getStatus() + ")");
            return Optional.empty();
        }
        return Optional.of(response.readEntity(String.class));
    }

    public Optional<String> deleteVideofile(String videoFileName) {
        Response response = this.getBuilder("/video/" + videoFileName).delete();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            log().debug("Cannot delete video file " + videoFileName + "(" + response.getStatus() + ")");
            log().debug(response.readEntity(String.class));
            return Optional.empty();
        }
        return Optional.of(response.readEntity(String.class));
    }

    public Optional<String> getClipboard(String remoteSessionId) {
        Response response = this.getBuilder("/clipboard/" + remoteSessionId).get();
        String result = response.readEntity(String.class);
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            log().debug("Cannot read clipboard from session (" + response.getStatus() + ")");
            log().debug(result);
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public void setClipbard(String remoteSessionId, String value) {
        Response response = this.getBuilder("/clipboard/" + remoteSessionId).post(Entity.entity(value, MediaType.TEXT_PLAIN_TYPE));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            log().debug("Cannot set clipboard to session (" + response.getStatus() + ")");
            log().debug(response.readEntity(String.class));
        }
    }

    private Invocation.Builder getBuilder(String path) {
        WebTarget webTarget = client
                .target(this.url)
                .path(path);
        log().debug(webTarget.getUri().toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        return builder;
    }

}
