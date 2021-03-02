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
package org.codehaus.griffon.compile.gsql.ast.transform

import griffon.plugins.gsql.GsqlHandler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Andres Almiray
 */
class GsqlAwareASTTransformationSpec extends Specification {
    def 'GsqlAwareASTTransformation is applied to a bean via @GsqlAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        @griffon.transform.gsql.GsqlAware
        class Bean { }
        new Bean()
        ''')

        then:
        bean instanceof GsqlHandler
        GsqlHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                candidate.returnType == target.returnType &&
                candidate.parameterTypes == target.parameterTypes &&
                candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }

    def 'GsqlAwareASTTransformation is not applied to a GsqlHandler subclass via @GsqlAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        import griffon.plugins.gsql.GsqlCallback
        import griffon.plugins.gsql.exceptions.RuntimeGsqlException
        import griffon.plugins.gsql.GsqlHandler

        import griffon.annotations.core.Nonnull
        
        @griffon.transform.gsql.GsqlAware
        class GsqlHandlerBean implements GsqlHandler {
            @Override
            public <R> R withSql(@Nonnull GsqlCallback<R> callback) throws RuntimeGsqlException {
                return null
            }
            @Override
            public <R> R withSql(@Nonnull String datasourceName, @Nonnull GsqlCallback<R> callback) throws RuntimeGsqlException {
                return null
            }
            @Override
            void closeSql(){}
            @Override
            void closeSql(@Nonnull String datasourceName){}
        }
        new GsqlHandlerBean()
        ''')

        then:
        bean instanceof GsqlHandler
        GsqlHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }
}
