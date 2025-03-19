package starray.adofai.libgdx.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import starray.adofai.AudioMerger;
import starray.adofai.Level;
import starray.adofai.LevelUtils;
import starray.adofai.libgdx.ADOFAI;
import starray.adofai.libgdx.Tools;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher extends JFrame {

    private LastOpenManager lastOpenManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Lwjgl3Launcher::new);
    }

    public Lwjgl3Launcher() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lastOpenManager = LastOpenManager.getInstance();
        initGUI();
    }

    public void initGUI() {
        setTitle("A4GDX");
        setLayout(null);
        setMinimumSize(new Dimension(450, 150));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField levelPath = new JTextField();
        levelPath.setBounds(50, 20, 250, 30); // 设置文本框的宽度和高度
        levelPath.setText(lastOpenManager.getLastOpenFile());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("关卡文件", "adofai"));
        fileChooser.setCurrentDirectory(new File(lastOpenManager.getLastOpenFile()).getParentFile());
        fileChooser.setDialogTitle("选择关卡文件");
        fileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

        JButton selectFile = new JButton("选择文件");
        selectFile.setBounds(300, 20, 100, 30); // 设置按钮的宽度和高度
        selectFile.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                lastOpenManager.setLastOpenFile(fileChooser.getSelectedFile().getAbsolutePath());
                levelPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                lastOpenManager.save();

            }
        });

        JButton startButton = new JButton("开始");
        startButton.setBounds(50, 50, 100, 30); // 设置按钮的宽度和高度

        startButton.addActionListener(e -> {
            try {
                Level level = Level.readLevelFile(levelPath.getText());
                new Thread(() -> {
                    try {
                        AudioMerger.export(getOrExportResources("kick.wav"), LevelUtils.getNoteTimes(level), level.getCurrentLevelDir() + File.separator + "HitSounds.wav");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
                new Lwjgl3Application(new ADOFAI(levelPath.getText(),
                    Boolean.parseBoolean(lastOpenManager.getData("showBPM", "false")),
                    Boolean.parseBoolean(lastOpenManager.getData("dynamicCameraSpeed", "false"))), getDefaultConfiguration());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "发生错误\n" + Tools.getStackTrace(ex));
            }
        });

        JToggleButton showBPM = new JToggleButton("显示BPM");
        showBPM.setSelected(Boolean.parseBoolean(lastOpenManager.getData("showBPM", "false")));
        showBPM.setBounds(150, 50, 100, 30); // 设置按钮的宽度和高度
        showBPM.addActionListener(e -> {
            if (showBPM.isSelected()) {
                lastOpenManager.putData("showBPM", "true");
            } else {
                lastOpenManager.putData("showBPM", "false");
            }
            lastOpenManager.save();
        });

        JToggleButton dynamicCameraSpeed = new JToggleButton("动态摄像机速度");
        dynamicCameraSpeed.setSelected(Boolean.parseBoolean(lastOpenManager.getData("dynamicCameraSpeed", "false")));
        dynamicCameraSpeed.setBounds(250, 50, 150, 30); // 设置按钮的宽度和高度
        dynamicCameraSpeed.addActionListener(e -> {
            if (dynamicCameraSpeed.isSelected()) {
                lastOpenManager.putData("dynamicCameraSpeed", "true");
            } else {
                lastOpenManager.putData("dynamicCameraSpeed", "false");
            }
            lastOpenManager.save();
        });

        add(levelPath);
        add(selectFile);
        add(startButton);
        add(showBPM);
        add(dynamicCameraSpeed);

        pack(); // 自动调整窗口大小以适应组件
        setLocationRelativeTo(null); // 将窗口居中显示
        setVisible(true);
    }

    public static String getRuntimePath() {
        String path = Lwjgl3Launcher.class.getResource(Lwjgl3Launcher.class.getSimpleName() + ".class").getPath();
        return new File(path.split("!")[0].replace("file:/", "")).getParent();
    }

    public static String getOrExportResources(String path) throws IOException {
        String runtimePath = getRuntimePath();
        File file = new File(runtimePath, path);
        if (!file.exists()) {
            Files.write(file.toPath(), Lwjgl3Launcher.class.getResourceAsStream("/" + path).readAllBytes());
        }
        return file.getAbsolutePath();
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("A4GDX");
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(0);
        configuration.useVsync(false);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        configuration.setWindowedMode(dimension.width, dimension.height);
        configuration.setMaximized(true);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
