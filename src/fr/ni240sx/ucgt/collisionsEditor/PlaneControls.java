package fr.ni240sx.ucgt.collisionsEditor;

import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.PlaneEquation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class PlaneControls extends HBox {

	private Button add = new Button("+");
	private Label planeName = new Label("No plane selected");
	private Spinner<Double> a = new Spinner<>(-10.000, 10.000, 0.000, 0.010);
	private Spinner<Double> b = new Spinner<>(-10.000, 10.000, 0.000, 0.010);
	private Spinner<Double> c = new Spinner<>(-10.000, 10.000, 0.000, 0.010);
	private Spinner<Double> d = new Spinner<>(-10.000, 10.000, 0.000, 0.010);
	private CheckBox sym = new CheckBox("Symmetry");
	
	private PlaneEquation plane = null;
	private PlaneEquation symPlane = null;
	
	private double ogColorR = 0.0;
	private double ogColorG = 0.0;
	private double ogColorB = 0.0;
	private double ogColorSymR = 0.0;
	private double ogColorSymG = 0.0;
	private double ogColorSymB = 0.0;
	
	public PlaneControls() {
		getChildren().addAll(planeName, add, a, b, c, d, sym);
		
		add.setDisable(true);
		a.setDisable(true);
		b.setDisable(true);
		c.setDisable(true);
		d.setDisable(true);
		
		a.setEditable(true);
		b.setEditable(true);
		c.setEditable(true);
		d.setEditable(true);
		
		a.setMaxWidth(100);
		b.setMaxWidth(100);
		c.setMaxWidth(100);
		d.setMaxWidth(100);
		
		sym.setSelected(true);
		
		a.setOnKeyPressed(e -> updatePlane());
		b.setOnKeyPressed(e -> updatePlane());
		c.setOnKeyPressed(e -> updatePlane());
		d.setOnKeyPressed(e -> updatePlane());
		a.setOnMousePressed(e -> updatePlane());
		b.setOnMousePressed(e -> updatePlane());
		c.setOnMousePressed(e -> updatePlane());
		d.setOnMousePressed(e -> updatePlane());
		a.setOnMouseReleased(e -> updatePlane());
		b.setOnMouseReleased(e -> updatePlane());
		c.setOnMouseReleased(e -> updatePlane());
		d.setOnMouseReleased(e -> updatePlane());
		
		add.setOnAction(e -> {
			if (plane != null) {
				PlaneEquation eq;
				plane.convexVerticeShape.PlaneEquations.add(eq = new PlaneEquation());
				eq.convexVerticeShape = plane.convexVerticeShape;
				ogColorR = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorR, 0.4);
				ogColorG = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorG, 0.4);
				ogColorB = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorB, 0.4);
				this.setPlane(eq);
			}
		});
		add.setTooltip(new Tooltip("Add a plane to the currently selected convex shape."));
	}
	
	public void updatePlane() {
		if (plane != null) {
			
			if (!Collisions.similarEnough(plane.a, 0.0f) && Collisions.similarEnough(a.getValue().floatValue(), 0.0f) && sym.isSelected() && symPlane != null) {
				plane.convexVerticeShape.PlaneEquations.remove(symPlane);
				symPlane = null;
			}
			if (Collisions.similarEnough(plane.a, 0.0f) && !Collisions.similarEnough(a.getValue().floatValue(), 0.0f) && sym.isSelected() && symPlane == null) {
				plane.convexVerticeShape.PlaneEquations.add(symPlane = new PlaneEquation());
				symPlane.convexVerticeShape = plane.convexVerticeShape;
				ogColorSymR = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorR, 0.4);
				ogColorSymG = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorG, 0.4);
				ogColorSymB = Collisions.randomizeColor(plane.convexVerticeShape.bound.colorB, 0.4);
			}
			
			plane.a = a.getValue().floatValue();
			plane.b = b.getValue().floatValue();
			plane.c = c.getValue().floatValue();
			plane.d = d.getValue().floatValue();
			
			if (symPlane != null && sym.isSelected()) {
				// symmetrical editing a = -a, if a=0 remove the duplicate plane, if a!=0 create a symmetrical plane
				symPlane.a = -a.getValue().floatValue();
				symPlane.b = b.getValue().floatValue();
				symPlane.c = c.getValue().floatValue();
				symPlane.d = d.getValue().floatValue();
			}
			plane.convexVerticeShape.updateShape();
		}
	}
	
	public void update() {
		if (plane != null) {
			planeName.setText("Plane #"+plane.convexVerticeShape.PlaneEquations.indexOf(plane));
			a.getValueFactory().setValue((double)plane.a);
			b.getValueFactory().setValue((double)plane.b);
			c.getValueFactory().setValue((double)plane.c);
			d.getValueFactory().setValue((double)plane.d);
			add.setDisable(false);
			a.setDisable(false);
			b.setDisable(false);
			c.setDisable(false);
			d.setDisable(false);
		} else {
			planeName.setText("No plane selected");
			a.getValueFactory().setValue(0.0);
			b.getValueFactory().setValue(0.0);
			c.getValueFactory().setValue(0.0);
			d.getValueFactory().setValue(0.0);

			add.setDisable(true);
			a.setDisable(true);
			b.setDisable(true);
			c.setDisable(true);
			d.setDisable(true);
		}
	}
	
	public void setPlane(PlaneEquation p) {
		if (plane != null) {
			plane.colorR = ogColorR;
			plane.colorG = ogColorG;
			plane.colorB = ogColorB;
		}
		if (symPlane != null) {
			symPlane.colorR = ogColorSymR;
			symPlane.colorG = ogColorSymG;
			symPlane.colorB = ogColorSymB;
		}
		symPlane = null;
		
		var oldPlane = plane;
		plane = p;
		if (p != null) {
			ogColorR = p.colorR;
			ogColorG = p.colorG;
			ogColorB = p.colorB;
			p.colorR = 1.0;
			p.colorG = 0.0;
			p.colorB = 0.0;
			if (sym.isSelected()) {
				for (var p2 : p.convexVerticeShape.PlaneEquations) {
					if (Collisions.similarEnough(p.a, -p2.a) && Collisions.similarEnough(p.b, p2.b) && Collisions.similarEnough(p.c, p2.c) && Collisions.similarEnough(p.d, p2.d) && p != p2) {
						symPlane = p2;
						ogColorSymR = p2.colorR;
						ogColorSymG = p2.colorG;
						ogColorSymB = p2.colorB;
						p2.colorR = 1.0;
						p2.colorG = 0.0;
						p2.colorB = 0.0;
						break;
					}
				}
			}
			p.convexVerticeShape.updateShape();
		}
		update();
		if (oldPlane != null) oldPlane.convexVerticeShape.updateShape();
	}

	
	public PlaneEquation getPlane() {
		return plane;
	}
	public PlaneEquation getPlaneSym() {
		return symPlane;
	}
}
