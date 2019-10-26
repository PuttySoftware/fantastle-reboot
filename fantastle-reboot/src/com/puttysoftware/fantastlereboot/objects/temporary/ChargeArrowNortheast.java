package com.puttysoftware.fantastlereboot.objects.temporary;

import com.puttysoftware.fantastlereboot.assets.ObjectImageIndex;
import com.puttysoftware.fantastlereboot.objectmodel.ColorShaders;
import com.puttysoftware.fantastlereboot.objectmodel.FantastleObject;

class ChargeArrowNortheast extends FantastleObject {
  // Constructors
  public ChargeArrowNortheast() {
    super(-1, "arrow_northeast", ObjectImageIndex.ARROW_NORTHEAST,
        ColorShaders.charge());
  }
}
