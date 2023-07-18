/*
 * Testerra
 *
 * (C) 2023, Martin Großmann, Deutsche Telekom MMS GmbH, Deutsche Telekom AG
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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.utils;

import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.testing.SeleniumChromeDevTools;
import org.openqa.selenium.Credentials;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created on 2023-06-20
 *
 * @author mgn
 */
public class SelenoidChromeDevTools extends SeleniumChromeDevTools {
    @Override
    public DevTools getRawDevTools(WebDriver webDriver) {
        if (!isSupported(webDriver)) {
            throw new RuntimeException("The current browser does not support DevTools");
        }

        if (isRemoteDriver(webDriver)) {
            SessionContext sessionContext = WEB_DRIVER_MANAGER.getSessionContext(webDriver).get();
            if (SelenoidHelper.get().isSelenoidUsed(sessionContext)) {
                this.spoofDevToolsCaps(webDriver, sessionContext);
            } else {
                log().info("Default implementation of BrowserDevTools is used.");
            }
        }
        return super.getRawDevTools(webDriver);
    }

    @Override
    public void setBasicAuthentication(WebDriver webDriver, Supplier<Credentials> credentials) {
        if (!isSupported(webDriver)) {
            throw new RuntimeException("The current browser does not support DevTools");
        }

        if (isRemoteDriver(webDriver)) {
            SessionContext sessionContext = WEB_DRIVER_MANAGER.getSessionContext(webDriver).get();
            if (SelenoidHelper.get().isSelenoidUsed(sessionContext)) {
                this.spoofDevToolsCaps(webDriver, sessionContext);
            } else {
                log().info("Default implementation of BrowserDevTools is used.");
            }
        }
        super.setBasicAuthentication(webDriver, credentials);
    }

    /**
     * Inject caps which are needed for DevTools in a Selenoid Grid
     * Inspired by https://gist.github.com/matthewlowry/69cc0c96e452d667b66adab4abd91db3
     */
    private void spoofDevToolsCaps(WebDriver webDriver, SessionContext sessionContext) {
        Optional<URL> nodeUrl = sessionContext.getNodeUrl();
        if (!nodeUrl.isPresent()) {
            log().error("Cannot inject caps, no node information found.");
            return;
        }

        try {
            RemoteWebDriver remoteWebDriver = WEB_DRIVER_MANAGER.unwrapWebDriver(webDriver, RemoteWebDriver.class).get();
            Field capabilitiesField = RemoteWebDriver.class.getDeclaredField("capabilities");
            capabilitiesField.setAccessible(true);
            String sessionId = SelenoidHelper.get().getSelenoidSessionId(sessionContext.getRemoteSessionId());
            String devtoolsUrl = String.format("ws://%s:%s/devtools/%s/page", nodeUrl.get().getHost(), nodeUrl.get().getPort(), sessionId);

            MutableCapabilities mutableCapabilities = (MutableCapabilities) capabilitiesField.get(remoteWebDriver);
            mutableCapabilities.setCapability("se:cdp", devtoolsUrl);
        } catch (Exception e) {
            log().error("Failed to inject RemoteWebDriver capabilities for Selenoid", e);
        }
    }
}
