package combat;

import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lwjgl.util.vector.Vector2f;

public class OrderftBoss_KeepShieldOff extends OrderftBoss_BaseCombatEffect {

  public static String ID = OrderftBoss_KeepShieldOff.class.getSimpleName();
  ShipAPI ship;
  Vector2f shieldTarget;

  public OrderftBoss_KeepShieldOff(ShipAPI ship, float lifeTime, Vector2f shieldTarget) {
    super(ship, lifeTime);
    this.ship = ship;
    this.shieldTarget = shieldTarget;
    ship.setCustomData(ID, this);
  }


  @Override
  public void advanceImpl(float amount) {
    if(ship.getShield() != null && ship.getShield().isOn() && ship.getShield().getType() == ShieldAPI.ShieldType.OMNI && shieldTarget != null){
      ship.setShieldTargetOverride(shieldTarget.x, shieldTarget.y);
    }
    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
  }


  @Override
  public void readyToEnd() {
    if(ship.getCustomData().containsKey(OrderftBoss_KeepShieldOff.ID)){
      ship.getCustomData().remove(OrderftBoss_KeepShieldOff.ID);
    }
  }


}

