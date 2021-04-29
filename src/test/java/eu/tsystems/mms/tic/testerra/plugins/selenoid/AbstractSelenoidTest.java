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

import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SuiteContext;
import eu.tsystems.mms.tic.testframework.report.model.context.TestContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.testing.TesterraTest;
import eu.tsystems.mms.tic.testframework.useragents.ChromeConfig;
import eu.tsystems.mms.tic.testframework.useragents.FirefoxConfig;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverProxyUtils;
import java.util.Optional;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractSelenoidTest extends TesterraTest {

    @BeforeSuite
    public void configureChromeOptions() {
        Proxy proxy = new WebDriverProxyUtils().getDefaultHttpProxy();

        WebDriverManager.setUserAgentConfig(Browsers.chrome, new ChromeConfig() {
            @Override
            public void configure(ChromeOptions options) {
                options.setProxy(proxy);
            }
        });

        WebDriverManager.setUserAgentConfig(Browsers.firefox, new FirefoxConfig() {
            @Override
            public void configure(FirefoxOptions firefoxOptions) {
                firefoxOptions.setProxy(proxy);
            }
        });

    }

    protected boolean isVideoPresentInMethodContext(String methodName, boolean expectPresence) {
        Optional<MethodContext> optionalMethodContext = ExecutionContextController.getCurrentExecutionContext().readSuiteContexts()
                .flatMap(SuiteContext::readTestContexts)
                .flatMap(TestContext::readClassContexts)
                .filter(classContext -> classContext.getTestClass().equals(this.getClass()))
                .flatMap(ClassContext::readMethodContexts)
                .filter(methodContext -> {
                    Optional<ITestResult> testNgResult = methodContext.getTestNgResult();
                    return (testNgResult.isPresent() && testNgResult.get().getMethod().getMethodName().equals(methodName));
                })
                .findFirst();

        Assert.assertTrue(optionalMethodContext.isPresent(), String.format("MethodContext \"%s\" found", methodName));

        MethodContext methodContext = optionalMethodContext.get();
        Optional<SessionContext> optionalSessionContext = methodContext.readSessionContexts().findFirst();

        Assert.assertTrue(optionalSessionContext.isPresent(), String.format("MethodContext \"%s\" has session context", methodName));

        boolean videoPresent = optionalSessionContext.get().getVideo().isPresent();
        Assert.assertEquals(videoPresent, expectPresence, String.format("SessionContext for MethodContext \"%s\" has video", methodName));

        return expectPresence == videoPresent;
    }
}
