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

import griffon.plugins.gsql.GsqlCallback;
import griffon.plugins.gsql.GsqlFactory;
import griffon.plugins.gsql.GsqlHandler;
import griffon.plugins.gsql.GsqlStorage;
import griffon.plugins.gsql.exceptions.RuntimeGsqlException;
import groovy.sql.Sql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import griffon.annotations.core.Nonnull;
import griffon.annotations.core.Nullable;
import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultGsqlHandler implements GsqlHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGsqlHandler.class);
    private static final String ERROR_DATASOURCE_NAME_BLANK = "Argument 'datasourceName' must not be blank";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final GsqlFactory gsqlFactory;
    private final GsqlStorage gsqlStorage;

    @Inject
    public DefaultGsqlHandler(@Nonnull GsqlFactory gsqlFactory, @Nonnull GsqlStorage gsqlStorage) {
        this.gsqlFactory = requireNonNull(gsqlFactory, "Argument 'gsqlFactory' must not be null");
        this.gsqlStorage = requireNonNull(gsqlStorage, "Argument 'gsqlStorage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withSql(@Nonnull GsqlCallback<R> callback) throws RuntimeGsqlException {
        return withSql(DefaultGsqlFactory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    public <R> R withSql(@Nonnull String datasourceName, @Nonnull GsqlCallback<R> callback) throws RuntimeGsqlException {
        requireNonBlank(datasourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);
        Sql sql = getSql(datasourceName);
        try {
            LOG.debug("Executing statements on datasource '{}'", datasourceName);
            return callback.handle(datasourceName, sql);
        } catch (Exception e) {
            throw new RuntimeGsqlException(datasourceName, e);
        }
    }

    @Override
    public void closeSql() {
        closeSql(DefaultGsqlFactory.KEY_DEFAULT);
    }

    @Override
    public void closeSql(@Nonnull String datasourceName) {
        Sql sql = gsqlStorage.get(datasourceName);
        if (sql != null) {
            gsqlFactory.destroy(datasourceName, sql);
            gsqlStorage.remove(datasourceName);
        }
    }

    @Nonnull
    private Sql getSql(@Nonnull String datasourceName) {
        Sql sql = gsqlStorage.get(datasourceName);
        if (sql == null) {
            sql = gsqlFactory.create(datasourceName);
            gsqlStorage.set(datasourceName, sql);
        }
        return sql;
    }
}
