package starray.adofai.libgdx.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import starray.adofai.level.Level;
import starray.adofai.libgdx.ADOFAI;
import starray.adofai.libgdx.Event;
import starray.adofai.libgdx.Tools;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

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

    public static void sendMessage(String title,String message) throws AWTException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        tray.add(trayIcon);
        trayIcon.displayMessage(title,message, TrayIcon.MessageType.INFO);
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
        fileChooser.setFileFilter(new FileNameExtensionFilter("关卡文件", "adofai"));
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
                new Lwjgl3Application(new ADOFAI(level, lastOpenManager.getBoolean("disablePlanet", false), new Event() {
                    @Override
                    public void onLoadDone(Level level) {
                        try {
                            sendMessage("A4GDX",String.format("关卡:%s加载完成",new File(level.getSetting("songFilename").toString()).getName()));
                        } catch (AWTException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void onPlay(Level level) {

                    }
                }), getDefaultConfiguration());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "发生错误\n" + Tools.getStackTrace(ex));
            }
        });

        JToggleButton disablePlanet = new JToggleButton("禁用星球");
        disablePlanet.setSelected(lastOpenManager.getBoolean("disablePlanet", false));
        disablePlanet.setBounds(250, 50, 150, 30); // 设置按钮的宽度和高度
        disablePlanet.addActionListener(e -> {
            lastOpenManager.putBoolean("disablePlanet", disablePlanet.isSelected());
            lastOpenManager.save();
        });

        add(levelPath);
        add(selectFile);
        add(startButton);
        add(disablePlanet);


        pack(); // 自动调整窗口大小以适应组件
        setLocationRelativeTo(null); // 将窗口居中显示
        setVisible(true);
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

    class SystemMonitor {

        private static final AtomicLong lastHeartbeat = new AtomicLong(System.currentTimeMillis());

        // 启动内存和心跳监控
        public static void startMonitoring() {
            startMemoryMonitor();
            startWatchdogThread();
        }

        // 更新心跳时间戳
        public static void updateHeartbeat() {
            lastHeartbeat.set(System.currentTimeMillis());
        }

        // 监控内存使用
        private static void startMemoryMonitor() {
            Thread memoryThread = new Thread(() -> {
                Runtime runtime = Runtime.getRuntime();
                while (true) {
                    long used = runtime.totalMemory() - runtime.freeMemory();
                    long max = runtime.maxMemory();
                    double usage = (double) used / max * 100;
                    System.out.printf("内存使用: %.2f%%%n", usage);
                    try {
                        Thread.sleep(5000); // 每5秒检查一次内存
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            memoryThread.setDaemon(true);
            memoryThread.start();
        }

        // 看门狗线程检测卡死
        private static void startWatchdogThread() {
            Thread watchdogThread = new Thread(() -> {
                while (true) {
                    long currentTime = System.currentTimeMillis();
                    long lastBeat = lastHeartbeat.get();
                    if (currentTime - lastBeat > 5000) {
                        System.err.println("应用卡死超过5秒，即将终止...");
                        System.exit(1); // 终止JVM
                    }
                    try {
                        Thread.sleep(1000); // 每秒检查一次
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            watchdogThread.setDaemon(true);
            watchdogThread.start();
        }
    }
    private static void startMemoryThread() {
        SystemMonitor.startMonitoring();
    }
}
