/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell


Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.fantastlereboot.datamanagers;

import java.io.IOException;
import java.util.ArrayList;

import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fileutils.ResourceStreamReader;

public class MusicDataManager {
    public static String[] getMusicData() {
        try (final ResourceStreamReader rsr = new ResourceStreamReader(
                MusicDataManager.class.getResourceAsStream(
                        "/com/puttysoftware/tallertower/resources/data/music/music.txt"))) {
            // Fetch data
            final ArrayList<String> rawData = new ArrayList<>();
            String line = "";
            while (line != null) {
                line = rsr.readString();
                if (line != null) {
                    rawData.add(line);
                }
            }
            return rawData.toArray(new String[rawData.size()]);
        } catch (final IOException e) {
            FantastleReboot.logError(e);
            return null;
        }
    }
}
