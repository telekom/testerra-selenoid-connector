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
package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.pageobjects.UiElementFinder;
import eu.tsystems.mms.tic.testframework.testing.BrowserDevToolsProvider;
import eu.tsystems.mms.tic.testframework.testing.UiElementFinderFactoryProvider;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.openqa.selenium.By;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * Created on 2023-06-21
 *
 * @author mgn
 */
public class SelenoidDevToolsTest extends AbstractSelenoidTest implements UiElementFinderFactoryProvider, BrowserDevToolsProvider {

    @Test
    public void testT01_GeoLocation() {
        final double latitude = 52.52084;
        final double longitude = 13.40943;

        DesktopWebDriverRequest request = new DesktopWebDriverRequest();
        WebDriver webDriver = WEB_DRIVER_MANAGER.getWebDriver(request);
        UiElementFinder uiElementFinder = UI_ELEMENT_FINDER_FACTORY.create(webDriver);

        BROWSER_DEV_TOOLS.setGeoLocation(webDriver, latitude, longitude, 1);

        webDriver.get("https://my-location.org/");
        uiElementFinder.find(By.id("latitude")).assertThat().text().isContaining(String.valueOf(latitude));
        uiElementFinder.find(By.id("longitude")).assertThat().text().isContaining(String.valueOf(longitude));
    }

    @Test
    public void testT02_BasicAuth() {
        DesktopWebDriverRequest request = new DesktopWebDriverRequest();

        WebDriver webDriver = WEB_DRIVER_MANAGER.getWebDriver(request);
        UiElementFinder uiElementFinder = UI_ELEMENT_FINDER_FACTORY.create(webDriver);

        BROWSER_DEV_TOOLS.setBasicAuthentication(webDriver, UsernameAndPassword.of("admin", "admin"));

        webDriver.get("https://the-internet.herokuapp.com/basic_auth");
        uiElementFinder.find(By.tagName("p")).assertThat().text().isContaining("Congratulations");

    }

}
