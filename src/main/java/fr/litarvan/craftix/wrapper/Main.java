/*
 * Copyright 2017 Adrien "Litarvan" Navratil
 *
 * This file is part of Craftix.
 *
 * Craftix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Craftix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Craftix.  If not, see <http://www.gnu.org/licenses/>.
 */
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
