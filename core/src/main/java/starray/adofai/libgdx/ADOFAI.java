package starray.adofai.libgdx;

import com.alibaba.fastjson2.JSONObject;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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

import starray.adofai.AudioMerger;
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
    int currentTileIndex;
    boolean inited = false;

    double bpm;

    private BitmapFont font;
    private SpriteBatch hudBatch;

    private float cameraSpeed = 0.25f;
    private float fps;
    private float elapsedTime;
    private int frameCount;

    boolean isPaused = false;

    boolean dynamicCameraSpeed;
    boolean showBPM;

    String tilesProgress = "";
    private Thread generateTilesThread;

    private ShaderProgram shader;

    public float timer;
    //endregion
    //region 生命周期
    @Override
    public void create() {
        currentTileIndex = 0;
        bpm = level.getBPM();
        // 初始化着色器
        cameraSpeed = Tools.calculateSpeed((float)bpm);
        sound = Gdx.audio.newSound(Gdx.files.internal("kick.wav"));
        shader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl").readString(),
            Gdx.files.internal("shaders/fragment.glsl").readString());
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader编译错误: " + shader.getLog());
        }
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Tools.getScreenCenter().x, Tools.getScreenCenter().y);
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>正在计算，进度创建轨道中";
        font = new FreeTypeFontGenerator(Gdx.files.internal("SourceHanSansSC-Normal.otf")).generateFont(parameter);
        hudBatch = new SpriteBatch();

        camera.zoom = 5f;
        bluePlanet = new Planet(Color.BLUE);
        bluePlanet.setSubPlanet(redPlanet);
        generateConnectedTiles();
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
        timer += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(Color.BLACK);
        shader.bind();
        updateFPS();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FPS: ").append(fps).append("\n");
        float x = 20;  // 右侧留100像素空间
        float y = Gdx.graphics.getHeight() - 20; // 顶部留20像素空间
        if (generateTilesThread.isAlive()) {
            stringBuilder
                .append(tilesProgress);
            drawText(stringBuilder.toString(), x, y);
            return;
        }
        if (currentTile != null && cameraSpeed != 1) {
            camera.position.lerp(new Vector3(currentTile.getPosition(), 0), cameraSpeed);
        } else if (cameraSpeed == 1f) {
            camera.position.set(currentTile.getPosition(), 0);
        }
        shader.setUniformMatrix("u_proj", camera.combined);
        for (Tile tile : reverseTiles) {
            // 检查是否在摄像机视锥体内
            if (camera.frustum.pointInFrustum(new Vector3(tile.getPosition().x, tile.getPosition().y, 0)) && !tile.isHitEd()) {
                tile.render(shader);
            }
        }
        bluePlanet.render();
        keyEvent();
        camera.update();
        if (showBPM) {
            stringBuilder.append("当前BPM: ").append(bpm).append("\n");
        }
        stringBuilder.append(String.format("当前轨道: %d/%d\n", currentTileIndex, tiles.size() - 1));
        drawText(stringBuilder.toString(), x, y);
    }

    @Override
    public void dispose() {
        for (Tile tile : tiles) {
            tile.dispose();
        }
        shader.dispose();
        tiles.clear();
        reverseTiles.clear();
        bpmList.clear();
    }

    //endregion
    //region 方法体
    public ADOFAI(Level level, boolean dynamicCameraSpeed, boolean showBPM) {
        this.level = level;
        new Thread(() -> {
            try {
                bpmList = LevelUtils.getNoteTimes(level);
                AudioMerger.export(Tools.getOrExportResources("kick.wav"), bpmList, level.getCurrentLevelDir() + File.separator + "HitSounds.wav");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
        this.dynamicCameraSpeed = dynamicCameraSpeed;
        this.showBPM = showBPM;
    }

    private void drawText(String text, float x, float y) {
        hudBatch.begin();
        font.draw(hudBatch, text, x, y);
        hudBatch.end();
    }

    private void updateFPS() {
        elapsedTime += Gdx.graphics.getDeltaTime();
        frameCount++;

        if (elapsedTime >= 0.25f) { // 每0.25秒更新一次
            fps = frameCount / elapsedTime;
            frameCount = 0;
            elapsedTime = 0;
        }
    }
/*
    private void calculateBPM() {
        double bpm = level.getBPM();
        speedList = new double[level.getCharts().size()];
        List<JSONObject> events = level.getAllEvents("SetSpeed");
        for (int i = 0; i < level.getCharts().size(); i++) {
            for (JSONObject event : events) {
                if (event.getIntValue("floor") == i && event.get("speedType").equals("Multiplier")) {
                    bpm *= event.getDoubleValue("bpmMultiplier");
                } else {
                    bpm = event.getDoubleValue("beatsPerMinute");
                }
            }
            getSpeedProgress = String.format("计算速度中,当前：%d/%d", i, level.getCharts().size());
            speedList[i] = bpm;
        }
    }*/

    private void generateConnectedTiles() {
        generateTilesThread = new Thread(() -> {
            try {
                if (bpmList.isEmpty()) bpmList = LevelUtils.getNoteTimes(level);
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
                List<JSONObject> jsonObjects = level.getAllEvents("PositionTrack");
                for (int i = 0; i <= anglesArray.length; i++) {
                    float angle1 = (i == anglesArray.length) ?
                        anglesArray[i - 1] :
                        anglesArray[i];
                    float angle2 = (i == 0) ? 0 : anglesArray[i - 1];
                    Vector2 pos = new Vector2(Tile.length * 2 * MathUtils.cosDeg(angle1), Tile.length * 2 * MathUtils.sinDeg(angle1));
                    if (anglesArray.length < 100_0000) {
                        for (JSONObject jsonObject : jsonObjects) {
                            if (jsonObject.getIntValue("floor") == i && jsonObject.containsKey("positionOffset") && !jsonObject.getBoolean("editorOnly")) {
                                Vector2 position = new Vector2(jsonObject.getJSONArray("positionOffset").getFloatValue(0), jsonObject.getJSONArray("positionOffset").getFloatValue(1));
                                startPos.add(position.x * Tile.length * 2, position.y * Tile.length * 2);
                            }
                        }
                    }
                    Tile tile = new Tile(angle1, angle2 - 180, new Vector2(startPos));
                    if (i == angles.size()) {
                        tile.setIsMidspin(false);
                    } else {
                        tile.setIsMidspin(midSpins.get(i));
                    }
                    Gdx.app.postRunnable(tile::createMesh);
                    startPos.add(pos);
                    tiles.add(tile);
                    tilesProgress = String.format("创建轨道中，进度:%d/%d", i, angles.size());
                }
                for (int i = 0; i < tiles.size(); i++) {
                    Tile tile = tiles.get(i);
                    if (i < tiles.size() - 1) {
                        tile.setNext(tiles.get(i + 1));
                    }
                    if (i > 0) {
                        tile.setPrev(tiles.get(i - 1));
                    }
                }
                reverseTiles = new ArrayList<>(tiles);
                Collections.reverse(reverseTiles);
                currentTile = tiles.get(0);
                bluePlanet.move(currentTile.getPosition());
            } catch (Exception e) {
                Tools.log("傻逼，对不起，你的应用发生了: " + e.getMessage() + """

                    某些文件可能他妈的非常傻逼，所以不支持，滚吧
                    """ + Tools.getStackTrace(e));
            }
        });
        generateTilesThread.start();
    }

    public void keyEvent() {
        if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Keys.LEFT)) {
            camera.position.set(new Vector3(Tools.getScreenCenter().x, Tools.getScreenCenter().y, 0));
        } else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Keys.RIGHT)) {
            camera.position.set(new Vector3(tiles.get(tiles.size() - 1).getPosition(), 0));
        } else if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isTouched()) && !isStarted) {
            if (generateTilesThread.isAlive()) {
                return;
            }
            Music hitSound = null;
            if (Gdx.app.getType() != Application.ApplicationType.Android) {
                hitSound = Gdx.audio.newMusic(Gdx.files.absolute(level.getCurrentLevelDir() + File.separator + "HitSounds.wav"));
                musics.add(hitSound);
            }

            Music music = null;
            try {
                music = Gdx.audio.newMusic(Gdx.files.absolute(level.getMusicPath()));
            } catch (Exception e) {
                Tools.log(new UnsupportedEncodingException("不支持或无效的音乐").initCause(e));
            }
            Music finalMusic = music;
            new Thread(() -> {
                musics.add(finalMusic);
                if (finalMusic != null) {
                    finalMusic.play();
                }
            }).start();
            final var h = hitSound;
            if (!Tools.isAndroid()) {
                hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 50, () -> {
                    if (h != null) {
                        h.play();
                    }
                }, 0));
                hitsoundThread.start();
            }
            gameThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 50, this::start, 0));
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
        try {
            currentTile = tiles.get(0);
            double start = Tools.currentTime();
            int events = 0;
            while (events < bpmList.size() - 1) {
                if (isPaused) break;
                double cur = Tools.currentTime();
                double timeMilliseconds = (cur - start) + bpmList.get(0);
                //大于15000BPM
                while (events < bpmList.size() - 1 && bpmList.get(events) <= timeMilliseconds) {
                    //根据bpm计算延迟
                    Gdx.app.postRunnable(this::moveTiles);
                    events++;
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
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
        gameThread = new Thread(() -> Tools.sleepRun(level.getOffset(), this::start, 5));
        hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset(), musics.get(0)::play, 4));
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
        if (currentTileIndex >= 2 && currentTile.getPrevTile() != null && currentTile.getPrevTile().getPrevTile() != null) {
            currentTile.getPrevTile().getPrevTile().setHitEd(true);
        }

        if (Tools.isAndroid() && fps > 30) {
            new Thread(() -> {
                try {
                    sound.play();
                } catch (Exception ignored) {
                }
            }
            ).start();
        }

        if (fps > 90 && (dynamicCameraSpeed || showBPM) && level.hasEvent(currentTileIndex, "SetSpeed")) {
            var event = level.getEvents(currentTileIndex, "SetSpeed").get(0);
            if (event.get("speedType").equals("Multiplier")) {
                bpm *= event.getDoubleValue("bpmMultiplier");
            } else {
                bpm = event.getDoubleValue("beatsPerMinute");
            }
            if (dynamicCameraSpeed) cameraSpeed = Tools.calculateSpeed((float) bpm);
        }
        bluePlanet.move(currentTile.getPosition());
    }
    //endregion
}
