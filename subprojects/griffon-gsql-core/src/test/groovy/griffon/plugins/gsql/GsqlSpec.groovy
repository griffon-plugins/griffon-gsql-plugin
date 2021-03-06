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
package griffon.plugins.gsql

import griffon.annotations.inject.BindTo
import griffon.core.GriffonApplication
import griffon.core.RunnableWithArgs
import griffon.plugins.datasource.events.DataSourceConnectEndEvent
import griffon.plugins.datasource.events.DataSourceConnectStartEvent
import griffon.plugins.datasource.events.DataSourceDisconnectEndEvent
import griffon.plugins.datasource.events.DataSourceDisconnectStartEvent
import griffon.plugins.gsql.events.GsqlConnectEndEvent
import griffon.plugins.gsql.events.GsqlConnectStartEvent
import griffon.plugins.gsql.events.GsqlDisconnectEndEvent
import griffon.plugins.gsql.events.GsqlDisconnectStartEvent
import griffon.plugins.gsql.exceptions.RuntimeGsqlException
import griffon.test.core.GriffonUnitRule
import groovy.sql.DataSet
import groovy.sql.Sql
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.application.event.EventHandler
import javax.inject.Inject

@Unroll
class GsqlSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private GsqlHandler gsqlHandler

    @Inject
    private GriffonApplication application

    void 'Open and close default gsql'() {
        given:
        List eventNames = [
            'GsqlConnectStartEvent', 'DataSourceConnectStartEvent',
            'DataSourceConnectEndEvent', 'GsqlConnectEndEvent',
            'GsqlDisconnectStartEvent', 'DataSourceDisconnectStartEvent',
            'DataSourceDisconnectEndEvent', 'GsqlDisconnectEndEvent'
        ]
        TestEventHandler testEventHandler = new TestEventHandler()
        application.eventRouter.subscribe(testEventHandler)

        when:
        gsqlHandler.withSql { String datasourceName, Sql sql ->
            true
        }
        gsqlHandler.closeSql()
        // second call should be a NOOP
        gsqlHandler.closeSql()

        then:
        testEventHandler.events.size() == 8
        testEventHandler.events == eventNames
    }

    void 'Connect to default Sql'() {
        expect:
        gsqlHandler.withSql { String datasourceName, Sql sql ->
            datasourceName == 'default' && sql
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        gsqlHandler.withSql { String datasourceName, Sql sql -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        gsqlHandler.withSql { String datasourceName, Sql sql -> }
        gsqlHandler.closeSql()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name Sql'() {
        expect:
        gsqlHandler.withSql(name) { String datasourceName, Sql sql ->
            datasourceName == name && sql
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus Sql name (#name) results in error'() {
        when:
        gsqlHandler.withSql(name) { String datasourceName, Sql sql ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people table'() {
        when:
        List peopleIn = gsqlHandler.withSql('people') { String datasourceName, Sql sql ->
            DataSet people = sql.dataSet('people')
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                people.add(data)
            }
        }

        List peopleOut = gsqlHandler.withSql('people') { String datasourceName, Sql sql ->
            DataSet people = sql.dataSet('people')
            people.rows().collect([]) { row ->
                [id: row.id, name: row.name, lastname: row.lastname]
            }
        }

        then:
        peopleIn == peopleOut
    }

    void 'A runtime SQLException is thrown within Sql handling'() {
        when:
        gsqlHandler.withSql { String datasourceName, Sql sql ->
            DataSet people = sql.dataSet('people')
            people.add([id: 0])
        }

        then:
        thrown(RuntimeGsqlException)
    }

    @BindTo(GsqlBootstrap)
    private TestGsqlBootstrap bootstrap = new TestGsqlBootstrap()

    private class TestEventHandler {
        List<String> events = []

        @EventHandler
        void handleDataSourceConnectStartEvent(DataSourceConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceConnectEndEvent(DataSourceConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectStartEvent(DataSourceDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectEndEvent(DataSourceDisconnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleGsqlConnectStartEvent(GsqlConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleGsqlConnectEndEvent(GsqlConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleGsqlDisconnectStartEvent(GsqlDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleGsqlDisconnectEndEvent(GsqlDisconnectEndEvent event) {
            events << event.class.simpleName
        }
    }
}
