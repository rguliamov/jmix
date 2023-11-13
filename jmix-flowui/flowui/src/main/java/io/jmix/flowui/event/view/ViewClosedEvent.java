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

package io.jmix.flowui.event.view;

import io.jmix.flowui.view.View;
import org.springframework.context.ApplicationEvent;

/**
 * Application event that is sent after the view is closed.
 */
public class ViewClosedEvent extends ApplicationEvent {

    /**
     * Creates a new ViewClosedEvent.
     *
     * @param view the view on which the event initially occurred (never {@code null})
     */
    public ViewClosedEvent(View<?> view) {
        super(view);
    }

    @Override
    public View<?> getSource() {
        return ((View<?>) super.getSource());
    }
}
