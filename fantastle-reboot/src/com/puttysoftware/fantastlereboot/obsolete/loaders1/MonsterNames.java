package com.puttysoftware.fantastlereboot.obsolete.loaders1;

import com.puttysoftware.fantastlereboot.loaders.DataLoader;

public class MonsterNames {
    // Fields
    private static String[] CACHE;
    private static boolean CACHE_CREATED = false;

    public static String[] getAllNames() {
        if (!MonsterNames.CACHE_CREATED) {
            MonsterNames.CACHE = DataLoader.loadMonsterData();
            MonsterNames.CACHE_CREATED = true;
        }
        return MonsterNames.CACHE;
    }
}