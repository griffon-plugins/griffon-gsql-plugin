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
package org.codehaus.griffon.compile.gsql;

import org.codehaus.griffon.compile.core.BaseConstants;
import org.codehaus.griffon.compile.core.MethodDescriptor;

import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedType;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotations;
import static org.codehaus.griffon.compile.core.MethodDescriptor.args;
import static org.codehaus.griffon.compile.core.MethodDescriptor.method;
import static org.codehaus.griffon.compile.core.MethodDescriptor.throwing;
import static org.codehaus.griffon.compile.core.MethodDescriptor.type;
import static org.codehaus.griffon.compile.core.MethodDescriptor.typeParams;
import static org.codehaus.griffon.compile.core.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface GsqlAwareConstants extends BaseConstants {
    String SQL_TYPE = "groovy.sql.Sql";
    String GSQL_HANDLER_TYPE = "griffon.plugins.gsql.GsqlHandler";
    String GSQL_CALLBACK_TYPE = "griffon.plugins.gsql.GsqlCallback";
    String RUNTIME_GSQL_EXCEPTION_TYPE = "griffon.plugins.gsql.exceptions.RuntimeGsqlException";
    String GSQL_HANDLER_PROPERTY = "gsqlHandler";
    String GSQL_HANDLER_FIELD_NAME = "this$" + GSQL_HANDLER_PROPERTY;

    String METHOD_WITH_SQL = "withSql";
    String METHOD_CLOSE_SQL = "closeSql";
    String DATASOURCE_NAME = "datasourceName";
    String CALLBACK = "callback";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_CLOSE_SQL
        ),
        method(
            type(VOID),
            METHOD_CLOSE_SQL,
            args(annotatedType(types(type(JAVAX_ANNOTATION_NULLABLE)), JAVA_LANG_STRING))
        ),

        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(R),
            typeParams(R),
            METHOD_WITH_SQL,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), GSQL_CALLBACK_TYPE, R)),
            throwing(type(RUNTIME_GSQL_EXCEPTION_TYPE))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NONNULL)),
            type(R),
            typeParams(R),
            METHOD_WITH_SQL,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), GSQL_CALLBACK_TYPE, R)),
            throwing(type(RUNTIME_GSQL_EXCEPTION_TYPE))
        )
    };
}
