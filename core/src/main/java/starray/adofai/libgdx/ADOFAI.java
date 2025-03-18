package starray.adofai.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import starray.adofai.Event.LevelEvent.PositionTrack;
import starray.adofai.Level;
import starray.adofai.LevelNotFoundException;
import starray.adofai.LevelUtils;

public class ADOFAI extends ApplicationAdapter {

    //region 全局变量
    public static OrthographicCamera camera;
    List<Tile> tiles = new ArrayList<>();
    List<Tile> reverseTiles = new ArrayList<>();
    List<Music> musics = new ArrayList<>();
    Thread gameThread;
    Thread hitsoundThread;
    List<Double> bpmList = new ArrayList<>();
    Tile currentTile;
    Level level;
    Sound sound;
    boolean isStarted;
    Planet bluePlanet;
    Planet redPlanet;
    Planet greenPlanet;
    boolean enableGreen;
    int currentTileIndex;
    double[] speedList;
    boolean inited = false;

    private float cameraSpeed = 0.1f;

    boolean isPaused = false;

    private ShaderProgram shader;
    private BitmapFont font; // 字体对象
    private SpriteBatch batch;

    //endregion
    //region 生命周期
    @Override
    public void create() {
        // 初始化着色器
        sound = Gdx.audio.newSound(Gdx.files.internal("kick.wav"));
        shader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl").readString(),
            Gdx.files.internal("shaders/fragment.glsl").readString());

        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader编译错误: " + shader.getLog());
        }
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Tools.getScreenCenter().x, Tools.getScreenCenter().y);

        speedList = level.getSpeedList();
        camera.zoom = 3.5f;
        bluePlanet = new Planet(Color.BLUE);

        bluePlanet.setSubPlanet(redPlanet);

        generateConnectedTiles();
        bluePlanet.move(currentTile.getPosition());
        // 设置输入处理器
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                // 拖动时移动摄像头
                float deltaX = -Gdx.input.getDeltaX() * (camera.zoom / 2);// X轴反向
                float deltaY = Gdx.input.getDeltaY() * (camera.zoom / 2);
                camera.translate(deltaX, deltaY);
                camera.update();
                return true;
            }
        });
        inited = true;
    }

    @Override
    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        ScreenUtils.clear(Color.GRAY);
        shader.bind();
        if (currentTile != null) {
            camera.position.lerp(new Vector3(currentTile.getPosition(), 0), cameraSpeed);
        }
        shader.setUniformMatrix("u_proj", camera.combined);
        for (Tile tile : reverseTiles) {
            Vector3 tilePos = new Vector3(tile.getPosition().x, tile.getPosition().y, 0);
            // 检查是否在摄像机视锥体内
            if (camera.frustum.pointInFrustum(tilePos) && !tile.isHitEd()) {
                tile.render(shader);
            }
        }
        bluePlanet.render();
        keyEvent();
        camera.update();
    }

    @Override
    public void dispose() {
        for (Tile tile : tiles) {
            tile.dispose();
        }
        shader.dispose();
    }

    //endregion
    //region 方法体
    public ADOFAI(String levelPath) throws LevelNotFoundException {
        level = Level.readLevelFile(levelPath);
    }

    private void generateConnectedTiles() {
        try {
            if (!tiles.isEmpty()) return;
            final List<Float> angles = level.getCharts();
            Float[] anglesArray = angles.toArray(new Float[0]);
            List<Boolean> midSpins = new ArrayList<>();

            for (int i = 0; i < anglesArray.length; i++) {
                midSpins.add(anglesArray[i] == 999);
                if (anglesArray[i] == 999) {
                    anglesArray[i] = anglesArray[i - 1] + 180;
                }
            }

            Vector2 startPos = new Vector2(Tools.getScreenCenter());
            boolean isCW = true;
            System.out.println("20");

            for (int i = 0; i <= anglesArray.length; i++) {
                float angle1 = (i == anglesArray.length) ?
                    anglesArray[i - 1] :
                    anglesArray[i];
                float angle2 = (i == 0) ? 0 : anglesArray[i - 1];
                Vector2 pos = new Vector2(Tile.length * 2 * MathUtils.cosDeg(angle1), Tile.length * 2 * MathUtils.sinDeg(angle1));
                if (level.hasEvent(i, PositionTrack.class.getSimpleName())) {
                    JSONObject jsonObject = level.getEvents(i, PositionTrack.class.getSimpleName()).get(0);
                    if (jsonObject.has("positionOffset") && !jsonObject.getBoolean("editorOnly")) {
                        Vector2 position = new Vector2((float) jsonObject.getJSONArray("positionOffset").getDouble(0), (float) jsonObject.getJSONArray("positionOffset").getDouble(1));
                        startPos.add(position.x * Tile.length * 2, position.y * Tile.length * 2);
                    }
                }
                Tile tile = new Tile(angle1, angle2 - 180, new Vector2(startPos));
                if (i == angles.size()) {
                    tile.setIsMidspin(false);
                } else {
                    tile.setIsMidspin(midSpins.get(i));
                }
                System.out.printf("%d/%d \n", i, angles.size());
                startPos.add(pos);
                tiles.add(tile);
            }
            System.out.println("40");
            for (int i = 0; i < tiles.size(); i++) {
                Tile tile = tiles.get(i);
                if (i < tiles.size() - 1) {
                    tile.setNext(tiles.get(i + 1));
                }
                if (i > 0) {
                    tile.setPrev(tiles.get(i - 1));
                }
            }
            System.out.println("100");
            reverseTiles = new ArrayList<>(tiles);
            Collections.reverse(reverseTiles);
            currentTile = tiles.get(0);
        } catch (Exception e) {
            Tools.log("傻逼，对不起，你的应用发生了: " + e.getMessage() + """

                某些文件可能他妈的非常傻逼，所以不支持，滚吧
                """ + Tools.getStackTrace(e));
        }
    }

    public void keyEvent() {
        if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Keys.LEFT)) {
            camera.position.set(new Vector3(Tools.getScreenCenter().x, Tools.getScreenCenter().y, 0));
        } else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Keys.RIGHT)) {
            camera.position.set(new Vector3(tiles.get(tiles.size() - 1).getPosition(), 0));
        } else if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isTouched()) && !isStarted) {
            bpmList = LevelUtils.getNoteTimes(level);
            Music hitSound = null;
            if (Gdx.app.getType() != Application.ApplicationType.Android) {
                hitSound = Gdx.audio.newMusic(Gdx.files.absolute(level.getCurrentLevelDir() + File.separator + "HitSounds.wav"));
                musics.add(hitSound);
            }

            try {
                if (level.getMusicPath() != null) {
                    Music music = Gdx.audio.newMusic(Gdx.files.absolute(level.getMusicPath()));
                    musics.add(music);
                    music.play();
                }
            } catch (Exception e) {
                Tools.log(new UnsupportedEncodingException("不支持的音乐").initCause(e));
            }
            final var h = hitSound;
            if (!Tools.isAndroid()) {
                hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset(), new Runnable() {
                    @Override
                    public void run() {
                        if (h != null) {
                            h.play();
                        }
                    }
                }, 10));
                hitsoundThread.start();
            }
            gameThread = new Thread(() -> Tools.sleepRun(level.getOffset(), this::start, 9));
            gameThread.start();
            isStarted = true;
        } else if (Gdx.input.isKeyPressed(Keys.R)) {
            try {
                restart();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void start() {
        currentTile = tiles.get(0);
        double start = Tools.currentTime();
        int events = 0;
        while (events < bpmList.size() -1) {
            if (isPaused) break;
            double cur = Tools.currentTime();
            double timeMilliseconds = (cur - start) + bpmList.get(0);
            //大于15000BPM
            while (events < bpmList.size() -1 && bpmList.get(events) <= timeMilliseconds) {
                //根据bpm计算延迟
                Gdx.app.postRunnable(this::moveTiles);
                events++;
            }
        }
    }

    public void restart() throws InterruptedException {
        isPaused = true;
        currentTileIndex = 0;
        currentTile = tiles.get(currentTileIndex);
        bluePlanet.move(currentTile.getPosition());
        hitsoundThread.interrupt();
        for (Music music : musics) {
            music.stop();
        }
        for (Tile tile : tiles) {
            tile.setHitEd(false);
        }
        isPaused = false;
        Thread.sleep(1000);
        gameThread = new Thread(() -> Tools.sleepRun(level.getOffset(), this::start, 3));
        hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset(), musics.get(0)::play, 2));
        gameThread.start();
        hitsoundThread.start();
        if (musics.size() == 2) {
            musics.get(1).play();
        }
    }

    public void moveTiles() {
        if (currentTileIndex >= tiles.size() - 1) {
            return;
        }
        currentTileIndex++;
        if (currentTile.getNextTile() != null && currentTile.getNextTile().isMidspin()) {
            currentTile.getPrevTile().setHitEd(true);
            currentTile.setHitEd(true);
            currentTile.getNextTile().setHitEd(true);
            currentTileIndex++;
        }
        currentTile = tiles.get(currentTileIndex);
        cameraSpeed = Tools.calculateSpeed((float) speedList[currentTileIndex -1]);
        if (currentTileIndex >= 2) {
            currentTile.getPrevTile().getPrevTile().setHitEd(true);
        }
        if (Tools.isAndroid()) {
            sound.play();
        }
        bluePlanet.move(currentTile.getPosition());
    }
    //endregion
}
