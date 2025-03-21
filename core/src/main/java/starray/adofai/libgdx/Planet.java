package starray.adofai.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Planet {

    private float bpm;

    private Planet subPlanet;

    private final Color color;

    private Tile currentTile;

    private Vector2 position;

    private boolean isCW;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public Planet(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setBpm(float bpm) {
        this.bpm = bpm;
    }

    public void reverse() {
        isCW = !isCW;
    }

    public void move(Vector2 position) {
        this.position = position;
    }

    public void setSubPlanet(Planet subPlanet) {
        this.subPlanet = subPlanet;
    }

    public void render(){
        if (position != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(ADOFAI.camera.combined);
            shapeRenderer.setColor(color);
            shapeRenderer.circle(position.x, position.y, Tile.width);
            shapeRenderer.end();
        }
    }
}
