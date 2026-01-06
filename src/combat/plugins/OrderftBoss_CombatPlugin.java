package combat.plugins;


import com.fs.starfarer.combat.ai.AI;
import combat.OrderftBoss_BaseCombatEffect;
import combat.OrderftBoss_KeepShieldOff;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.ui.LazyFont;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.ui.Fonts.ORBITRON_24AA;
import static utils.OrderftBoss_Utils.getExtendedLocationFromPoint;
import static utils.OrderftBoss_Utils.txt;

public class OrderftBoss_CombatPlugin extends BaseEveryFrameCombatPlugin {

  static LazyFont FONT1;
  static LazyFont.DrawableString string1;
  static LazyFont.DrawableString string1_2;
  static LazyFont.DrawableString string2;
  static LazyFont.DrawableString string2_2;
  static LazyFont.DrawableString string3;
  static LazyFont.DrawableString string3_2;
  static LazyFont.DrawableString string4;
  static LazyFont.DrawableString string4_2;

  List<ShipAPI> selected = new ArrayList<>();
  ShipAPI target;
  int selectNum  = 0;
  boolean locked = false;

  float elapsedTimeTotal = 0;
  float elapsedTimeFromLastOrder = 99;

  int controlKey = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_r");
  int KEY_Q = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_q");
  int KEY_W = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_w");
  int KEY_A = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_a");
  int KEY_S = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_s");
  int KEY_D = LunaSettings.getInt("order_from_the_boss_en", "OfB_keycode_d");


  Color frameColor = Misc.getPositiveHighlightColor();

  public OrderftBoss_CombatPlugin() {
    try {
      FONT1 = LazyFont.loadFont(ORBITRON_24AA);
      string1 = FONT1.createText(Keyboard.getKeyName(KEY_W), frameColor);
      string1_2 = FONT1.createText(txt("use_system"), frameColor);
      string2 = FONT1.createText(Keyboard.getKeyName(KEY_A), frameColor);
      string2_2 = FONT1.createText(txt("vent_flux"), frameColor);
      string3 = FONT1.createText(Keyboard.getKeyName(KEY_S), frameColor);
      string3_2 = FONT1.createText(txt("toggle_shield"), frameColor);
      string4 = FONT1.createText(Keyboard.getKeyName(KEY_D), frameColor);
      string4_2 = FONT1.createText(txt("fire_missiles"), frameColor);
    }catch (Exception e1){

    }

  }

  @Override
  public void advance(float amount, List<InputEventAPI> events) {

    if(locked) {
      return;
    }

    if (Global.getCombatEngine().getPlayerShip() == null) {
      return;
    }

    if(!Keyboard.isKeyDown(controlKey)){
      target = null;
      return;
    }


    Vector2f point = Global.getCombatEngine().getPlayerShip().getMouseTarget();
    if (point == null) {
      return;
    }

    selected.clear();
    target = null;
    List<ShipAPI> nearby = new ArrayList<>();
    for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
      if (tmp == Global.getCombatEngine().getPlayerShip()) continue;
      if (tmp.getOwner() != Global.getCombatEngine().getPlayerShip().getOwner()) continue;
      if (tmp.isHulk()) continue;
      if (!tmp.isAlive()) continue;
      if (tmp.isFighter()) continue;
      if (tmp.isStation()) continue;
      if (MathUtils.getDistance(tmp, point) > 100) continue;
      selected.add(tmp);
    }

