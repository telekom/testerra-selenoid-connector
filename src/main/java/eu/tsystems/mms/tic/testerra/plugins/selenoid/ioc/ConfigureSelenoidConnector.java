package eu.tsystems.mms.tic.testerra.plugins.selenoid.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.hooks.SelenoidVideoHook;
import eu.tsystems.mms.tic.testframework.hooks.ModuleHook;

public class ConfigureSelenoidConnector extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
        Multibinder<ModuleHook> hookBinder = Multibinder.newSetBinder(binder(), ModuleHook.class);
        hookBinder.addBinding().to(SelenoidVideoHook.class).in(Scopes.SINGLETON);
    }
}
