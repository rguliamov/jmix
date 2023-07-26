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

package io.jmix.dynattrflowui.view.category;

import com.google.common.io.Files;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.entity.ReferenceToEntity;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_Category.browse")
@ViewDescriptor("category-browse.xml")
@LookupComponent("categoriesTable")
public class CategoryBrowse extends StandardListView<Category> {

    private static final Logger log = LoggerFactory.getLogger(CategoryBrowse.class);

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected FileUploadField importField;

    @ViewComponent
    protected DataGrid<Category> categoriesTable;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> attributesDc;
    @ViewComponent
    protected InstanceContainer<Category> categoryDc;
    @ViewComponent
    protected InstanceLoader<Category> categoryDl;
    @ViewComponent
    private CollectionLoader<Category> categoriesDl;
    @ViewComponent
    private AccessManager accessManager;
    @ViewComponent
    private Button applyChangesBtn;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        Objects.requireNonNull(attributesDc.getSorter()).sort(Sort.by(Sort.Direction.ASC, "orderNo"));
        setupFieldsLock();
    }

    @Subscribe("categoriesTable.applyChanges")
    protected void onCategoriesTableApplyChanges(ActionPerformedEvent event) {
        dynAttrMetadata.reload();
        notifications.create(messages.getMessage(CategoryBrowse.class, "notification.changesApplied"))
                .withType(Notifications.Type.WARNING)
                .show();
    }

    @Install(to = "categoriesTable.entityType", subject = "columnGenerator")
    protected Span categoriesTableEntityTypeColumnGenerator(Category category) {
        Span dataTypeLabel = uiComponents.create(Span.class);
        MetaClass metaClass = metadata.getSession().getClass(category.getEntityType());
        dataTypeLabel.setText(messageTools.getEntityCaption(metaClass));
        return dataTypeLabel;
    }

//  todo  @Install(to = "attributesTable.dataType", subject = "columnGenerator")
//    protected Table.PlainTextCell attributesTableDataTypeColumnGenerator(CategoryAttribute categoryAttribute) {
//        String labelContent;
//        if (BooleanUtils.isTrue(categoryAttribute.getIsEntity())) {
//            Class<?> clazz = categoryAttribute.getJavaType();
//            if (clazz != null) {
//                MetaClass metaClass = metadata.getSession().getClass(clazz);
//                labelContent = messageTools.getEntityCaption(metaClass);
//            } else {
//                labelContent = "";
//            }
//        } else {
//            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
//            labelContent = messages.getMessage(AttributeType.class, key);
//        }
//        return new Table.PlainTextCell(labelContent);
//    }

    @Install(to = "categoriesTable.edit", subject = "afterCommitHandler")
    private void categoriesTableEditAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Install(to = "categoriesTable.create", subject = "afterCommitHandler")
    private void categoriesTableCreateAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Subscribe(id = "categoriesDc", target = Target.DATA_CONTAINER)
    protected void onCategoriesDcItemChange(InstanceContainer.ItemChangeEvent<Category> event) {
        Category category = event.getItem();
        if (category != null) {
            categoryDl.setEntityId(category.getId());
            categoryDl.load();
        } else {
            categoryDc.setItem(null);
        }
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            applyChangesBtn.setEnabled(false);
        }
    }

    @Subscribe("exportBtn.exportJSON")
    public void onExportBtnExportJSON(ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("exportBtn.exportZIP")
    public void onExportBtnExportZIP(ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        Collection<Category> selected = categoriesTable.getSelectedItems();
        if (selected.isEmpty() && categoriesTable.getItems() != null) {
            selected = categoriesTable.getItems().getItems();
        }

        if (selected.isEmpty()) {
            notifications.create(messages.getMessage(CategoryBrowse.class, "nothingToExport"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
            return;
        }

        try {
            byte[] data;
            if (downloadFormat == JSON) {
                data = entityImportExport.exportEntitiesToJSON(new ArrayList<>(selected), buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
            } else {
                data = entityImportExport.exportEntitiesToZIP(new ArrayList<>(selected), buildExportFetchPlan());
            }
            downloader.download(data, String.format("Categories.%s", downloadFormat.getFileExt()), downloadFormat);
        } catch (Exception e) {
            log.warn("Unable to export categories", e);
            notifications.create(messages.getMessage(CategoryBrowse.class, "exportFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(Category.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("categoryAttrs", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            Collection<Object> importedEntities;
            if (JSON.getFileExt().equals(Files.getFileExtension(event.getFileName()))) {
                importedEntities = entityImportExport.importEntitiesFromJson(new String(bytes, StandardCharsets.UTF_8), createEntityImportPlan());
            } else {
                importedEntities = entityImportExport.importEntitiesFromZIP(bytes, createEntityImportPlan());
            }

            if (importedEntities.size() > 0) {
                categoriesDl.load();
                notifications.create(messages.getMessage(CategoryBrowse.class, "importSuccessful"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import categories", e);
            notifications.create(messages.getMessage(CategoryBrowse.class, "importFailed"), e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected EntityImportPlan createEntityImportPlan() {
        return entityImportPlans.builder(Category.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "categoryAttrs",
                        entityImportPlans.builder(CategoryAttribute.class)
                                .addLocalProperties()
                                .addEmbeddedProperty(
                                        "defaultEntity",
                                        entityImportPlans.builder(ReferenceToEntity.class)
                                                .addLocalProperties().
                                                build()
                                )
                                .build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }
}