    if(selected.size() > 0){
      elapsedTimeTotal += amount;
      elapsedTimeFromLastOrder += amount;
    }


  }

  @Override
  public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {


    if (target == null) {
      return;
    }

    List<InputEventAPI> shouldRemove = new ArrayList<>();
    for(InputEventAPI event : events){
      if(event.isKeyDownEvent() && Keyboard.isKeyDown(controlKey) ){

        if(Keyboard.getEventKey() == KEY_Q){
          selectNum += 1;
          elapsedTimeTotal = 0f;
          event.consume();
        }
//        if(Keyboard.getEventKey() == Keyboard.KEY_MINUS){
//          selectNum -= 1;
//          event.consume();
//        }

        if(Keyboard.getEventKey() == KEY_W){
          target.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
          Global.getCombatEngine().addFloatingText(
            target.getLocation(), txt("use_system")+"!!", 20f, frameColor, target, 1f, 1f);
          elapsedTimeFromLastOrder = 0f;
          //event.consume();
        }
        if(Keyboard.getEventKey() == KEY_A){
          target.giveCommand(ShipCommand.VENT_FLUX, null, 0);
          Global.getCombatEngine().addFloatingText(
            target.getLocation(), txt("vent_flux") +"!!", 20f, frameColor, target, 1f, 1f);
          elapsedTimeFromLastOrder = 0f;
          //event.consume();
        }
        if(Keyboard.getEventKey() == KEY_S){
          //多个不共存
          if(target.getCustomData().containsKey(OrderftBoss_KeepShieldOff.ID)){
            ((OrderftBoss_BaseCombatEffect)target.getCustomData().get(OrderftBoss_KeepShieldOff.ID)).shouldEnd = true;
            target.getCustomData().remove(OrderftBoss_KeepShieldOff.ID);
            return;
          }

          target.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
          Global.getCombatEngine().addFloatingText(
            target.getLocation(), txt("toggle_shield")+"!!", 20f, frameColor, target, 1f, 1f);
          float lockTime = LunaSettings.getInt("order_from_the_boss_en", "OfB_keep_shield_time");
          if(target.getShield() != null && target.getAIFlags() != null){
            if(target.getShield().isOn()){
              target.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, lockTime);
            }else {
              target.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, lockTime);
            }
          }
          Global.getCombatEngine().addLayeredRenderingPlugin(new OrderftBoss_KeepShieldOff(target,lockTime, new Vector2f( Global.getCombatEngine().getPlayerShip().getMouseTarget())));
          elapsedTimeFromLastOrder = 0f;
          //event.consume();
        }

        if(Keyboard.getEventKey() == KEY_D){
          for(WeaponAPI weap : target.getAllWeapons()){
            //排掉装饰武器等等
            if(weap.getSlot().getWeaponType() == WeaponAPI.WeaponType.DECORATIVE) continue;
            if(weap.getType() == WeaponAPI.WeaponType.DECORATIVE) continue;

            if(weap.getType() == WeaponAPI.WeaponType.MISSILE){
              weap.setForceFireOneFrame(true);
            }
            Global.getCombatEngine().addFloatingText(
                    target.getLocation(), txt("fire_missiles")+"!!", 20f, frameColor, target, 1f, 1f);
          }

          elapsedTimeFromLastOrder = 0f;
          //event.consume();
        }
      }
    }

  }

  @Override
  public void renderInWorldCoords(ViewportAPI viewport) {
    if(!Keyboard.isKeyDown(controlKey)){
      elapsedTimeTotal = 0f;
      return;
    }


    if (selected == null || selected.size() < 1) {
      target = null;
      elapsedTimeTotal = 0f;
      return;
    }

    if(selectNum >= selected.size() || selectNum < 0) {
      selectNum = 0;
    }
    target = (ShipAPI)(selected.get(selectNum));

    //Global.getCombatEngine().addSmoothParticle(target.getMouseTarget(), new Vector2f(0f,0f),100f,1f,0.1f,Color.magenta);


    float rad = target.getCollisionRadius();

    float width = 8f;
    float largeRad = (width + rad);
    float smallRad = largeRad - width;
    float extraRange = 100f;
    //控制框的缩放
    float level = 0f;
    float changeTime = 0.12f;
    if(elapsedTimeTotal < changeTime){
      level = (changeTime - elapsedTimeTotal)/changeTime;
    }
    //控制框的颜色
    float colorLevel = 0f;
    float colorChangeTime = 0.35f;
    if(elapsedTimeFromLastOrder < colorChangeTime){
      colorLevel = (colorChangeTime - elapsedTimeFromLastOrder)/colorChangeTime;
    }

    float r = frameColor.getRed()/255f;
    float g = frameColor.getGreen()/255f;
    float b = frameColor.getBlue()/255f;
    float alpha = Float.parseFloat(txt("frame_alpha"));
    float nearAlpha = alpha/2.5f;



    if(FONT1 != null){
      float range = target.getCollisionRadius() * 1.2f + 50f;
      Vector2f txtPoint1 = getExtendedLocationFromPoint(target.getLocation(), 90f, range + 64f);
      string1.setFontSize(32f);
      string1.setMaxHeight(40f);
      string1.setMaxWidth(200f);
      string1.draw(txtPoint1.x, txtPoint1.y);
      string1_2.setFontSize(24f);
      string1_2.setMaxHeight(80f);
      string1_2.setMaxWidth(300f);
      string1_2.draw(txtPoint1.x -80f, txtPoint1.y - 32f);

      Vector2f txtPoint2 = getExtendedLocationFromPoint(target.getLocation(), 180f, range);
      string2.setFontSize(32f);
      string2.setMaxHeight(40f);
      string2.setMaxWidth(200f);
      string2.draw(txtPoint2.x, txtPoint2.y + 32f);
      string2_2.setFontSize(24f);
      string2_2.setMaxHeight(80f);
      string2_2.setMaxWidth(300f);
      string2_2.draw(txtPoint2.x -120f, txtPoint2.y);

      Vector2f txtPoint3 = getExtendedLocationFromPoint(target.getLocation(), 270f, range);
      string3.setFontSize(32f);
      string3.setMaxHeight(40f);
      string3.setMaxWidth(200f);
      string3.draw(txtPoint3.x, txtPoint3.y);
      string3_2.setFontSize(24f);
      string3_2.setMaxHeight(80f);
      string3_2.setMaxWidth(300f);
      string3_2.draw(txtPoint3.x -120f, txtPoint3.y - 32f);

      Vector2f txtPoint4 = getExtendedLocationFromPoint(target.getLocation(), 00f, range);
      string4.setFontSize(32f);
      string4.setMaxHeight(40f);
      string4.setMaxWidth(200f);
      string4.draw(txtPoint4.x, txtPoint4.y + 32f);
      string4_2.setFontSize(24f);
      string4_2.setMaxHeight(80f);
      string4_2.setMaxWidth(300f);
      string4_2.draw(txtPoint4.x -20f, txtPoint4.y);
    }

    useOpengl();
    drawFrame(
      largeRad + extraRange * level,
      smallRad + extraRange * level,
      r, g * (1f-colorLevel), b * (1f-colorLevel), alpha * (1f-level),
      nearAlpha * (1f-level));

    closeOpengl();


  }

  void useOpengl(){
    //这些设定都要在begin之前设置好，防止破坏其他人的参数
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPushMatrix();

    //画纯色图不需要材质，打开材质就一定要绑定，就会导致画不出东西
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    //这里不做绑定
    //GL11.glBindTexture(GL11.GL_TEXTURE_2D, Global.getSettings().getSprite("aEP_FX", "thick_smoke_all2").textureId)

    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  }

  void closeOpengl(){
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  void drawFrame(float largeRad, float smallRad, float r, float g, float b, float alpha, float nearAlpha){
    //画4个角
    float angleStep = 90f;
    float startingAngle = 0f;

    int i = 0;
    while (i <= 3) {
      float a = i * angleStep + startingAngle;

      GL11.glBegin(GL11.GL_QUAD_STRIP);
      Vector2f pointFar = getExtendedLocationFromPoint(target.getLocation(), a - 15f, largeRad);
      GL11.glColor4f(r,g,b, alpha);
      GL11.glVertex2f(pointFar.x, pointFar.y);

      Vector2f pointNear = getExtendedLocationFromPoint(target.getLocation(), a - 15f, smallRad);
      GL11.glColor4f(r,g,b, nearAlpha);
      GL11.glVertex2f(pointNear.x, pointNear.y);

      float pointExtraMultiple = 1.18f;
      Vector2f pointFar2 = getExtendedLocationFromPoint(target.getLocation(), a, largeRad * pointExtraMultiple);
      GL11.glColor4f(r,g,b, alpha);
      GL11.glVertex2f(pointFar2.x, pointFar2.y);

      Vector2f pointNear2 = getExtendedLocationFromPoint(target.getLocation(), a, smallRad * pointExtraMultiple);
      GL11.glColor4f(r,g,b, nearAlpha);
      GL11.glVertex2f(pointNear2.x, pointNear2.y);

      Vector2f pointFar3 = getExtendedLocationFromPoint(target.getLocation(), a + 15f, largeRad);
      GL11.glColor4f(r,g,b, alpha);
      GL11.glVertex2f(pointFar3.x, pointFar3.y);

      Vector2f pointNear3 = getExtendedLocationFromPoint(target.getLocation(), a + 15f, smallRad);
      GL11.glColor4f(r,g,b, nearAlpha);
      GL11.glVertex2f(pointNear3.x, pointNear3.y);
      GL11.glEnd();
      i += 1;
    }
  }



}
