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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import starray.adofai.AudioMerger;
import starray.adofai.level.Level;
import starray.adofai.level.LevelUtils;


public class ADOFAI extends ApplicationAdapter {

    //region 全局变量
    public static OrthographicCamera camera;
    private final boolean disablePlanet;
    private Event callBack;
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
    private float maxBpm;
    private double realBPM;
    boolean isPaused = false;
    String tilesProgress = "";
    private Thread generateTilesThread;
    private ShaderProgram shader;
    public float timer;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean hitSoundReady = false;
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
        Gdx.graphics.setContinuousRendering(true);
        currentTileIndex = 0;
        hitSoundReady = false;
        if (!Tools.isAndroid()) {
            executor.submit(() -> {
                try {
                    bpmList = LevelUtils.getNoteTimes(level);
                    AudioMerger.export(Tools.getOrExportResources("kick.wav"), bpmList, level.getCurrentLevelDir() + File.separator + "HitSounds.wav");
                    hitSoundReady = true;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } else {
            hitSoundReady = true;
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
        parameter.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>正在计算，设置事件最大当前进度创建轨道中";
        font = new FreeTypeFontGenerator(Gdx.files.internal("SourceHanSansSC-Normal.otf")).generateFont(parameter);
        hudBatch = new SpriteBatch();

        Sound sound1 = Gdx.audio.newSound(Gdx.files.internal("kick.wav"));
        Sound sound2 = Gdx.audio.newSound(Gdx.files.internal("kick.wav"));
        System.out.println(sound1.equals(sound2));
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
        if (currentTile != null) {
            if (!disablePlanet && !bpmList.isEmpty()) {
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
            stringBuilder.append("实际BPM: ").append(realBPM).append("\n");
            stringBuilder.append("最大BPM: ").append(maxBpm).append("\n");
            stringBuilder.append(String.format("当前轨道: %d/%d\n", currentTileIndex, tiles.size() - 1));
            drawText(stringBuilder.toString(), x, y);
        }
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
    public ADOFAI(Level level, boolean disablePlanet,Event callBack) {
        // 初始化level属性
        this.level = level;
        // 初始化disablePlanet属性
        this.disablePlanet = disablePlanet;
        this.callBack = callBack;
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
                // 事件预处理：为每个floor预先记录SetSpeed和Twirl事件
                int n = anglesArray.length + 1;
                Double[] setSpeedBpm = new Double[n];
                Double[] setSpeedMultiplier = new Double[n];
                Boolean[] setSpeedIsMultiplier = new Boolean[n];
                Boolean[] twirlAt = new Boolean[n];
                for (int i = 0; i < n; i++) {
                    setSpeedBpm[i] = null;
                    setSpeedMultiplier[i] = null;
                    setSpeedIsMultiplier[i] = null;
                    twirlAt[i] = false;
                }
                for (int a = 0; a < level.getEvents().size(); a++) {
                    JSONObject event = level.getEvents().getJSONObject(a);
                    int floor = event.getIntValue("floor");
                    String eventType = event.getString("eventType");
                    if (eventType.equals("SetSpeed")) {
                        if ("Multiplier".equals(event.getString("speedType"))) {
                            setSpeedMultiplier[floor] = event.getDouble("bpmMultiplier");
                            setSpeedIsMultiplier[floor] = true;
                        } else {
                            setSpeedBpm[floor] = event.getDouble("beatsPerMinute");
                            setSpeedIsMultiplier[floor] = false;
                        }
                    } else if (eventType.equals("Twirl")) {
                        twirlAt[floor] = true;
                    }
                }
                // 1. 并行批量创建Tile及其Mesh
                Tile[] tileArr = new Tile[n];
                Vector2[] posArr = new Vector2[n];
                float[] angle1Arr = new float[n];
                float[] angle2Arr = new float[n];
                for (int i = 0; i < n; i++) {
                    float angle1 = (i == anglesArray.length) ? anglesArray[i - 1] : anglesArray[i];
                    float angle2 = (i == 0) ? 0 : anglesArray[i - 1];
                    // 先判断当前floor是否有PositionTrack，若有则修正startPos
                    if (anglesArray.length < 10_000000) {
                        for (JSONObject jsonObject : jsonObjects) {
                            if (jsonObject.getIntValue("floor") == i && jsonObject.containsKey("positionOffset") && !jsonObject.getBoolean("editorOnly")) {
                                Vector2 position = new Vector2(jsonObject.getJSONArray("positionOffset").getFloatValue(0), jsonObject.getJSONArray("positionOffset").getFloatValue(1));
                                startPos = new Vector2(startPos).add(position.x * Tile.length * 2, position.y * Tile.length * 2);
                            }
                        }
                    }
                    Vector2 currPos = new Vector2(startPos);
                    posArr[i] = new Vector2(currPos);
                    angle1Arr[i] = angle1;
                    angle2Arr[i] = angle2;
                    Vector2 step = new Vector2(Tile.length * 2 * MathUtils.cosDeg(angle1), Tile.length * 2 * MathUtils.sinDeg(angle1));
                    startPos.add(step);
                }
                // 并行创建Tile和Mesh
                List<Callable<Tile>> tasks = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    final int idx = i;
                    tasks.add(() -> {
                        Tile tile = new Tile(angle1Arr[idx], angle2Arr[idx] - 180, posArr[idx], shader);
                        if (idx == angles.size()) {
                            tile.setIsMidspin(false);
                        } else {
                            tile.setIsMidspin(midSpins.get(idx));
                        }
                        tile.angle = idx == anglesArray.length ? anglesArray[idx - 1] + 180 : anglesArray[idx];
                        Gdx.app.postRunnable(tile::createMesh);
                        return tile;
                    });
                }
                List<Future<Tile>> futures = executor.invokeAll(tasks);
                for (int i = 0; i < n; i++) {
                    tileArr[i] = futures.get(i).get();
                }
                // 2. 串行设置事件、bpm、isCW、链表关系（直接查预处理数组）
                tiles.clear();
                double bpm = level.getBPM();
                double lastbpm;
                boolean isCW = true;
                for (int i = 0; i < tileArr.length; i++) {
                    Tile tile = tileArr[i];
                    // 事件预处理优化
                    if (!disablePlanet) {
                        if (twirlAt[i]) {
                            isCW = !isCW;
                            if (i < tileArr.length - 1) {
                                tile.setIcon(swirlRed);
                            }
                        }
                        if (setSpeedIsMultiplier[i] != null) {
                            lastbpm = bpm;
                            if (setSpeedIsMultiplier[i]) {
                                bpm *= setSpeedMultiplier[i];
                            } else {
                                bpm = setSpeedBpm[i];
                            }
                            final Texture speedIcon = bpm < lastbpm ? speedDown : speedUp;
                            tile.setIcon(speedIcon);
                        }
                        // 立即初始化图标
                        Gdx.app.postRunnable(() -> tile.initIcon());
                        tilesProgress = String.format("正在设置事件，进度:%d/%d", i, tileArr.length);
                    }
                    tile.bpm = (float) bpm;
                    tile.isCW = isCW;
                    if (i < tileArr.length - 1) {
                        tile.setNext(tileArr[i + 1]);
                    }
                    if (i > 0) {
                        tile.setPrev(tileArr[i - 1]);
                    }
                    tiles.add(tile);
                }
                reverseTiles = new ArrayList<>(tiles);
                Collections.reverse(reverseTiles);
                currentTile = tiles.get(0);
                cameraSpeed = Tools.calculateSpeed(currentTile.bpm);
                bluePlanet.move(currentTile.getPosition());
                if (callBack != null) {
                    callBack.onLoadDone(level);
                }
            } catch (InterruptedException | ExecutionException e) {
                Tools.log("轨道生成线程池异常: " + e.getMessage() + "\n" + Tools.getStackTrace(e));
            } catch (Exception e) {
                Tools.log("傻逼，对不起，你的应用发生了: " + e.getMessage() + "\n某些文件可能他妈的非常傻逼，所以不支持，滚吧\n" + Tools.getStackTrace(e));
            }
        });
        executor.submit(generateTilesThread);
    }

    public void keyEvent() {
        if (!hitSoundReady) return; // 未生成HitSounds禁止开始
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
            final var h = hitSound;


            Music music = null;
            try {
                music = Gdx.audio.newMusic(Gdx.files.absolute(level.getMusicPath()));
            } catch (Exception e) {
                Tools.log(new UnsupportedEncodingException("不支持或无效的音乐").initCause(e));
            }
            Music finalMusic = music;
            executor.submit(() -> {
                musics.add(finalMusic);
                if (finalMusic != null) {
                    finalMusic.setVolume(0.5f);
                    finalMusic.play();
                }
            });
            if (!Tools.isAndroid()) {
                hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 5, () -> {
                    if (h != null) {
                        h.play();
                    }
                }, 0));
                executor.submit(hitsoundThread);
            }

