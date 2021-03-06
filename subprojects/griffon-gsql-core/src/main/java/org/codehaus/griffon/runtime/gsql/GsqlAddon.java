/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.gsql;

import griffon.annotations.core.Nonnull;
import griffon.annotations.inject.DependsOn;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.core.events.StartupStartEvent;
import griffon.plugins.gsql.GsqlCallback;
import griffon.plugins.gsql.GsqlFactory;
import griffon.plugins.gsql.GsqlHandler;
import griffon.plugins.gsql.GsqlStorage;
import griffon.plugins.monitor.MBeanManager;
import groovy.sql.Sql;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.gsql.monitor.GsqlStorageMonitor;

import javax.application.event.EventHandler;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
@DependsOn("datasource")
@Named("gsql")
public class GsqlAddon extends AbstractGriffonAddon {
    @Inject
    private GsqlHandler gsqlHandler;

    @Inject
    private GsqlFactory gsqlFactory;

    @Inject
    private GsqlStorage gsqlStorage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new GsqlStorageMonitor(metadata, gsqlStorage));
    }

    @EventHandler
    public void handleStartupStartEvent(@Nonnull StartupStartEvent event) {
        for (String dataSourceName : gsqlFactory.getDatasourceNames()) {
            Map<String, Object> config = gsqlFactory.getConfigurationFor(dataSourceName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                gsqlHandler.withSql(dataSourceName, new GsqlCallback<Void>() {
                    @Override
                    public Void handle(@Nonnull String dataSourceName, @Nonnull Sql sql) {
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void onShutdown(@Nonnull GriffonApplication application) {
        for (String dataSourceName : gsqlFactory.getDatasourceNames()) {
            gsqlHandler.closeSql(dataSourceName);
        }
    }
}
