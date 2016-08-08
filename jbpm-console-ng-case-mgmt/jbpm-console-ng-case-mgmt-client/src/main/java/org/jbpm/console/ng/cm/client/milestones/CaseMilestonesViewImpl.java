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

package org.jbpm.console.ng.cm.client.milestones;

import java.util.Date;
import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class CaseMilestonesViewImpl extends Composite implements CaseMilestonesPresenter.View {

    //    @Inject @DataField
//    private ListGroup listgroup = GWT.create(ListGroup.class);

    public CaseMilestonesViewImpl() {
//        initWidget(listgroup);
    }

    @Override
    public void init(final CaseMilestonesPresenter presenter) {
//        final ListGroupItem item1 = GWT.create(ListGroupItem.class);
//        item1.setText("milestone 1");
//        listgroup.add(item1);
//        final ListGroupItem item2 = GWT.create(ListGroupItem.class);
//        item2.setText("milestone 2");
//        listgroup.add(item2);
    }

    @Override
    public void addMilestone(final String name) {
//        final Heading heading = new Heading(HeadingSize.H4, name);
//        final Label label = GWT.create(Label.class);
//        label.setType(LabelType.DEFAULT);
//        label.setText("AVAILABLE");
//        label.setPull(Pull.RIGHT);
//        heading.add(label);
//        final ListGroupItem item = GWT.create(ListGroupItem.class);
//        item.add(heading);
//        listgroup.add(item);
    }

    @Override
    public void addArchivedMilestone(final String name, final Date achievedAt) {
//        final Heading heading = new Heading(HeadingSize.H4, name);
//        final Label label = GWT.create(Label.class);
//        label.setType(LabelType.SUCCESS);
//        label.setText("COMPLETED");
//        label.setPull(Pull.RIGHT);
//        heading.add(label);
//        final LinkedGroupItemText text = GWT.create(LinkedGroupItemText.class);
//        final Icon clock = new Icon(IconType.CLOCK_O);
//        clock.getElement().getStyle().setPaddingRight(10, Style.Unit.PX);
//        text.add(clock);
//        text.add(new Text(achievedAt.toString()));
//        final ListGroupItem item = GWT.create(ListGroupItem.class);
//        item.add(heading);
//        item.add(text);
//        listgroup.add(item);
    }

    @Override
    public void clearAllMilestones() {
//        listgroup.clear();
    }
}