/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.console.ng.cm.client.comments;

import java.util.Date;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class CaseCommentsViewImpl extends Composite implements CaseCommentsPresenter.View {

    @Inject
    @DataField("comments")
    FlowPanel comments;

    @Inject
    @DataField("comment-area")
    TextArea comment;

    @Inject
    @DataField("add-comment")
    Anchor addComment;

    private CaseCommentsPresenter presenter;

    @Override
    public void init(final CaseCommentsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void removeAllComments() {
        comments.clear();
    }

    @Override
    public void addComment(final String comment, final String user, final Date time) {
        final Panel panel = GWT.create(Panel.class);
        final PanelHeader panelHeader = GWT.create(PanelHeader.class);
        final Strong strong = GWT.create(Strong.class);
        strong.setText(user);
        final Span headerSpan = GWT.create(Span.class);
        headerSpan.setText(" added a comment a few seconds ago");
        panelHeader.add(strong);
        panelHeader.add(headerSpan);
        final PanelBody panelBody = GWT.create(PanelBody.class);
        panelBody.getElement().getStyle().setPaddingRight(15, Style.Unit.PX);
        panelBody.getElement().getStyle().setPaddingLeft(15, Style.Unit.PX);
        panelBody.getElement().getStyle().setPaddingTop(10, Style.Unit.PX);
        panelBody.getElement().getStyle().setPaddingBottom(10, Style.Unit.PX);
        final Span span = GWT.create(Span.class);
        span.setText(comment);
        panelBody.add(span);
        panel.add(panelHeader);
        panel.add(panelBody);
        comments.add(panel);
    }

    @EventHandler("add-comment")
    @SuppressWarnings("unsued")
    protected void onAddCommentClick(final ClickEvent event) {
        presenter.addComment(comment.getText());
        comment.setText("");
    }

}
