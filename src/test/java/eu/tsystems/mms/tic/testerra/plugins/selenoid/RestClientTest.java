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

package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidRestClient;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Created on 01.07.2021
 *
 * @author mgn
 */
public class RestClientTest extends AbstractSelenoidTest {

    private String selenoidUrl = "";

    @BeforeClass
    public void init() throws MalformedURLException {
        DesktopWebDriverRequest request = new DesktopWebDriverRequest();
        String url = request.getServerUrl().get().toString();
        this.selenoidUrl = url.replace("/wd/hub", "");
        request.setBaseUrl(new URL("https://the-internet.herokuapp.com/"));
        WebDriverManager.getWebDriver(request);
    }

    /**
     * Executes a REST call against a GGR Selenoid instance
     * <p>
     * ! This test is only passed if it executed against a GGR !
     */
    @Test
    public void test_Selenoid_GetHost() {
        String remoteSessionId = ExecutionContextController.getCurrentSessionContext().getRemoteSessionId().get();
        SelenoidRestClient client = new SelenoidRestClient(selenoidUrl);
        Optional<String> response = client.getHost(remoteSessionId);

        Assert.assertTrue(response.isPresent());

        // A GGR response looks like
        // {"Name":"<host-ip>","Port":<port>,"Count":40,"Username":"","Password":"","VNC":"","Scheme":""}
        String ggrJson = response.get();
        Assert.assertTrue(ggrJson.contains("\"Name\":"));
        Assert.assertTrue(ggrJson.contains("\"Port\":"));
    }

}
