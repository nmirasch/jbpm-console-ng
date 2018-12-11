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

package org.jbpm.workbench.pr.client.editors.diagram;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.ait.lienzo.client.core.shape.GridLayer;
import com.ait.lienzo.client.core.shape.Layer;

import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import org.uberfire.ext.widgets.common.client.common.BusyPopup;

@Dependent
@Templated
public class ProcessDiagramWidgetView extends Composite implements ProcessDiagramPresenter.View,
                                                                   RequiresResize {

    private static final double INC_FACTOR = 1.1;
    private static final int HEIGHT = 600;

    @Inject
    @DataField("processDiagramDiv")
    FlowPanel testsPanel;

    @Inject
    @DataField("message")
    @Named("span")
    HTMLElement heading;

    @Inject
    @DataField
    HTMLDivElement alert;

    private HorizontalPanel screenButtonsPanel = new HorizontalPanel();

    private double w;
    private double h;
    private Layer layer;
    private Picture picture;
    private Button scaleButton;

    public void displayImage(final String svgContent) {
        screenButtonsPanel.clear();
        testsPanel.clear();
        testsPanel.getElement().getStyle().setMargin(10,
                                                     Style.Unit.PX);
        testsPanel.getElement().getStyle().setBorderWidth(1,
                                                          Style.Unit.PX);
        testsPanel.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        testsPanel.getElement().getStyle().setBorderColor("#000000");

        int panelWidth = testsPanel.getElement().getOffsetWidth();

        final LienzoPanel panel = new LienzoPanel(panelWidth,
                                                  HEIGHT);
        applyGrid(panel);
        testsPanel.add(panel);

        layer = new Layer();
        layer.setTransformable(true);
        panel.add(layer);

        picture = new Picture(UriUtils.fromSafeConstant("data:image/svg+xml;utf8," + svgContent).asString(),
                              (Picture picture) -> {
                                  ProcessDiagramWidgetView.this.picture = picture;
                                  w = picture.getBoundingBox().getWidth();
                                  h = picture.getBoundingBox().getHeight();
                                  scalePicture(picture,
                                               w,
                                               h);
                                  layer.add(picture);
                                  layer.draw();
                              });

        setButtonsPanel(screenButtonsPanel);
        testsPanel.add(screenButtonsPanel);
    }

    private void applyGrid(final LienzoPanel panel) {

        // Grid.
        Line line1 = new Line(0,
                              0,
                              0,
                              0)
                .setStrokeColor("#0000FF")
                .setAlpha(0.2);
        Line line2 = new Line(0,
                              0,
                              0,
                              0)
                .setStrokeColor("#00FF00")
                .setAlpha(0.2);

        line2.setDashArray(2,
                           2);

        GridLayer gridLayer = new GridLayer(100,
                                            line1,
                                            25,
                                            line2);

        panel.setBackgroundLayer(gridLayer);
    }

    public void displayMessage(final String message) {
        alert.classList.remove("hidden");
        heading.textContent = message;
    }

    @Override
    public void onResize() {
        int height = getParent().getOffsetHeight();
        int width = getParent().getOffsetWidth();

        setPixelSize(width,
                     height);
    }

    @Override
    public void showBusyIndicator(final String message) {
        BusyPopup.showMessage(message);
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    public void setButtonsPanel(Panel panel) {

        // Some buttons for test scaling.
        final Button b1 = new Button("(+)");
        b1.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ProcessDiagramWidgetView.this.w = w * INC_FACTOR;
                ProcessDiagramWidgetView.this.h = h * INC_FACTOR;
                scalePicture();
            }
        });
        panel.add(b1);

        final Button b2 = new Button("(-)");
        b2.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ProcessDiagramWidgetView.this.w = w / INC_FACTOR;
                ProcessDiagramWidgetView.this.h = h / INC_FACTOR;
                scalePicture();
            }
        });
        panel.add(b2);

        scaleButton = new Button("(" + w + ", " + h + ")");
        scaleButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                scalePicture();
            }
        });
        panel.add(scaleButton);
    }

    private void scalePicture() {
        scalePicture(picture,
                     w,
                     h);
        scaleButton.setText("(" + w + ", " + h + ")");
        layer.draw();
    }

    private static void scalePicture(final Picture pictureToScale,
                                     final double width,
                                     final double height) {
        final BoundingBox bb = pictureToScale.getBoundingBox();
        final double[] scale = getScaleFactor(bb.getWidth(),
                                              bb.getHeight(),
                                              width,
                                              height);
        pictureToScale.setScale(scale[0],
                                scale[1]);
    }

    private static double[] getScaleFactor(final double width,
                                           final double height,
                                           final double targetWidth,
                                           final double targetHeight) {
        return new double[]{
                width > 0 ? targetWidth / width : 1,
                height > 0 ? targetHeight / height : 1};
    }
}