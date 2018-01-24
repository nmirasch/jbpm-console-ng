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

package org.jbpm.workbench.common.client.list;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.OrderedList;
import org.jboss.errai.common.client.dom.UnorderedList;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

import static org.jboss.errai.common.client.dom.DOMUtil.addCSSClass;
import static org.jboss.errai.common.client.dom.DOMUtil.removeCSSClass;
import static org.jboss.errai.common.client.dom.Window.getDocument;

@Dependent
@Templated
public class BreadcrumbViewImpl  implements BreadcrumbView{

    @Inject
    @DataField("breadcrumb-container")
    Div container;

    @Inject
    @DataField("breadcrumb-path")
    OrderedList orderedList;

    @Inject
    @DataField("breadcrumb-title")
    Span breadcrumbTitle;

    @Inject
    @DataField("primary-action")
    Button primaryAction;

    @Inject
    @DataField("actions-dropdown")
    Div actions;

    @Inject
    @DataField("actions-button")
    Button actionsButton;

    @Inject
    @DataField("actions-items")
    UnorderedList actionsItems;

    Command primaryActionCommand;

    @PostConstruct
    public void init() {
    }

    @Override
    public void setBreadcrubTitle(final String title) {
        breadcrumbTitle.setTextContent(title);
    }

    public void addPrimaryAction(final BreadcrumbAction action) {
        removeCSSClass(primaryAction,
                       "hidden");
        primaryAction.setTextContent(action.label());
        primaryActionCommand= action;
    }

    public void addSubAction(final BreadcrumbAction action) {
        removeCSSClass(actions,
                       "hidden");
        removeCSSClass(actionsButton,
                       "disabled");

        final HTMLElement a = getDocument().createElement("a");
        a.setTextContent(action.label());
        a.setOnclick(e -> action.execute());

        final HTMLElement li = getDocument().createElement("li");
        li.appendChild(a);
        actionsItems.appendChild(li);
    }

    public void addElementToPath(final BreadcrumbAction action) {

        final HTMLElement a = getDocument().createElement("a");
        a.setTextContent(action.label());
        a.setOnclick(e -> action.execute());

        final HTMLElement li = getDocument().createElement("li");
        li.appendChild(a);
        orderedList.appendChild(li);
    }


    public void setLastElementStyle() {
        addCSSClass(actions,
                    "dropup");
    }

    @EventHandler("primary-action")
    public void onPrimaryActionClick(final @ForEvent("click") MouseEvent event) {
        primaryActionCommand.execute();
    }

    @Override
    public HTMLElement getElement() {
        return container;
    }

}