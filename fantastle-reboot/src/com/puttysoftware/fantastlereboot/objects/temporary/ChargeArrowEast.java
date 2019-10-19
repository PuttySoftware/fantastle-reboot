package com.puttysoftware.fantastlereboot.objects.temporary;

import com.puttysoftware.fantastlereboot.assets.ObjectImageIndex;
import com.puttysoftware.fantastlereboot.objectmodel.ColorShaders;
import com.puttysoftware.fantastlereboot.objectmodel.FantastleObject;

public class ChargeArrowEast extends FantastleObject {
    // Constructors
    public ChargeArrowEast() {
        super(-1, "arrow_east", ObjectImageIndex.ARROW_EAST,
                ColorShaders.charge());
    }
}
