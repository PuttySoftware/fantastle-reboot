package com.puttysoftware.fantastlereboot.objects.temporary;

import com.puttysoftware.fantastlereboot.assets.ObjectImageIndex;
import com.puttysoftware.fantastlereboot.objectmodel.ColorShaders;
import com.puttysoftware.fantastlereboot.objectmodel.FantastleObject;

public class ShadowArrowNortheast extends FantastleObject {
    // Constructors
    public ShadowArrowNortheast() {
        super(-1, "arrow_northeast", ObjectImageIndex.ARROW_NORTHEAST,
                ColorShaders.shadow());
    }
}
