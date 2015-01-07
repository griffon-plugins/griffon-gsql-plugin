/*
 * Copyright 2014-2015 the original author or authors.
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
 */
package org.codehaus.griffon.runtime.gsql;

import griffon.core.addon.GriffonAddon;
import griffon.core.injection.Module;
import griffon.inject.DependsOn;
import griffon.plugins.gsql.GsqlFactory;
import griffon.plugins.gsql.GsqlHandler;
import griffon.plugins.gsql.GsqlStorage;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;

/**
 * @author Andres Almiray
 */
@DependsOn("datasource")
@Named("gsql")
@ServiceProviderFor(Module.class)
public class GsqlModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(GsqlStorage.class)
            .to(DefaultGsqlStorage.class)
            .asSingleton();

        bind(GsqlFactory.class)
            .to(DefaultGsqlFactory.class)
            .asSingleton();

        bind(GsqlHandler.class)
            .to(DefaultGsqlHandler.class)
            .asSingleton();

        bind(GriffonAddon.class)
            .to(GsqlAddon.class)
            .asSingleton();
        // end::bindings[]
    }
}
