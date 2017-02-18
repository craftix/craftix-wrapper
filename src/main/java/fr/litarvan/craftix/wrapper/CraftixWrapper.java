package fr.litarvan.craftix.wrapper;

import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.ProcessLogManager;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.util.WindowMover;
import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.imageio.ImageIO;
import org.json.JSONObject;

public class CraftixWrapper
{
    public static final String VERSIONS = "http://litarvan.github.io/craftix/craftix.json";
    public static final String CRAFTIX = "http://litarvan.github.io/craftix/craftix-${version}.zip";

    private JSONObject config;
    private CraftixUpdater updater;

    private SplashScreen splash;
    private SColoredBar bar;

    private File folder = GameDirGenerator.createGameDir("craftix");
    private CrashReporter reporter;

    public CraftixWrapper(JSONObject config)
    {
        this.config = config;
        this.reporter = new CrashReporter(config.getString("title"), new File(folder, config.getString("id") + "-crashes"));
    }

    public void start()
    {
        JSONObject bar = config.getJSONObject("bar");

        splash(bar);
        update();

        updateLauncher();
        launch();
    }

    private void handleCrash(String message, Exception ex)
    {
        reporter.catchError(ex, message);
    }

    private void splash(JSONObject bar)
    {
        Image splash = getSplash();

        this.splash = new SplashScreen(config.getString("title"), splash);

        if (config.has("icon"))
        {
            InputStream resource = getClass().getResourceAsStream(config.getString("icon"));

            if (resource != null)
            {
                try
                {
                    this.splash.setIconImage(ImageIO.read(resource));
                }
                catch (IOException e)
                {
                    System.err.println("Unable to read icon");
                    e.printStackTrace();
                }
            }
        }

        this.splash.setResizable(false);
        this.splash.setLayout(null);

        WindowMover mover = new WindowMover(this.splash);
        this.splash.addMouseListener(mover);
        this.splash.addMouseMotionListener(mover);

        Color background = hex(bar.getString("background"));
        Color foreground = hex(bar.getString("foreground"));

        this.bar = new SColoredBar(background, foreground);
        this.bar.setStringPainted(true);
        this.bar.setStringColor(hex(bar.getString("text")));
        this.bar.setBounds(0, splash.getHeight(this.splash) - bar.getInt("height"), splash.getWidth(this.splash), bar.getInt("height"));
        this.splash.add(this.bar);

        this.splash.display();
    }

    private void update()
    {
        JSONObject strings = strings();

        this.bar.setString(strings.getString("retrieving-versions"));

        JSONObject versions = new JSONObject(retrieveVersions());

        this.bar.setString(strings.getString("checking"));

        this.updater = new CraftixUpdater(versions, config.getString("version"), this);

        boolean updateElectron = this.updater.shouldUpdateElectron();
        boolean updateCraftix = this.updater.shouldUpdateCraftix();

        try
        {
            if (updateElectron)
            {
                this.bar.setString(strings.getString("updating-electron"));
                this.updater.updateElectron();
            }

            if (updateCraftix)
            {
                this.bar.setString(strings.getString("updating-craftix"));
                this.updater.updateCraftix();
            }
        }
        catch (IOException e)
        {
            handleCrash(strings.getString("download-fail"), e);
            return;
        }

        this.bar.setString(strings.getString("launching"));
    }

    private String retrieveVersions()
    {
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new InputStreamReader(new URL(VERSIONS).openStream()));

            String file = "";
            String line;

            while ((line = reader.readLine()) != null)
            {
                file += line;
            }

            return file;
        }
        catch (IOException e)
        {
            handleCrash("FATAL Error: Unable to read remote Craftix versions\n\nEither your connection is down, an update is uploading, or the Craftix structure is unavailable (this is really bad)\nPlease try again in 5 minutes, then if it doesn't come from your connection, please open an issue on https://github.com/craftix/craftix-wrapper\n\nSoit votre connexion a été coupee, soit une mise a jour est en train d'etre mise en place, soit toute la structure Craftix est hors-service (C'est très grave)\nMerci de reessayer dans 5 minutes, si ca ne fonctionne toujours pas et que votre connexion fonctionne merci d'ouvrir une issue sur https://github.com/craftix/craftix-wrapper", e);
            return null;
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    private void updateLauncher()
    {
        this.bar.setString(strings().getString("updating-launcher"));

        SUpdate su = new SUpdate(config.getString("launcher-update"), getLauncherFolder());
        su.getServerRequester().setRewriteEnabled(true);

        try
        {
            su.start();
        }
        catch (Exception e)
        {
            handleCrash(strings().getString("launcher-fail"), e);
        }
    }

    private void launch()
    {
        String os = System.getProperty("os.name").toLowerCase();
        String executable = folder.getAbsolutePath() + "/electron/electron";

        if (os.contains("win"))
        {
            executable += ".exe";
        }
        else if (os.contains("mac"))
        {
            executable = folder.getAbsolutePath() + "/electron/Electron.app/Contents/MacOS/Electron";
        }

        ProcessBuilder builder = new ProcessBuilder(executable, getLauncherFolder().getAbsolutePath());
        builder.redirectErrorStream(true);

        Process p;

        try
        {
            p = builder.start();
        }
        catch (IOException e)
        {
            handleCrash(strings().getString("launching-fail").replace("${folder}", folder.getAbsolutePath()), e);
            return;
        }

        ProcessLogManager manager = new ProcessLogManager(p.getInputStream());
        manager.setToWrite(new File(folder, "electron-logs.txt"));
        manager.start();

        /*InternalLaunchProfile profile = new InternalLaunchProfile("fr.litarvan.craftix.Main", "start", new Object[]{}, new Class[]{});
        profile.setClasspath(Explorer.dir(folder).sub("craftix").sub(getConfig().getString("version")).files().get());

        InternalLauncher launcher = new InternalLauncher(profile);
        try
        {
            launcher.launch();
        }
        catch (LaunchException e)
        {
            e.printStackTrace();
        }*/

        splash.setVisible(false);

        try
        {
            p.waitFor();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private File getLauncherFolder()
    {
        return new File(folder, "launcher/" + config.getString("id"));
    }

    private JSONObject strings()
    {
        return config.getJSONObject("strings");
    }

    private Color hex(String hex)
    {
        int r = Integer.valueOf(hex.substring(1, 3), 16);
        int g = Integer.valueOf(hex.substring(3, 5), 16);
        int b = Integer.valueOf(hex.substring(5, 7), 16);
        int a = 255;

        if (hex.length() == 9)
        {
            a = Integer.valueOf(hex.substring(7, 9), 16);
        }

        return new Color(r, g, b, a);
    }

    private Image getSplash()
    {
        try
        {
            return ImageIO.read(getClass().getResourceAsStream(config.getString("splash")));
        }
        catch (IOException e)
        {
            handleCrash("FATAL ERROR: Couldn't read the splash image", e);
            return null;
        }
    }

    public JSONObject getConfig()
    {
        return config;
    }

    public CraftixUpdater getUpdater()
    {
        return updater;
    }

    public File getFolder()
    {
        return folder;
    }
}