            gameThread = new Thread(() -> Tools.sleepRun(level.getOffset() + 5, this::start, 0));
            executor.submit(gameThread);
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
                    if (events >=1 ) realBPM = 60000 / (bpmList.get(events) - bpmList.get(events-1));
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
        currPlanet.move(currentTile.getPosition());
        lastPlanet.move(currentTile.getNextTile().getPosition());
        if (!Tools.isAndroid()) hitsoundThread.interrupt();
        for (Music music : musics) {
            if (music != null) music.stop();
        }
        for (Tile tile : tiles) {
            tile.setHitEd(false);
        }
        isPaused = false;
        Thread.sleep(1000);
        gameThread = new Thread(() -> Tools.sleepRun(level.getOffset(), this::start, 5));
        executor.submit(gameThread);
        if (!Tools.isAndroid())
            hitsoundThread = new Thread(() -> Tools.sleepRun(level.getOffset(), musics.get(0)::play, 4));
        if (!Tools.isAndroid()) executor.submit(hitsoundThread);
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
        maxBpm = (float) Math.max(realBPM, maxBpm);

        cameraSpeed = Tools.calculateSpeed(currentTile.bpm);
        angle = Tile.Game.fmod(currentTile.getPrevTile().angle + 180, 360);
        if (!disablePlanet) currPlanet.move(currentTile.getPosition());
    }
    //endregion
}
