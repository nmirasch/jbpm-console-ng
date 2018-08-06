/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.workbench.ht.client.editors.taskdetailsmulti;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.jbpm.workbench.ht.client.resources.i18n.Constants;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class TaskDetailsMultiViewImpl extends Composite
        implements TaskDetailsMultiPresenter.TaskDetailsMultiView {

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    NavTabs navTabs = GWT.create(NavTabs.class);

    @UiField
    TabContent tabContent = GWT.create(TabContent.class);

    private Constants constants = GWT.create(Constants.class);

    private TaskDetailsMultiPresenter presenter;

    private TabPane genericFormDisplayPane;

    private TabListItem genericFormDisplayTab;

    private TabPane taskDetailsPane;

    private TabListItem taskDetailsTab;

    private TabPane taskAssignmentsPane;

    private TabListItem taskAssignmentsTab;

    private TabPane taskCommentsPane;

    private TabListItem taskCommentsTab;

    private TabPane taskAdminPane;

    private TabListItem taskAdminTab;

    private TabPane taskLogsPane;

    private TabListItem taskLogsTab;

    @Inject
    private Event<NotificationEvent> notification;

    @Override
    public void init(final TaskDetailsMultiPresenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        this.presenter = presenter;
        initTabs();
    }

    protected void initTabs() {
        tabContent.setPaddingBottom(50);

        {
            genericFormDisplayPane = GWT.create(TabPane.class);
            genericFormDisplayPane.add(presenter.getGenericFormView());

            genericFormDisplayTab = GWT.create(TabListItem.class);
            genericFormDisplayTab.setText(constants.Work());
            genericFormDisplayTab.setDataTargetWidget(genericFormDisplayPane);
            genericFormDisplayTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(genericFormDisplayTab);
            tabContent.add(genericFormDisplayPane);
        }

        {
            taskDetailsPane = GWT.create(TabPane.class);
            taskDetailsPane.add(presenter.getTaskDetailsView());

            taskDetailsTab = GWT.create(TabListItem.class);
            taskDetailsTab.setText(constants.Details());
            taskDetailsTab.setDataTargetWidget(taskDetailsPane);
            taskDetailsTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(taskDetailsTab);
            tabContent.add(taskDetailsPane);
        }

        {
            taskAssignmentsPane = GWT.create(TabPane.class);
            taskAssignmentsPane.add(presenter.getTaskAssignmentsView());

            taskAssignmentsTab = GWT.create(TabListItem.class);
            taskAssignmentsTab.setText(constants.Assignments());
            taskAssignmentsTab.setDataTargetWidget(taskAssignmentsPane);
            taskAssignmentsTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(taskAssignmentsTab);
            tabContent.add(taskAssignmentsPane);
            taskAssignmentsTab.addShowHandler(new TabShowHandler() {
                @Override
                public void onShow(final TabShowEvent event) {
                    presenter.taskAssignmentsRefresh();
                }
            });
        }

        {
            taskCommentsPane = GWT.create(TabPane.class);
            taskCommentsPane.add(presenter.getTaskCommentsView());

            taskCommentsTab = GWT.create(TabListItem.class);
            taskCommentsTab.setText(constants.Comments());
            taskCommentsTab.setDataTargetWidget(taskCommentsPane);
            taskCommentsTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(taskCommentsTab);
            tabContent.add(taskCommentsPane);
            taskCommentsTab.addShowHandler(new TabShowHandler() {
                @Override
                public void onShow(final TabShowEvent event) {
                    presenter.taskCommentsRefresh();
                }
            });
        }

        {
            taskAdminPane = GWT.create(TabPane.class);
            taskAdminPane.add(presenter.getTaskAdminView());

            taskAdminTab = GWT.create(TabListItem.class);
            taskAdminTab.setText(constants.Task_Admin());
            taskAdminTab.setDataTargetWidget(taskAdminPane);
            taskAdminTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(taskAdminTab);
            tabContent.add(taskAdminPane);
            taskAdminTab.addShowHandler(new TabShowHandler() {
                @Override
                public void onShow(final TabShowEvent event) {
                    presenter.taskAdminRefresh();
                }
            });
        }

        {
            taskLogsPane = GWT.create(TabPane.class);
            taskLogsPane.add(presenter.getTaskLogsView());

            taskLogsTab = GWT.create(TabListItem.class);
            taskLogsTab.setText(constants.Logs());
            taskLogsTab.setDataTargetWidget(taskLogsPane);
            taskLogsTab.addStyleName("uf-dropdown-tab-list-item");

            navTabs.add(taskLogsTab);
            tabContent.add(taskLogsPane);
            taskLogsTab.addShowHandler(new TabShowHandler() {
                @Override
                public void onShow(final TabShowEvent event) {
                    presenter.taskLogsRefresh();
                }
            });
        }
    }

    @Override
    public void setAdminTabVisible(final boolean value) {
        taskAdminTab.setVisible(value);
        taskAdminPane.setVisible(value);
    }

    @Override
    public void resetTabs(boolean onlyLogTab) {
        if (onlyLogTab) {
            taskDetailsTab.showTab();
        } else {
            ((TabListItem) navTabs.getWidget(0)).showTab();
        }
    }

    @Override
    public void displayAllTabs() {
        for (Widget active : navTabs) {
            active.setVisible(true);
        }
        for (Widget active : tabContent) {
            active.setVisible(true);
        }
    }

    @Override
    public void displayOnlyLogTab() {
        for (Widget active : navTabs) {
            active.setVisible(false);
        }
        for (Widget active : tabContent) {
            active.setVisible(false);
        }
        taskDetailsPane.setVisible(true);
        taskDetailsTab.setVisible(true);

        taskLogsPane.setVisible(true);
        taskLogsTab.setVisible(true);

        taskCommentsPane.setVisible(true);
        taskCommentsTab.setVisible(true);
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    interface Binder
            extends
            UiBinder<Widget, TaskDetailsMultiViewImpl> {

    }
}