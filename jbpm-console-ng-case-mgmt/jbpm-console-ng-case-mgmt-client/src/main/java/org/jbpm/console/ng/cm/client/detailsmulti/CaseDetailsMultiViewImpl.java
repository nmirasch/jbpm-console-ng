/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.console.ng.cm.client.detailsmulti;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jbpm.console.ng.cm.client.details.CaseDetailsPresenter;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;


@Dependent
public class CaseDetailsMultiViewImpl extends Composite implements CaseDetailsMultiPresenter.CaseDetailsMultiView, RequiresResize {

    private static Binder uiBinder = GWT.create(Binder.class);

    private ScrollPanel caseDetailsScrollPanel = GWT.create(ScrollPanel.class);

    private TabLayoutPanel tabsContainer;

    @Inject
    private CaseDetailsPresenter caseDetailsPresenter;

    public CaseDetailsMultiViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init(final CaseDetailsMultiPresenter presenter) {
    }

    public void initTabs() {
        tabsContainer.add(caseDetailsScrollPanel, Constants.INSTANCE.Details());

        caseDetailsScrollPanel.add(caseDetailsPresenter.getView());

        tabsContainer.addSelectionHandler(e -> caseDetailsPresenter.refreshCase());
    }

    @Override
    public void onResize() {
//        Scheduler.get().scheduleDeferred(() -> {
//            tabsContainer.setHeight(CaseDetailsMultiViewImpl.this.getParent().getOffsetHeight() - 30 + "px");
//            caseDetailsScrollPanel.setHeight(CaseDetailsMultiViewImpl.this.getParent().getOffsetHeight() - 30 + "px");
//        });
    }

    interface Binder extends UiBinder<Widget, CaseDetailsMultiViewImpl> {
    }

}