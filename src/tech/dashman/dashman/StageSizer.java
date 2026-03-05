// Copyright (c) 2017-2018 Flexpoint Tech Ltd. All rights reserved.
// adapted by needeka

package tech.dashman.dashman;

import javafx.application.Platform;
//import javafx.geometry.Rectangle2D;
//import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageSizer {
//    private static double MINIMUM_VISIBLE_WIDTH = 100;
//    private static double MINIMUM_VISIBLE_HEIGHT = 50;
//    private static double MARGIN = 50;

    private Boolean maximized = false;
    private Boolean hidden = false;
    private Double x = null;
    private Double y = null;
    private Double width = null;
    private Double height = null;

    private Boolean hideable = true;

    public void setStage(Stage stage) {
        // First, restore the size and position of the stage.
        resizeAndPosition(stage, () -> {
            // If the stage is not visible in any of the current screens, relocate it to the primary screen.
//            if (isWindowIsOutOfBounds(stage)) {
//                moveToPrimaryScreen(stage);
//            }
            // And now watch the stage to keep the properties updated.
            watchStage(stage);
        });
    }
    
    

    public Boolean getMaximized() {
		return maximized;
	}



	public void setMaximized(Boolean maximized) {
		this.maximized = maximized;
	}



	public Boolean getHidden() {
		return hidden;
	}



	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}



	public Double getX() {
		return x;
	}



	public void setX(Double x) {
		this.x = x;
	}



	public Double getY() {
		return y;
	}



	public void setY(Double y) {
		this.y = y;
	}



	public Double getWidth() {
		return width;
	}



	public void setWidth(Double width) {
		this.width = width;
	}



	public Double getHeight() {
		return height;
	}



	public void setHeight(Double height) {
		this.height = height;
	}



	public Boolean getHideable() {
		return hideable;
	}



	public void setHideable(Boolean hideable) {
		this.hideable = hideable;
	}



	private void resizeAndPosition(Stage stage, Runnable callback) {
        Platform.runLater(() -> {
            if (getHidden() != null && getHidden() && getHideable()) {
                stage.hide();
            }
            if (getX() != null) {
                stage.setX(getX());
            }
            if (getY() != null) {
                stage.setY(getY());
            }
            if (getWidth() != null) {
                stage.setWidth(getWidth());
            }
            if (getHeight() != null) {
                stage.setHeight(getHeight());
            }
            if (getMaximized() != null) {
                stage.setMaximized(getMaximized());
            }
            if (getHidden() == null || !getHidden() || !getHideable()) {
                stage.show();
            }

            new Thread(callback).start();
        });
    }

    public void setHidden(boolean value) {
        this.hidden = value;
    }

//    private boolean isWindowIsOutOfBounds(Stage stage) {
//        for (Screen screen : Screen.getScreens()) {
//            Rectangle2D bounds = screen.getVisualBounds();
//            if (stage.getX() + stage.getWidth() - MINIMUM_VISIBLE_WIDTH >= bounds.getMinX() &&
//                stage.getX() + MINIMUM_VISIBLE_WIDTH <= bounds.getMaxX() &&
//                bounds.getMinY() <= stage.getY() && // We want the title bar to always be visible.
//                stage.getY() + MINIMUM_VISIBLE_HEIGHT <= bounds.getMaxY()) {
//                return false;
//            }
//        }
//        return true;
//    }

//    private void moveToPrimaryScreen(Stage stage) {
//        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
//        stage.setX(bounds.getMinX() + MARGIN);
//        stage.setY(bounds.getMinY() + MARGIN);
//    }

    private void watchStage(Stage stage) {
        // Get the current values.
        setX(stage.getX());
        setY(stage.getY());
        setWidth(stage.getWidth());
        setHeight(stage.getHeight());
        setMaximized(stage.isMaximized());
        setHidden(!stage.isShowing());

        // Watch for future changes.
        stage.xProperty().addListener((observable, old, x) -> setX((Double) x));
        stage.yProperty().addListener((observable, old, y) -> setY((Double) y));
        stage.widthProperty().addListener((observable, old, width) -> setWidth((Double) width));
        stage.heightProperty().addListener((observable, old, height) -> setHeight((Double) height));
        stage.maximizedProperty().addListener((observable, old, maximized) -> setMaximized(maximized));
        stage.showingProperty().addListener(observable -> setHidden(!stage.isShowing())); // Using an invalidation instead of a change listener due to this weird behaviour: https://stackoverflow.com/questions/50280052/property-not-calling-change-listener-unless-theres-an-invalidation-listener-as
    }
}