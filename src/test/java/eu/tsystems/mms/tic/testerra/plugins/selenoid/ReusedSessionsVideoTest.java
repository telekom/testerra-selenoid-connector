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
import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;
import eu.tsystems.mms.tic.testframework.events.ExecutionFinishEvent;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManagerConfig;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReusedSessionsVideoTest extends AbstractSelenoidTest {

    private WebDriver webDriver;

    @Test()
    public void test_fails_without_closing_webdriver() {
        System.setProperty(TesterraProperties.CLOSE_WINDOWS_AFTER_TEST_METHODS, "false");
        System.setProperty(TesterraProperties.CLOSE_WINDOWS_ON_FAILURE, "false");
        WebDriverManagerConfig config = WebDriverManager.getConfig();
        config.reset();
        this.webDriver = WebDriverManager.getWebDriver();
        this.webDriver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    @Test(dependsOnMethods = "test_fails_without_closing_webdriver", alwaysRun = true)
    public void test_Video_is_present_after_execution() {
        Assert.assertTrue(WebDriverSessionsManager.getSessionContext(this.webDriver).isPresent(), "SessionContext is present");
        WebDriverManagerConfig config = WebDriverManager.getConfig();

        Assert.assertFalse(config.shouldShutdownSessionAfterTestMethod());
        Assert.assertFalse(config.shouldShutdownSessionOnFailure());

        System.setProperty(TesterraProperties.CLOSE_WINDOWS_AFTER_TEST_METHODS, "true");
        System.setProperty(TesterraProperties.CLOSE_WINDOWS_ON_FAILURE, "true");

        config.reset();

        Assert.assertTrue(config.shouldShutdownSessionAfterTestMethod());
        Assert.assertTrue(config.shouldShutdownSessionOnFailure());

        ReusedSessionsVideoTest self = this;

        TesterraListener.getEventBus().register(new ExecutionFinishEvent.Listener() {
            @Override
            @Subscribe
            public void onExecutionFinish(ExecutionFinishEvent event) {
                /**
                 * TODO: Move that into a TestUnderTest
                 */
                self.isVideoPresentInMethodContext("test_fails_without_closing_webdriver", true);
            }
        });
    }
}
