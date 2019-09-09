package com.puttysoftware.fantastlereboot.creatures.castes;

import com.puttysoftware.fantastlereboot.Messager;
import com.puttysoftware.fantastlereboot.spells.SpellBook;
import com.puttysoftware.fantastlereboot.spells.SpellBookManager;

public class CasteManager {
    private static boolean CACHE_CREATED = false;
    private static Caste[] CACHE;

    public static Caste selectCaste() {
        final String[] names = CasteConstants.CASTE_NAMES;
        String dialogResult = null;
        dialogResult = Messager.showInputDialog("Select a Caste",
                "Select Caste", names, names[0]);
        if (dialogResult != null) {
            int index;
            for (index = 0; index < names.length; index++) {
                if (dialogResult.equals(names[index])) {
                    break;
                }
            }
            return CasteManager.getCaste(index);
        } else {
            return null;
        }
    }

    public static SpellBook getSpellBookByID(final int ID) {
        return SpellBookManager.getSpellBookByID(ID);
    }

    public static Caste getCaste(final int casteID) {
        if (!CasteManager.CACHE_CREATED) {
            // Create cache
            CasteManager.CACHE = new Caste[CasteConstants.CASTES_COUNT];
            for (int x = 0; x < CasteConstants.CASTES_COUNT; x++) {
                CasteManager.CACHE[x] = new Caste(x);
            }
            CasteManager.CACHE_CREATED = true;
        }
        return CasteManager.CACHE[casteID];
    }
}
