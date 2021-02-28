package model;

import com.sun.istack.internal.NotNull;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import util.FXUtil;

public class TextBox {
    private double orgSceneX, orgSceneY, orgTranslateX, orgTranslateY;
    private Label label;
    private Line xLine, yLine;
    private Group group;
    private Pane parent;
    private DraggedCardData cardData;
    private TextData textData;

    public TextBox(TextData textData, Group group) {
        this.textData = textData;
        this.group = group;
        this.parent = (Pane) group.getParent();
        this.label = new Label(textData.getText());
        this.label.setFont(Font.font(textData.getFontFamily(), textData.getFontSize()));
        this.label.setTextFill(Color.web(textData.getHexColor()));
        this.label.setTranslateX(textData.getPosition().getX());
        this.label.setTranslateY(textData.getPosition().getY());
        loadConDetails();
    }

    public TextBox(String string, @NotNull Group root, @NotNull DraggedCardData cardData) {
        this.label = new Label(string);
        this.group = root;
        this.parent = (Pane) root.getParent();
        this.cardData = cardData;
        this.textData = new TextData(this);
        this.cardData.addText(textData);
        loadConDetails();
    }

    public TextData getTextData() {
        return textData;
    }

    private void loadConDetails() {
        group.getChildren().add(label);
        xLine = new Line(0, parent.getHeight() / 2, parent.getWidth(), parent.getHeight() / 2);
        xLine.setStroke(Color.RED);
        yLine = new Line(parent.getWidth() / 2, 0, parent.getWidth() / 2, parent.getHeight());
        yLine.setStroke(Color.RED);
        addListeners();
    }

    private void addListeners() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Change Font");
        MenuItem item2 = new MenuItem("Change Color");
        MenuItem item3 = new MenuItem("Delete");
        item3.setOnAction(e -> {
            group.getChildren().remove(label);
            cardData.removeText(textData);
        });
        item2.setOnAction(e -> {
            FXUtil.showColorDialog(label);
            textData.setHexColor(label);
        });
        item1.setOnAction(e -> {
            FXUtil.showFontDialog(label);
            textData.setFontFamily(label.getFont().getName());
            textData.setFontSize(label.getFont().getSize());
        });
        contextMenu.getItems().addAll(item1, item2, item3);
        label.setContextMenu(contextMenu);
        label.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                orgSceneX = e.getSceneX();
                orgSceneY = e.getSceneY();
                orgTranslateX = ((Label) (e.getSource())).getTranslateX();
                orgTranslateY = ((Label) (e.getSource())).getTranslateY();
            }
        });
        label.setOnMouseDragged(e -> {
            double offsetX = e.getSceneX() - orgSceneX;
            double offsetY = e.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            label.setTranslateX(newTranslateX);
            label.setTranslateY(newTranslateY);
            textData.getPosition().setX(newTranslateX);
            textData.getPosition().setY(newTranslateY);
            double xHalfScene = parent.getWidth() / 2.0;
            double yHalfScene = parent.getHeight() / 2.0;
            double xCenter = label.getTranslateX() + label.getWidth() / 2.0;
            double yCenter = label.getTranslateY() + label.getHeight() / 2.0;
            // Check if image is touching y-axis
            if (isNearAxis(xCenter, xHalfScene)) {
                label.setTranslateX(xHalfScene - (label.getWidth() / 2.0));
                if (!group.getChildren().contains(yLine)) {
                    group.getChildren().add(yLine);
                }
            } else {
                group.getChildren().remove(yLine);
            }

            // Check if image is touching x-axis
            if (isNearAxis(yCenter, yHalfScene)) {
                label.setTranslateY(yHalfScene - (label.getHeight() / 2.0));
                if (!group.getChildren().contains(xLine)) {
                    group.getChildren().add(xLine);
                }
            } else { // Remove the line if no longer on center with respect to Y
                group.getChildren().remove(xLine);
            }
        });
        label.setOnMouseReleased(e -> {
            group.getChildren().remove(xLine);
            group.getChildren().remove(yLine);
        });
    }

    public Label getLabel() {
        return label;
    }

    public boolean isNearAxis(double centerCoord, double sceneCoord) {
        return centerCoord <= sceneCoord + 15 && centerCoord >= sceneCoord - 15;
    }
}
