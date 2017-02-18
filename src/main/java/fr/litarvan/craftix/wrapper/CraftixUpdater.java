package fr.litarvan.craftix.wrapper;

import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONObject;

public class CraftixUpdater
{
    private JSONObject remote;
    private String version;
    private CraftixWrapper wrapper;

    public CraftixUpdater(JSONObject remote, String version, CraftixWrapper wrapper)
    {
        this.remote = remote;
        this.version = version;
        this.wrapper = wrapper;
    }

    public boolean shouldUpdateElectron()
    {
        File electron = new File(wrapper.getFolder(), "electron");
        return !electron.isDirectory() || electron.listFiles().length == 0;
    }

    public boolean shouldUpdateCraftix()
    {
        File craftix = new File(wrapper.getFolder(), "craftix/" + version);
        return !craftix.isDirectory() || craftix.listFiles().length == 0;
    }

    public void updateCraftix() throws IOException
    {
        URL url = new URL(CraftixWrapper.CRAFTIX.replace("${version}", version));
        File zip = new File(wrapper.getFolder(), getFile(url));

        downloadFile(url, zip);
        unzip(zip, new File(wrapper.getFolder(), "craftix/" + version));
    }

    public void updateElectron() throws IOException
    {
        List<Object> list = remote.getJSONArray("electron").toList();
        String os = "linux";
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win"))
        {
            os = "windows";
        }
        else if (osName.contains("mac"))
        {
            os = "osx";
        }

        os = remote.getJSONObject("os").getString(os);

        String arch = "32";

        if (System.getProperty("os.arch").contains("64"))
        {
            arch = "64";
        }

        arch = remote.getJSONObject("arch").getString(arch);

        for (int i = 0; i < list.size(); i++)
        {
            list.set(i, ((String) list.get(i)).replace("${os}", os).replace("${arch}", arch));
        }

        File electron = new File(wrapper.getFolder(), "electron/");

        for (Object obj : list)
        {
            URL url = new URL((String) obj);
            File zip = new File(wrapper.getFolder(), getFile(url));

            downloadFile(url, zip);
            unzip(zip, electron);
        }

        if (!osName.contains("win"))
        {
            File executable = new File(electron, osName.contains("mac") ? "Electron.app/Contents/MacOS/Electron" : "electron");
            ProcessBuilder builder = new ProcessBuilder("chmod", "u+x", executable.getAbsolutePath());
            builder.start();
        }
    }

    private String getFile(URL url)
    {
        String file = url.getFile();
        return file.substring(file.lastIndexOf('/') + 1, file.length());
    }

    private void downloadFile(URL url, File file) throws IOException
    {
        System.out.println("Downloading " + url + " to " + file.getAbsolutePath());

        file.getParentFile().mkdirs();

        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    public void unzip(File file, File dest) throws IOException
    {
        System.out.println("Unzipping " + file.getAbsolutePath() + " to " + file.getAbsolutePath());

        int bufferSize = 2048;
        ZipFile zip = new ZipFile(file);

        dest.mkdirs();
        Enumeration zipFileEntries = zip.entries();

        while (zipFileEntries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(dest, currentEntry);
            File destinationParent = destFile.getParentFile();

            destinationParent.mkdirs();

            if (!entry.isDirectory())
            {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                byte data[] = new byte[bufferSize];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream out = new BufferedOutputStream(fos, bufferSize);

                while ((currentByte = is.read(data, 0, bufferSize)) != -1)
                {
                    out.write(data, 0, currentByte);
                }

                out.flush();
                out.close();
                is.close();
            }
        }
    }

    public JSONObject getRemote()
    {
        return remote;
    }

    public CraftixWrapper getWrapper()
    {
        return wrapper;
    }
}
