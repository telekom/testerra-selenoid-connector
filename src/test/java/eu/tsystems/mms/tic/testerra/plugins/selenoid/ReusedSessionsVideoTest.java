/*
 * Testerra
 *
 * (C) 2021, Mike Reiche,  T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 */

package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.events.ExecutionFinishEvent;
import eu.tsystems.mms.tic.testframework.testing.WebDriverManagerProvider;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReusedSessionsVideoTest extends AbstractSelenoidTest implements WebDriverManagerProvider {

    private WebDriver webDriver;

    private WebDriver getWebDriver() {
        if (this.webDriver == null) {
            DesktopWebDriverRequest desktopWebDriverRequest = new DesktopWebDriverRequest();
            desktopWebDriverRequest.setShutdownAfterTestFailed(false);
            desktopWebDriverRequest.setShutdownAfterTest(false);
            this.webDriver = WEB_DRIVER_MANAGER.getWebDriver(desktopWebDriverRequest);
        }
        return this.webDriver;
    }

    @Test()
    public void test_FailsWithoutClosingWebdriver() {
        WebDriver webDriver = getWebDriver();
        webDriver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    @Test(dependsOnMethods = "test_FailsWithoutClosingWebdriver", alwaysRun = true)
    public void test_VideoIsPresent_after_FailsWithoutClosingWebdriver() {
        WebDriver webDriver = getWebDriver();
        Assert.assertTrue(WEB_DRIVER_MANAGER.getSessionContext(webDriver).isPresent(), "SessionContext is present");

        ReusedSessionsVideoTest self = this;

        Testerra.getEventBus().register(new ExecutionFinishEvent.Listener() {
            @Override
            @Subscribe
            public void onExecutionFinish(ExecutionFinishEvent event) {
                /**
                 * TODO: Move that into a TestUnderTest
                 */
                self.isVideoPresentInMethodContext("test_FailsWithoutClosingWebdriver", true);
            }
        });
    }
}
