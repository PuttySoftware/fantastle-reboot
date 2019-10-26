package com.puttysoftware.fantastlereboot.objects.temporary;

import com.puttysoftware.fantastlereboot.assets.ObjectImageIndex;
import com.puttysoftware.fantastlereboot.objectmodel.ColorShaders;
import com.puttysoftware.fantastlereboot.objectmodel.FantastleObject;

class ChargeArrowNorth extends FantastleObject {
  // Constructors
  public ChargeArrowNorth() {
    super(-1, "arrow_north", ObjectImageIndex.ARROW_NORTH,
        ColorShaders.charge());
  }
}
