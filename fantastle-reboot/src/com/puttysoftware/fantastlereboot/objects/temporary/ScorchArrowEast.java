package com.puttysoftware.fantastlereboot.objects.temporary;

import com.puttysoftware.fantastlereboot.assets.ObjectImageIndex;
import com.puttysoftware.fantastlereboot.objectmodel.ColorShaders;
import com.puttysoftware.fantastlereboot.objectmodel.FantastleObject;

class ScorchArrowEast extends FantastleObject {
    // Constructors
    public ScorchArrowEast() {
        super(-1, "arrow_east", ObjectImageIndex.ARROW_EAST,
                ColorShaders.scorch());
    }
}
