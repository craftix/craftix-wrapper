package fr.litarvan.craftix.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import org.json.JSONObject;

public class Main
{
    public static final String CONFIG = "/craftix-wrapper.json";

    public static void main(String[] args)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(CONFIG)));
        CraftixWrapper wrapper;

        try
        {
            String file = "";
            String line;

            while ((line = reader.readLine()) != null)
            {
                file += line;
            }

            JSONObject config = new JSONObject(file);
            wrapper = new CraftixWrapper(config);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "FATAL Error: Unable to read config", "Fatal error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            return;
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException ignored)
            {
            }
        }

        wrapper.start();
    }
}
