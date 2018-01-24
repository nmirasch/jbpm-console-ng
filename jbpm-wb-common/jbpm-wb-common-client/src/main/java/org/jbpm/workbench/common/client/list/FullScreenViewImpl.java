/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.workbench.common.client.list;

import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;


@Dependent
@Templated
public class FullScreenViewImpl implements IsElement, FullScreenView{

    @Inject
    @DataField("container")
    Div container;

    @Inject
    @DataField("breadcrumb")
    BreadcrumbView breadcrumb;

    @Inject
    @DataField("screen-body")
    FlowPanel screenBody;

    @Inject
    PlaceManager placeManager;


    public void addScreenview(String placeId,final Map<String, String> parameters) {
        addWidget(placeId,
                  parameters,
                  screenBody);
    }

    public void addWidget(final String placeId,
                           final Map<String, String> parameters,
                           final HasWidgets widget) {
        placeManager.goTo(new DefaultPlaceRequest(placeId,parameters),
                          widget);
    }

    @Override
    public void setBreadcrubTitle(String title) {
        breadcrumb.setBreadcrubTitle(title);
    }

    @Override
    public void addSubAction(BreadcrumbAction action) {
        breadcrumb.addSubAction(action);
    }

    @Override
    public void addPrimaryAction(BreadcrumbAction action) {
        breadcrumb.addPrimaryAction(action);
    }

    @Override
    public void addElementToPath(BreadcrumbAction action) {
        breadcrumb.addElementToPath(action);
    }

    @Override
    public HTMLElement getElement() {
        return container;
    }
}