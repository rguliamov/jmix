/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchopensearch.index;

import io.jmix.search.index.BaseIndexSettingsConfigurationContext;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.IndexSettingsAnalysis;

/**
 * Configuration context for OpenSearch index settings
 */
public class OpenSearchIndexSettingsConfigurationContext
        extends BaseIndexSettingsConfigurationContext<IndexSettings.Builder, IndexSettingsAnalysis.Builder> {

    public OpenSearchIndexSettingsConfigurationContext() {
        super(IndexSettings.Builder::new, IndexSettingsAnalysis.Builder::new);
    }
}
