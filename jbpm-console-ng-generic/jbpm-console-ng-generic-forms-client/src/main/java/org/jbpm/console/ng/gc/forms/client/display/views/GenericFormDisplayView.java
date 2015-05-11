/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.gc.forms.client.display.views;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.workbench.events.NotificationEvent;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jbpm.console.ng.ga.forms.display.view.FormDisplayerView;

/**
 * @author salaboy
 */
@Dependent
@Templated(value = "GenericFormDisplayView.html")
public class GenericFormDisplayView extends Composite implements GenericFormDisplayPresenter.GenericFormDisplayView {

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    @DataField
    private FlowPanel formContainer;

    @Inject
    private EmbeddedFormDisplayView view;

    @PostConstruct
    public void init() {
        formContainer.add(view.getView());
    }
    @Override
    public FormDisplayerView getDisplayerView() {
        return view;
    }

    @Override
    public void displayNotification( final String text ) {
        notification.fire( new NotificationEvent( text ) );
    }
}