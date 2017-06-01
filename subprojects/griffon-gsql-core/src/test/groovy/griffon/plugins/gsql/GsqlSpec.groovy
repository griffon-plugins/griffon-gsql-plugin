/*
 * Copyright 2014-2017 the original author or authors.
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
package griffon.plugins.gsql

import griffon.core.GriffonApplication
import griffon.core.RunnableWithArgs
import griffon.core.test.GriffonUnitRule
import griffon.inject.BindTo
import griffon.plugins.gsql.exceptions.RuntimeGsqlException
import groovy.sql.DataSet
import groovy.sql.Sql
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

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
            'GsqlConnectStart', 'DataSourceConnectStart',
            'DataSourceConnectEnd', 'GsqlConnectEnd',
            'GsqlDisconnectStart', 'DataSourceDisconnectStart',
            'DataSourceDisconnectEnd', 'GsqlDisconnectEnd'
        ]
        List events = []
        eventNames.each { name ->
            application.eventRouter.addEventListener(name, ({ Object... args ->
                events << [name: name, args: args]
            }) as RunnableWithArgs)
        }

        when:
        gsqlHandler.withSql { String datasourceName, Sql sql ->
            true
        }
        gsqlHandler.closeSql()
        // second call should be a NOOP
        gsqlHandler.closeSql()

        then:
        events.size() == 8
        events.name == eventNames
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
}
