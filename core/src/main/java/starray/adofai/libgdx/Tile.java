package starray.adofai.libgdx;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import java.util.ArrayList;
import java.util.List;

public class Tile {

    private float startAngle;
    private float endAngle;
    private boolean isMidspin;
    private Mesh mesh;
    private Vector2 position;

    public static float width = 27.5f;
    public static float length = 50f;

    private Tile prevTile;
    private Tile nextTile;
    private float alpha = 1f;
    private float outline = 2f;

    private Texture icon;
    private SpriteBatch sprite;

    private boolean hited = false;

    private List<Event> events;

    public Tile(float startAngle, float endAngle, Vector2 pos) {
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        position = pos;
    }

    public void createMesh() {
        mesh = isMidspin ? CreateMidSpinMesh(startAngle) : CreateTileMesh(length, width);
    }

    public void setHitEd(boolean hited) {
        this.hited = hited;
    }

    public boolean isHitEd() {
        return hited;
    }

    public void setIcon(Texture icon) {
        sprite = new SpriteBatch((int)width);
        this.icon = icon;
    }

    public Tile setIsMidspin(boolean a) {
        isMidspin = a;
        return this;
    }

    public boolean isMidspin() {
        return isMidspin;
    }

    public Tile setNext(Tile tile) {
        this.nextTile = tile;
        return this;
    }

    public Tile setPrev(Tile tile) {
        this.prevTile = tile;
        return this;
    }

    public Tile setPosition(float x, float y) {
        position.set(x, y);
        return this;
    }

    public static void scale(float scale) {
        length *= scale;
        width *= scale;
    }

    public void render(ShaderProgram shader) {
        shader.setUniformMatrix("u_model", new Matrix4().translate(position.x, position.y, 0));
        shader.setUniformf("u_alpha", alpha);
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    public static class Game {
        public static void CreateCircle(Vector3 center, float r, Color c, List<Vector3> vertices, List<Integer> triangles, List<Color> colors, int resolution) {
            if (resolution <= 0) {
                resolution = 32; // Default value if not provided
            }

            int centerIndex = vertices.size();
            vertices.add(new Vector3(center));
            colors.add(new Color(c));

            for (int i = 0; i < resolution; i++) {
                float angle = 2f * (float)Math.PI * i / resolution;
                Vector3 vertex = new Vector3((float)Math.cos(angle) * r, (float)Math.sin(angle) * r, 0).add(center);
                vertices.add(vertex);
                colors.add(new Color(c));
            }

            for (int i = 1; i < resolution; i++) {
                triangles.add(centerIndex);
                triangles.add(centerIndex + i);
                triangles.add(centerIndex + i + 1);
            }

            // Closing the circle by connecting the last vertex to the first
            triangles.add(centerIndex);
            triangles.add(centerIndex + resolution);
            triangles.add(centerIndex + 1);
        }

        public static float fmod(float x, float y) {
            return (x >= 0) ?(x % y): (x % y + y);
        }
    }

    private Mesh CreateTileMesh(float length, float width) {
        float m11 = (float)Math.cos(startAngle / 180f * (float)Math.PI);
        float m12 = (float)Math.sin(startAngle / 180f * (float)Math.PI);
        float m21 = (float)Math.cos(endAngle / 180f * (float)Math.PI);
        float m22 = (float)Math.sin(endAngle / 180f * (float)Math.PI);

        List<Vector3> vertices = new ArrayList<>();
        List<Integer> triangles = new ArrayList<>();
        List<Color> colors = new ArrayList<Color>();

        float[] a = new float[2];

        if (Game.fmod(startAngle - endAngle, 360f) >= Game.fmod(endAngle - startAngle, 360f)) {
            a[0] = Game.fmod(startAngle, 360f) * (float)Math.PI / 180f;
            a[1] = a[0] + Game.fmod(endAngle - startAngle, 360f) * (float)Math.PI / 180f;
        } else {
            a[0] = Game.fmod(endAngle, 360f) * (float)Math.PI / 180f;
            a[1] = a[0] + Game.fmod(startAngle - endAngle, 360f) * (float)Math.PI / 180f;
        }
        float angle = a[1] - a[0];
        float mid = a[0] + angle / 2f;
        if (angle < 2.0943952f && angle > 0) {
            float x;
            if (angle < 0.08726646f) {
                x = 1f;
            } else if (angle < 0.5235988f) {
                x = MathUtils.lerp(1f, 0.83f, (float)Math.pow((angle - 0.08726646f) / 0.43633235f, 0.5f));
            } else if (angle < 0.7853982f) {
                x = MathUtils.lerp(0.83f, 0.77f, (float)Math.pow((angle - 0.5235988f) / 0.2617994f, 1f));
            } else if (angle < 1.5707964f) {
                x = MathUtils.lerp(0.77f, 0.15f, (float)Math.pow((angle - 0.7853982f) / 0.7853982f, 0.7f));
            } else {
                x = MathUtils.lerp(0.15f, 0f, (float)Math.pow((angle - 1.5707964f) / 0.5235988f, 0.5f));
            }
            float distance;
            float radius;
            if (x == 1f) {
                distance = 0f;
                radius = width;
            } else {
                radius = MathUtils.lerp(0f, width, x);
                distance = (width - radius) / (float)Math.sin(angle / 2f);

            }
            float circlex = -distance * (float)Math.cos(mid);
            float circley = -distance * (float)Math.sin(mid);
            width += outline;
            length += outline;
            radius += outline;
            Game.CreateCircle(new Vector3(circlex, circley, 0), radius, Color.BLACK, vertices, triangles, colors, 0);
            {
                int count = vertices.size();
                vertices.add(new Vector3(-(radius) * (float)Math.sin(a[1]) + circlex, (radius) * (float)Math.cos(a[1]) + circley, 0));
                vertices.add(new Vector3(circlex, circley, 0));
                vertices.add(new Vector3((radius) * (float)Math.sin(a[0]) + circlex, -(radius) * (float)Math.cos(a[0]) + circley, 0));
                vertices.add(new Vector3((width) * (float)Math.sin(a[0]), -(width) * (float)Math.cos(a[0]), 0));
                vertices.add(Vector3.Zero);
                vertices.add(new Vector3(-(width) * (float)Math.sin(a[1]), (width) * (float)Math.cos(a[1]), 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 5);
                triangles.add(count + 4);
                triangles.add(count + 1);
                triangles.add(count + 5);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count + 4);
                triangles.add(count + 1);
                triangles.add(count + 3);
                triangles.add(count + 4);
                for (int i = 0; i < 6; i++) colors.add(Color.BLACK);
            }
            {
                int count = vertices.size();
                vertices.add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0));
                vertices.add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0));
                vertices.add(new Vector3(-(width) * m12, (width) * m11, 0));
                vertices.add(new Vector3((width) * m12, -(width) * m11, 0));

