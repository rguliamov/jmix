/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import io.jmix.core.JmixModules;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Allows to sort the list of {@link ViewControllersConfiguration} in the same order as Jmix modules containing the screens
 * have been sorted.
 * @see AbstractBasePackageConfigurationSorter
 */
@Component("flowui_ViewControllersConfigurationSorter")
public class ViewControllersConfigurationSorter
        extends AbstractBasePackageConfigurationSorter<ViewControllersConfiguration> {

    public ViewControllersConfigurationSorter(JmixModules jmixModules) {
        super(jmixModules);
    }

    @Override
    protected List<String> getBasePackages(ViewControllersConfiguration configuration) {
        return configuration.getBasePackages();
    }
}
