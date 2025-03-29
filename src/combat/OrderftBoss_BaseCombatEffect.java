package combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.MathUtils;

import java.util.List;

public class OrderftBoss_BaseCombatEffect extends BaseCombatLayeredRenderingPlugin {
  public float time;
  public float lifeTime;
  public boolean shouldEnd = false;


  public OrderftBoss_BaseCombatEffect(ShipAPI ship, float lifeTime) {
    this.lifeTime = lifeTime;
    this.entity = ship;
  }

  @Override
  public void advance(float amount) {
    if(shouldEnd) return;

    float realAmount =  amount;
    if(entity != null){
      //判断是否isInPlay
      if(!Global.getCombatEngine().isEntityInPlay(entity)){
        shouldEnd = true;
        return;
      }

      //如果绑定的实体是舰船，就多加几个判断，并且同步时流
      if(entity instanceof ShipAPI){
        ShipAPI s = (ShipAPI) entity;
        realAmount *= s.getMutableStats().getTimeMult().getModifiedValue();
        if(s.isHulk() || !s.isAlive()) {
          shouldEnd = true;
          return;
        }
      }
    }

    //如果lifeTime大于0，激活计时系统，如果超时也结束
    if(lifeTime > 0f){
      if(time >= lifeTime){
        shouldEnd = true;
        return;
      }
    }

    //time = lifeTime的一帧一定会得到
    time += realAmount;
    time = MathUtils.clamp(time,0f,lifeTime);
    advanceImpl(realAmount);
  }

  @Override
  public boolean isExpired() {
    if(shouldEnd){
      readyToEnd();
    }
    return shouldEnd;
  }

  public void advanceImpl(float amount){

  }

  /**
   * 结算函数，只要是结束一定会调用一次，无论是setShouldEnd还是因为lifeTime到了。
   * */
  public void readyToEnd(){

  }

}
