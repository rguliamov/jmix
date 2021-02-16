/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.property.UiFilterProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class GroupFilterImpl extends CompositeComponent<GroupBoxLayout> implements GroupFilter {

    protected static final String GROUP_FILTER_STYLENAME = "jmix-group-filter";

    protected UiComponents uiComponents;

    protected DataLoader dataLoader;
    protected Condition initialDataLoaderCondition;
    protected boolean autoApply;

    @Internal
    protected boolean conditionModificationDelegated = false;

    protected int columnsCount;
    protected CaptionPosition captionPosition = CaptionPosition.LEFT;

    protected Operation operation = Operation.AND;
    protected LogicalCondition queryCondition = LogicalCondition.and();

    protected List<FilterComponent> ownFilterComponentsOrder = new ArrayList<>();

    protected ResponsiveGridLayout conditionsLayout;

    public GroupFilterImpl() {
        addCreateListener(this::onCreate);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUiFilterProperties(UiFilterProperties uiFilterProperties) {
        this.columnsCount = uiFilterProperties.getColumnsCount();
        this.autoApply = uiFilterProperties.isAutoApply();
    }

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
        checkNotNull(dataLoader);

        this.dataLoader = dataLoader;
        this.initialDataLoaderCondition = dataLoader.getCondition();

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply != autoApply) {
            this.autoApply = autoApply;

            getOwnFilterComponents().forEach(filterComponent -> filterComponent.setAutoApply(autoApply));
        }
    }

    @Internal
    @Override
    public boolean isConditionModificationDelegated() {
        return conditionModificationDelegated;
    }

    @Internal
    @Override
    public void setConditionModificationDelegated(boolean conditionModificationDelegated) {
        this.conditionModificationDelegated = conditionModificationDelegated;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        if (this.operation != operation) {
            this.operation = operation;

            updateQueryCondition();

            if (!isConditionModificationDelegated()) {
                updateDataLoaderCondition();
            }
        }
    }

    @Override
    public int getColumnsCount() {
        return columnsCount;
    }

    @Override
    public void setColumnsCount(int columnsCount) {
        if (this.columnsCount != columnsCount) {
            this.columnsCount = columnsCount;

            updateConditionsLayout();
        }
    }

    @Override
    public CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    @Override
    public void setCaptionPosition(CaptionPosition position) {
        if (this.captionPosition != position) {
            this.captionPosition = position;

            updateConditionsLayout();
        }
    }

    @Override
    public void add(FilterComponent filterComponent) {
        if (dataLoader != filterComponent.getDataLoader()) {
            throw new IllegalArgumentException("The data loader of child component must be the same as the owner " +
                    "GroupFilter component");
        }

        filterComponent.setConditionModificationDelegated(true);
        filterComponent.setAutoApply(isAutoApply());
        queryCondition.add(filterComponent.getQueryCondition());
        ownFilterComponentsOrder.add(filterComponent);
        updateConditionsLayout();

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }
    }

    @Override
    public void remove(FilterComponent filterComponent) {
        if (ownFilterComponentsOrder.contains(filterComponent)) {
            ownFilterComponentsOrder.remove(filterComponent);

            if (filterComponent instanceof SingleFilterComponent) {
                getDataLoader().removeParameter(((SingleFilterComponent<?>) filterComponent).getParameterName());
            }

            updateConditionsLayout();
            updateQueryCondition();

            if (!isConditionModificationDelegated()) {
                updateDataLoaderCondition();
            }
        } else {
            ownFilterComponentsOrder.stream()
                    .filter(ownComponent -> ownComponent instanceof LogicalFilterComponent)
                    .map(ownComponent -> (LogicalFilterComponent) ownComponent)
                    .forEach(childLogicalFilterComponent -> childLogicalFilterComponent.remove(filterComponent));
        }
    }

    @Override
    public void removeAll() {
        getComposition().removeAll();
        ownFilterComponentsOrder = new ArrayList<>();

        updateConditionsLayout();
        updateQueryCondition();

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }
    }

    @Override
    public LogicalCondition getQueryCondition() {
        return queryCondition;
    }

    @Override
    public List<FilterComponent> getOwnFilterComponents() {
        return ownFilterComponentsOrder;
    }

    @Override
    public List<FilterComponent> getFilterComponents() {
        List<FilterComponent> components = new ArrayList<>();
        for (FilterComponent ownComponent : ownFilterComponentsOrder) {
            components.add(ownComponent);
            if (ownComponent instanceof LogicalFilterComponent) {
                components.addAll(((LogicalFilterComponent) ownComponent).getFilterComponents());
            }
        }

        return components;
    }

    protected void onCreate(CreateEvent createEvent) {
        root = createRootComponent();
    }

    protected GroupBoxLayout createRootComponent() {
        GroupBoxLayout rootLayout = uiComponents.create(GroupBoxLayout.class);
        rootLayout.setWidthFull();
        rootLayout.unwrap(com.vaadin.ui.Component.class)
                .setPrimaryStyleName(GROUP_FILTER_STYLENAME);
        return rootLayout;
    }

    protected void updateQueryCondition() {
        queryCondition = new LogicalCondition(WrapperUtils.toLogicalConditionType(operation));

        for (FilterComponent ownComponent : ownFilterComponentsOrder) {
            queryCondition.add(ownComponent.getQueryCondition());
        }
    }

    protected void updateDataLoaderCondition() {
        if (dataLoader != null) {
            LogicalCondition resultCondition;
            if (initialDataLoaderCondition instanceof LogicalCondition) {
                resultCondition = (LogicalCondition) initialDataLoaderCondition.copy();
                resultCondition.add(getQueryCondition());
            } else if (initialDataLoaderCondition != null) {
                resultCondition = LogicalCondition.and()
                        .add(initialDataLoaderCondition)
                        .add(getQueryCondition());
            } else {
                resultCondition = getQueryCondition();
            }

            dataLoader.setCondition(resultCondition);
        }
    }

    protected void updateConditionsLayout() {
        getComposition().removeAll();

        boolean isAnyFilterComponentVisible = getOwnFilterComponents().stream().anyMatch(Component::isVisible);

        if (isAnyFilterComponentVisible) {
            conditionsLayout = createConditionsLayout();
            getComposition().add(conditionsLayout);

            ResponsiveGridLayout.Row row = createConditionsLayoutRow(conditionsLayout);
            getOwnFilterComponents().stream()
                    .filter(Component::isVisible)
                    .forEach(ownFilterComponent -> {
                        if (ownFilterComponent instanceof LogicalFilterComponent) {
                            addLogicalFilterComponentToConditionsLayoutRow(
                                    (LogicalFilterComponent) ownFilterComponent, row);
                        } else {
                            addFilterComponentToConditionsLayoutRow(ownFilterComponent, row);
                        }
                    });
        } else {
            conditionsLayout = null;
        }
    }

    protected ResponsiveGridLayout createConditionsLayout() {
        ResponsiveGridLayout layout = uiComponents.create(ResponsiveGridLayout.NAME);
        layout.addStyleName("px-0");
        return layout;
    }

    protected ResponsiveGridLayout.Row createConditionsLayoutRow(ResponsiveGridLayout layout) {
        ResponsiveGridLayout.Row row = layout.addRow();

        int columnsCount = getColumnsCount();
        Map<ResponsiveGridLayout.Breakpoint, ResponsiveGridLayout.RowColumnsValue> rowColumns = new HashMap<>();
        rowColumns.put(ResponsiveGridLayout.Breakpoint.XS,
                ResponsiveGridLayout.RowColumnsValue.columns(1));

        if (columnsCount > 1) {
            rowColumns.put(ResponsiveGridLayout.Breakpoint.LG,
                    ResponsiveGridLayout.RowColumnsValue.columns(2));
        }

        if (columnsCount > 2) {
            rowColumns.put(ResponsiveGridLayout.Breakpoint.XL,
                    ResponsiveGridLayout.RowColumnsValue.columns(columnsCount));
        }

        row.setRowColumns(rowColumns);

        row.setAlignItems(ResponsiveGridLayout.AlignItems.CENTER);
        return row;
    }

    protected void addLogicalFilterComponentToConditionsLayoutRow(LogicalFilterComponent logicalFilterComponent,
                                                                  ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column column = createLogicalFilterComponentColumn(row);
        logicalFilterComponent.setParent(null);
        ComponentsHelper.getComposition(logicalFilterComponent).setParent(null);
        column.setComponent(logicalFilterComponent);

        if (logicalFilterComponent instanceof SupportsCaptionPosition) {
            ((SupportsCaptionPosition) logicalFilterComponent).setCaptionPosition(getCaptionPosition());
        }

        if (logicalFilterComponent instanceof SupportsColumnsCount) {
            ((SupportsColumnsCount) logicalFilterComponent).setColumnsCount(getColumnsCount());
        }
    }

    protected ResponsiveGridLayout.Column createLogicalFilterComponentColumn(ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column column = row.addColumn();
        column.setColumns(ResponsiveGridLayout.Breakpoint.XL, ResponsiveGridLayout.ColumnsValue.columns(12));

        int columnIndex = row.getColumns().indexOf(column);
        if (columnIndex != 0) {
            column.addStyleName("pt-2");
        }

        return column;
    }

    protected void addFilterComponentToConditionsLayoutRow(FilterComponent filterComponent,
                                                           ResponsiveGridLayout.Row row) {
        ResponsiveGridLayout.Column conditionValueColumn = createFilterComponentColumn(row);

        filterComponent.setParent(null);
        ComponentsHelper.getComposition(filterComponent).setParent(null);

        filterComponent.setWidthFull();
        if (filterComponent instanceof SupportsCaptionPosition) {
            ((SupportsCaptionPosition) filterComponent).setCaptionPosition(getCaptionPosition());
        }

        conditionValueColumn.setComponent(filterComponent);
    }

    protected ResponsiveGridLayout.Column createFilterComponentColumn(ResponsiveGridLayout.Row row) {
        boolean logicalFilterComponentAdded = row.getColumns().stream()
                .anyMatch(rowColumn -> rowColumn.getComponent() instanceof LogicalFilterComponent);

        ResponsiveGridLayout.Column column = row.addColumn();

        int columnIndex = row.getColumns().indexOf(column);
        if (columnIndex != 0) {
            column.addStyleName("pt-2");
        }

        if (!logicalFilterComponentAdded) {
            int columnsCount = getColumnsCount();

            if (columnIndex == 1 && columnsCount > 1) {
                column.addStyleName("pt-lg-0");
            }

            if (columnIndex == 2 && columnsCount > 2) {
                column.addStyleName("pt-xl-0");
            }
        }

        return column;
    }
}
