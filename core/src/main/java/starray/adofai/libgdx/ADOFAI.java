package starray.adofai.libgdx;

import com.alibaba.fastjson2.JSONObject;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import starray.adofai.LevelUtils;


public class ADOFAI extends ApplicationAdapter {

    //region 全局变量
    public static OrthographicCamera camera;
    private final boolean disablePlanet;
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

    private BitmapFont font;
    private SpriteBatch hudBatch;
    private float cameraSpeed = 0.25f;
    private float fps;
    private float elapsedTime;
    private int frameCount;
    boolean isPaused = false;
    String tilesProgress = "";
    private Thread generateTilesThread;
    private ShaderProgram shader;
    public float timer;
    //endregion

    //region 游戏控制
    private Planet currPlanet;
    private Planet lastPlanet;
    private float angle;
    private float rotationSpeed;
    //endregion

    //region 图标
    private Texture swirlRed;
    private Texture speedUp;
    private Texture speedDown;
    //endregion

    //region 生命周期
    @Override
    public void create() {
        currentTileIndex = 0;
        if (!Tools.isAndroid()) {
           Thread thread = new Thread(() -> {
                try {
                    bpmList = LevelUtils.getNoteTimes(level);
                    AudioMerger.export(Tools.getOrExportResources("kick.wav"), bpmList, level.getCurrentLevelDir() + File.separator + "HitSounds.wav");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
           thread.start();
            /*try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
        }
        swirlRed = new Texture("texture/swirl_red.png");
        speedUp = new Texture("texture/tile_rabbit_light_new0.png");
        speedDown = new Texture("texture/tile_snail_light_new0.png");
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
        parameter.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>正在计算，设置事件当前进度创建轨道中";
        font = new FreeTypeFontGenerator(Gdx.files.internal("SourceHanSansSC-Normal.otf")).generateFont(parameter);
        hudBatch = new SpriteBatch();

        camera.zoom = 5f;
        bluePlanet = new Planet(Color.BLUE,true);
        redPlanet = new Planet(Color.RED,true);
        currPlanet = bluePlanet;
        lastPlanet = redPlanet;
        generateConnectedTiles();
        // 设置输入处理器
        inited = true;
    }

    @Override
    public void render() {
        timer += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(Color.BLACK);
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

        for (Tile tile : reverseTiles) {
            // 检查是否在摄像机视锥体内
            if (camera.frustum.pointInFrustum(new Vector3(tile.getPosition().x, tile.getPosition().y, 0)) && !tile.isHitEd()) {
                tile.render();
            }
        }
        if (!disablePlanet) {
            rotationSpeed = ((currentTile.isCW ? -1 : 1) * (currentTile.bpm / 60f) * 180 * Gdx.graphics.getDeltaTime());
            angle += rotationSpeed;
            if (angle >= 360) angle = 0;

            lastPlanet.update(Gdx.graphics.getDeltaTime());
            currPlanet.update(Gdx.graphics.getDeltaTime());
            lastPlanet.render();
            currPlanet.render();
        }
        Vector2 position = new Vector2(MathUtils.cosDeg(angle) * Tile.length * 2, MathUtils.sinDeg(angle) * Tile.length * 2);
        lastPlanet.setPosition(new Vector2(position).add(currentTile.getPosition()));
        keyEvent();
        camera.update();
        stringBuilder.append("当前BPM: ").append(currentTile.bpm).append("\n");
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
    // 构造函数，用于初始化ADOFAI对象
    public ADOFAI(Level level, boolean disablePlanet) {
        // 初始化level属性
        this.level = level;
        // 初始化disablePlanet属性
        this.disablePlanet = disablePlanet;
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

                boolean isCW = true;
                for (int i = 0; i <= anglesArray.length; i++) {
                    float angle1 = (i == anglesArray.length) ?
                        anglesArray[i - 1] :
                        anglesArray[i];
                    float angle2 = (i == 0) ? 0 : anglesArray[i - 1];
                    Vector2 pos = new Vector2(Tile.length * 2 * MathUtils.cosDeg(angle1), Tile.length * 2 * MathUtils.sinDeg(angle1));
                    if (anglesArray.length < 10_0000) {
                        for (JSONObject jsonObject : jsonObjects) {
                            if (jsonObject.getIntValue("floor") == i && jsonObject.containsKey("positionOffset") && !jsonObject.getBoolean("editorOnly")) {
                                Vector2 position = new Vector2(jsonObject.getJSONArray("positionOffset").getFloatValue(0), jsonObject.getJSONArray("positionOffset").getFloatValue(1));
                                startPos.add(position.x * Tile.length * 2, position.y * Tile.length * 2);
                            }
                        }
                    }
                    Tile tile = new Tile(angle1, angle2 - 180, new Vector2(startPos), shader);
                    if (i == angles.size()) {
                        tile.setIsMidspin(false);
                    } else {
                        tile.setIsMidspin(midSpins.get(i));
                    }
                    tile.angle = i == anglesArray.length ? anglesArray[i - 1] + 180 : anglesArray[i];
                    tile.isCW = isCW;
                    Gdx.app.postRunnable(tile::createMesh);
                    startPos.add(pos);
                    tiles.add(tile);
                    tilesProgress = String.format("创建轨道中，进度:%d/%d", i, angles.size());
                }
                double bpm = level.getBPM();
                double lastbpm;
                for (int i = 0; i < tiles.size(); i++) {
                    Tile tile = tiles.get(i);
                    if (!disablePlanet && (level.hasEvent(i, "SetSpeed") || level.hasEvent(i, "Twirl"))) {
                        if (level.hasEvent(i, "Twirl")) {
                            JSONObject twirl = level.getEvents(i, "Twirl").get(0);
                            if (twirl.getIntValue("floor") == i) {
                                isCW = !isCW;
                                if (i < tiles.size() -1){
                                    tile.setIcon(swirlRed);
                                }
                            }
                        }
                        if (level.hasEvent(i, "SetSpeed")) {
                            lastbpm = bpm;
                            JSONObject speed = level.getEvents(i, "SetSpeed").get(0);
                            if (speed.get("speedType").equals("Multiplier")) {
                                bpm *= speed.getDoubleValue("bpmMultiplier");
                            } else {
                                bpm = speed.getDoubleValue("beatsPerMinute");
                            }
                            final Texture speedIcon = bpm < lastbpm ? speedDown : speedUp;
                            tile.setIcon(speedIcon);
                        }
                        tilesProgress = String.format("正在设置事件，进度:%d/%d", i, tiles.size());
                    }
                    tile.bpm = (float) bpm;
                    tile.isCW = isCW;

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
                cameraSpeed = Tools.calculateSpeed(currentTile.bpm);
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
            for (Tile tile: tiles) {
                tile.initIcon();
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
                    finalMusic.setVolume(0.5f);
                    finalMusic.play();
                }
            }).start();
            final var h = hitSound;
            if (!Tools.isAndroid()) {
                hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 5, () -> {
                    if (h != null) {
                        h.play();
                    }
                }, 0));
                hitsoundThread.start();
            }
            gameThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 5, this::start, 0));
            gameThread.start();
            isStarted = true;
        } else if (Gdx.input.isKeyPressed(Keys.R) || Gdx.input.isTouched(4)) {
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
        if (!Tools.isAndroid()) hitsoundThread.interrupt();
        for (Music music : musics) {
            music.stop();
        }
        for (Tile tile : tiles) {
            tile.setHitEd(false);
        }
        isPaused = false;
        Thread.sleep(1000);
        gameThread = new Thread(() -> Tools.sleepRun(level.getOffset(), this::start, 5));
        gameThread.start();
        if (!Tools.isAndroid())
            hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset(), musics.get(0)::play, 4));
        if (!Tools.isAndroid()) hitsoundThread.start();
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
        if (!currentTile.getPrevTile().isMidspin() && !disablePlanet) {
            Planet temp = currPlanet;
            currPlanet = lastPlanet;
            lastPlanet = temp;
        }

        cameraSpeed = Tools.calculateSpeed(currentTile.bpm);
        angle = Tile.Game.fmod(currentTile.getPrevTile().angle + 180, 360);
        if (!disablePlanet) currPlanet.move(currentTile.getPosition());
    }
    //endregion
}
