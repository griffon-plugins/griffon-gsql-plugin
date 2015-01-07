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

import griffon.core.GriffonApplication;
import griffon.core.injection.Injector;
import griffon.plugins.datasource.DataSourceFactory;
import griffon.plugins.datasource.DataSourceStorage;
import griffon.plugins.gsql.GsqlBootstrap;
import griffon.plugins.gsql.GsqlFactory;
import groovy.sql.Sql;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultGsqlFactory extends AbstractObjectFactory<Sql> implements GsqlFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGsqlFactory.class);

    @Inject
    private DataSourceFactory dataSourceFactory;

    @Inject
    private DataSourceStorage dataSourceStorage;

    @Inject
    private Injector injector;

    @Inject
    public DefaultGsqlFactory(@Nonnull @Named("datasource") griffon.core.Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
    }

    @Nonnull
    @Override
    public Set<String> getDatasourceNames() {
        return dataSourceFactory.getDataSourceNames();
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String datasourceName) {
        return dataSourceFactory.getConfigurationFor(datasourceName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "dataSource";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "dataSources";
    }

    @Nonnull
    @Override
    public Sql create(@Nonnull String name) {
        Map<String, Object> config = getConfigurationFor(name);
        event("GsqlConnectStart", asList(name, config));
        Sql sql = createSql(name);

        for (Object o : injector.getInstances(GsqlBootstrap.class)) {
            ((GsqlBootstrap) o).init(name, sql);
        }
        sql.close();
        sql = createSql(name);

        event("GsqlConnectEnd", asList(name, config, sql));
        return sql;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull Sql instance) {
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = getConfigurationFor(name);
        event("GsqlDisconnectStart", asList(name, config, instance));

        for (Object o : injector.getInstances(GsqlBootstrap.class)) {
            ((GsqlBootstrap) o).destroy(name, instance);
        }
        instance.close();

        closeDataSource(name);

        event("GsqlDisconnectEnd", asList(name, config));
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private Sql createSql(@Nonnull String dataSourceName) {
        return new Sql(getDataSource(dataSourceName));
    }

    private void closeDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource != null) {
            dataSourceFactory.destroy(dataSourceName, dataSource);
            dataSourceStorage.remove(dataSourceName);
        }
    }

    @Nonnull
    private DataSource getDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource == null) {
            dataSource = dataSourceFactory.create(dataSourceName);
            dataSourceStorage.set(dataSourceName, dataSource);
        }
        return dataSource;
    }
}
