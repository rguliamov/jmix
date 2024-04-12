/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.structure.impl;

import com.google.common.base.Preconditions;
import io.jmix.reports.yarg.structure.ReportParameterWithDefaultValue;

public class ReportParameterImpl implements ReportParameterWithDefaultValue {
    protected String name;
    protected String alias;
    protected Boolean required;
    protected Class paramClass;
    protected String defaultValue = null;

    public ReportParameterImpl(String name, String alias, Boolean required, Class paramClass) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(alias, "\"alias\" parameter can not be null");

        this.name = name;
        this.alias = alias;
        this.required = required;
        this.paramClass = paramClass;
    }

    public ReportParameterImpl(String name, String alias, Boolean required, Class paramClass, String defaultValue) {
        this(name, alias, required, paramClass);
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Boolean getRequired() {
        return required;
    }

    @Override
    public Class getParameterClass() {
        return paramClass;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}