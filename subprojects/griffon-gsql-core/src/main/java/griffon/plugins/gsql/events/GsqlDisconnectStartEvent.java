/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
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
package griffon.plugins.gsql.events;

import griffon.annotations.core.Nonnull;
import griffon.core.event.Event;
import groovy.sql.Sql;

import java.util.Map;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 3.0.0
 */
public class GsqlDisconnectStartEvent extends Event {
    private final String name;
    private final Map<String, Object> config;
    private final Sql sql;

    public GsqlDisconnectStartEvent(@Nonnull String name, @Nonnull Map<String, Object> config, @Nonnull Sql sql) {
        this.name = requireNonBlank(name, "Argument 'name' must not be blank");
        this.config = requireNonNull(config, "Argument 'config' must not be null");
        this.sql = requireNonNull(sql, "Argument 'sql' must not be null");
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Map<String, Object> getConfig() {
        return config;
    }

    @Nonnull
    public Sql getSql() {
        return sql;
    }

    @Nonnull
    public static GsqlDisconnectStartEvent of(@Nonnull String name, @Nonnull Map<String, Object> config, @Nonnull Sql sql) {
        return new GsqlDisconnectStartEvent(name, config, sql);
    }
}
