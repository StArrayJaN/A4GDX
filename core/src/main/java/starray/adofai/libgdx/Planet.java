package starray.adofai.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Planet {
    private final Color color;
    private Vector2 position;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // 粒子系统相关属性
    private final boolean trailEnabled;
    private List<Particle> trailParticles = new ArrayList<>();
    private float minParticleDistance = 3.0f;
    private float particleLifetime = 0.5f;
    private Vector2 lastParticlePosition;
    private boolean firstUpdate = true;

    // 构造方法
    public Planet(Color color, boolean trailEnabled) {
        this.color = color;
        this.trailEnabled = trailEnabled;
    }

    public Planet(Color color) {
        this(color, true); // 默认启用拖尾
    }

    public void move(Vector2 position) {
        this.position = position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void update(float deltaTime) {
        if (!trailEnabled) return;

        updateParticles(deltaTime);
        generateParticles();
    }

    private void updateParticles(float deltaTime) {
        // 使用迭代器避免ConcurrentModificationException
        for (int i = trailParticles.size() - 1; i >= 0; i--) {
            Particle p = trailParticles.get(i);
            p.lifetime -= deltaTime;
            if (p.lifetime <= 0) {
                trailParticles.remove(i);
            }
        }
    }

    private void generateParticles() {
        if (position == null || !trailEnabled) return;

        if (firstUpdate) {
            spawnParticle(position);
            lastParticlePosition = new Vector2(position);
            firstUpdate = false;
            return;
        }

        float distance = position.dst(lastParticlePosition);

        if (distance >= minParticleDistance) {
            int particlesToSpawn = (int)(distance / minParticleDistance);
            Vector2 direction = new Vector2(position).sub(lastParticlePosition).nor();
            float spacing = distance / particlesToSpawn;

            for (int i = 1; i <= particlesToSpawn; i++) {
                Vector2 spawnPos = new Vector2(lastParticlePosition)
                    .mulAdd(direction, spacing * i);
                spawnParticle(spawnPos);
            }

            lastParticlePosition.set(position);
        }
    }

    private void spawnParticle(Vector2 position) {
        Particle p = new Particle();
        p.position = new Vector2(position);
        p.radius = Tile.width * 0.9f;
        p.color = new Color(color);
        p.lifetime = particleLifetime;
        p.initialLifetime = particleLifetime;
        trailParticles.add(p);
    }

    public void render() {
        if (position == null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(ADOFAI.camera.combined);

        if (trailEnabled) {
            for (Particle p : trailParticles) {
                float alpha = p.lifetime / p.initialLifetime;
                float sizeScale = (float)Math.sqrt(alpha);
                Color particleColor = new Color(p.color.r, p.color.g, p.color.b, alpha);

                shapeRenderer.setColor(particleColor);
                shapeRenderer.circle(p.position.x, p.position.y, p.radius * sizeScale);
            }
        }

        shapeRenderer.setColor(color);
        shapeRenderer.circle(position.x, position.y, Tile.width);

        shapeRenderer.end();
    }

    public boolean isTrailEnabled() {
        return trailEnabled;
    }

    private static class Particle {
        Vector2 position;
        float radius;
        Color color;
        float lifetime;
        float initialLifetime;
    }
}