                vertices.add(new Vector3((length) * m21 + (width) * m22, (length) * m22 - (width) * m21, 0));
                vertices.add(new Vector3((length) * m21 - (width) * m22, (length) * m22 + (width) * m21, 0));
                vertices.add(new Vector3(-(width) * m22, (width) * m21, 0));
                vertices.add(new Vector3((width) * m22, -(width) * m21, 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                triangles.add(count + 4);
                triangles.add(count + 5);
                triangles.add(count + 6);
                triangles.add(count + 6);
                triangles.add(count + 7);
                triangles.add(count + 4);
                for (int i = 0; i < 8; i++) colors.add(Color.BLACK);
            }
            width -= outline * 2f;
            length -= outline * 2f;
            radius -= outline * 2f;
            if (radius < 0) {
                radius = 0;
                circlex = -(width) / (float)Math.sin(angle / 2f) * (float)Math.cos(mid);
                circley = -(width) / (float)Math.sin(angle / 2f) * (float)Math.sin(mid);
            }
            Game.CreateCircle(new Vector3(circlex, circley, 0), radius, Color.WHITE, vertices, triangles, colors, 0);
            {
                int count = vertices.size();
                vertices.add(new Vector3(-(radius) * (float)Math.sin(a[1]) + circlex, (radius) * (float)Math.cos(a[1]) + circley, 0));
                vertices.add(new Vector3(circlex, circley, 0));
                vertices.add(new Vector3((radius) * (float)Math.sin(a[0]) + circlex, -(radius) * (float)Math.cos(a[0]) + circley, 0));
                vertices.add(new Vector3((width) * (float)Math.sin(a[0]), -(width) * (float)Math.cos(a[0]), 0));
                vertices.add(Vector3.Zero);
                vertices.add(new Vector3(-(width) * (float)Math.sin(a[1]), (width) * (float)Math.cos(a[1]), 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 5);
                triangles.add(count + 4);
                triangles.add(count + 1);
                triangles.add(count + 5);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count + 4);
                triangles.add(count + 1);
                triangles.add(count + 3);
                triangles.add(count + 4);
                for (int i = 0; i < 6; i++) colors.add(Color.WHITE);
            }
            {
                int count = vertices.size();
                vertices.add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0));
                vertices.add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0));
                vertices.add(new Vector3(-(width) * m12, (width) * m11, 0));
                vertices.add(new Vector3((width) * m12, -(width) * m11, 0));

                vertices.add(new Vector3((length) * m21 + (width) * m22, (length) * m22 - (width) * m21, 0));
                vertices.add(new Vector3((length) * m21 - (width) * m22, (length) * m22 + (width) * m21, 0));
                vertices.add(new Vector3(-(width) * m22, (width) * m21, 0));
                vertices.add(new Vector3((width) * m22, -(width) * m21, 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                triangles.add(count + 4);
                triangles.add(count + 5);
                triangles.add(count + 6);
                triangles.add(count + 6);
                triangles.add(count + 7);
                triangles.add(count + 4);
                for (int i = 0; i < 8; i++) colors.add(Color.WHITE);
            }

        } else if (angle > 0) {
            width += outline;
            length += outline;

            float circlex = -(width) / (float)Math.sin(angle / 2f) * (float)Math.cos(mid);
            float circley = -(width) / (float)Math.sin(angle / 2f) * (float)Math.sin(mid);

            {
                int count = 0;
                vertices.add(new Vector3(circlex, circley, 0));
                vertices.add(new Vector3((width) * (float)Math.sin(a[0]), -(width) * (float)Math.cos(a[0]), 0));
                vertices.add(Vector3.Zero);
                vertices.add(new Vector3(-(width) * (float)Math.sin(a[1]), (width) * (float)Math.cos(a[1]), 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                for (int i = 0; i < 4; i++) colors.add(Color.BLACK);
            }
            {
                int count = vertices.size();
                vertices.add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0));
                vertices.add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0));
                vertices.add(new Vector3(-(width) * m12, (width) * m11, 0));
                vertices.add(new Vector3((width) * m12, -(width) * m11, 0));

                vertices.add(new Vector3((length) * m21 + (width) * m22, (length) * m22 - (width) * m21, 0));
                vertices.add(new Vector3((length) * m21 - (width) * m22, (length) * m22 + (width) * m21, 0));
                vertices.add(new Vector3(-(width) * m22, (width) * m21, 0));
                vertices.add(new Vector3((width) * m22, -(width) * m21, 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                triangles.add(count + 4);
                triangles.add(count + 5);
                triangles.add(count + 6);
                triangles.add(count + 6);
                triangles.add(count + 7);
                triangles.add(count + 4);
                for (int i = 0; i < 8; i++) colors.add(Color.BLACK);
            }

            width -= outline * 2f;
            length -= outline * 2f;

            circlex = -(width) / (float)Math.sin(angle / 2f) * (float)Math.cos(mid);
            circley = -(width) / (float)Math.sin(angle / 2f) * (float)Math.sin(mid);

            {
                int count = vertices.size();
                vertices.add(new Vector3(circlex, circley, 0));
                vertices.add(new Vector3((width) * (float)Math.sin(a[0]), -(width) * (float)Math.cos(a[0]), 0));
                vertices.add(Vector3.Zero);
                vertices.add(new Vector3(-(width) * (float)Math.sin(a[1]), (width) * (float)Math.cos(a[1]), 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                for (int i = 0; i < 4; i++) colors.add(Color.WHITE);
            }
            {
                int count = vertices.size();
                vertices.add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0));
                vertices.add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0));
                vertices.add(new Vector3(-(width) * m12, (width) * m11, 0));
                vertices.add(new Vector3((width) * m12, -(width) * m11, 0));

                vertices.add(new Vector3((length) * m21 + (width) * m22, (length) * m22 - (width) * m21, 0));
                vertices.add(new Vector3((length) * m21 - (width) * m22, (length) * m22 + (width) * m21, 0));
                vertices.add(new Vector3(-(width) * m22, (width) * m21, 0));
                vertices.add(new Vector3((width) * m22, -(width) * m21, 0));
                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                triangles.add(count + 4);
                triangles.add(count + 5);
                triangles.add(count + 6);
                triangles.add(count + 6);
                triangles.add(count + 7);
                triangles.add(count + 4);
                for (int i = 0; i < 8; i++) colors.add(Color.WHITE);
            }
        } else {
            length = width;
            width += outline;
            length += outline;

            Vector3 midpoint = new Vector3(-m11 * 0.04f, -m12 * 0.04f, 0);
            Game.CreateCircle(midpoint, width, Color.BLACK, vertices, triangles, colors, 0);

            {
                int count = vertices.size();
                vertices.add(new Vector3(midpoint).add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3(-(width) * m12, (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3((width) * m12, -(width) * m11, 0)));

                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                for (int i = 0; i < 4; i++) colors.add(Color.BLACK);
            }

            width -= outline * 2f;
            length -= outline * 2f;


            Game.CreateCircle(midpoint, width, Color.WHITE, vertices, triangles, colors, 0);


            {
                int count = vertices.size();
                vertices.add(new Vector3(midpoint).add(new Vector3((length) * m11 + (width) * m12, (length) * m12 - (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3((length) * m11 - (width) * m12, (length) * m12 + (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3(-(width) * m12, (width) * m11, 0)));
                vertices.add(new Vector3(midpoint).add(new Vector3((width) * m12, -(width) * m11, 0)));

                triangles.add(count);
                triangles.add(count + 1);
                triangles.add(count + 2);
                triangles.add(count + 2);
                triangles.add(count + 3);
                triangles.add(count);
                for (int i = 0; i < 4; i++) colors.add(Color.WHITE);
            }
        }
        FloatArray vertexData = new FloatArray();
        for (int i = 0; i < vertices.size(); i++) {
            Vector3 pos = vertices.get(i);
            vertexData.add(pos.x, pos.y, pos.z);
            vertexData.add(colors.get(i).toFloatBits());
        }
        Mesh mesh = new Mesh(true,
                             vertices.size() * 3, triangles.size(),
                             new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                             new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color")
                             //new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord") // 新增UV
                             );
        //Log.e("顶点数据",String.format("vertices:%d,indices:%d",vertices.size(),triangles.size()));
        mesh.setVertices(vertexData.items);

        short[] intArray = new short[triangles.size()];
        for (int i = 0; i < triangles.size(); i++) {
            intArray[i] = triangles.get(i).shortValue();
        }
        mesh.setIndices(intArray);
        return mesh;
    }
    private Mesh CreateMidSpinMesh(float a1) {

        float width = Tile.width;
        float length = Tile.width;
        float m1 = MathUtils.cos(a1 / 180f * MathUtils.PI);
        float m2 = MathUtils.sin(a1 / 180f * MathUtils.PI);

        List<Vector3> vertices = new ArrayList<>();
        List<Integer> triangles = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        Vector3 midpoint = new Vector3(-m1 * 0.04f, -m2 * 0.04f, 0);
        width += outline;
        length += outline;
        {
            int count = 0;
            vertices.add(new Vector3(midpoint).add(new Vector3((length) * m1 + (width) * m2, (length) * m2 - (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3((length) * m1 - (width) * m2, (length) * m2 + (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-(width) * m2, (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3((width) * m2, -(width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-width * m1, -width * m2, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(width * m2, -width * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-width * m2, width * m1, 0)));
            triangles.add(count);
            triangles.add(count + 1);
            triangles.add(count + 2);
            triangles.add(count + 2);
            triangles.add(count + 3);
            triangles.add(count);
            triangles.add(count + 4);
            triangles.add(count + 5);
            triangles.add(count + 6);
            for (int i = 0; i < 7; i++) colors.add(Color.BLACK);
        }
        width -= outline * 2;
        length -= outline * 2;
        {
            int count = vertices.size();
            vertices.add(new Vector3(midpoint).add(new Vector3((length) * m1 + (width) * m2, (length) * m2 - (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3((length) * m1 - (width) * m2, (length) * m2 + (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-(width) * m2, (width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3((width) * m2, -(width) * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-width * m1, -width * m2, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(width * m2, -width * m1, 0)));
            vertices.add(new Vector3(midpoint).add(new Vector3(-width * m2, width * m1, 0)));
            triangles.add(count);
            triangles.add(count + 1);
            triangles.add(count + 2);
            triangles.add(count + 2);
            triangles.add(count + 3);
            triangles.add(count);
            triangles.add(count + 4);
            triangles.add(count + 5);
            triangles.add(count + 6);
            for (int i = 0; i < 7; i++) colors.add(Color.WHITE);
        }

        FloatArray vertexData = new FloatArray();
        for (int i = 0; i < vertices.size(); i++) {
            Vector3 pos = vertices.get(i);
            vertexData.add(pos.x, pos.y, pos.z);
            vertexData.add(colors.get(i).toFloatBits());
        }
        Mesh mesh = new Mesh(true,
                             vertices.size() * 3, triangles.size(),
                             new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                             new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color")
                             );
        mesh.setVertices(vertexData.items);

        short[] intArray = new short[triangles.size()];
        for (int i = 0; i < triangles.size(); i++) {
            intArray[i] = triangles.get(i).shortValue();
        }
        mesh.setIndices(intArray);
        return mesh;
    }

    public Tile getNextTile() {
        return nextTile;
    }

    public Tile getPrevTile() {
        return prevTile;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        prevTile = null;
        nextTile = null;
        mesh.dispose();
    }

    @Override
    public String toString() {
        return "Tile{" +
            "outline=" + outline +
            ", alpha=" + alpha +
            ", hasNextTile=" + (nextTile != null) +
            ", hasPrevTile=" + (prevTile != null) +
            ", position=" + position +
            ", isMidspin=" + isMidspin +
            ", endAngle=" + endAngle +
            ", startAngle=" + startAngle +
            '}';
    }
}
