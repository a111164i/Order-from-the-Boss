package utils;

import com.fs.starfarer.api.Global;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;

public class OrderftBoss_Utils {

  public static String txt(String id) {
    return Global.getSettings().getString("OrderftBoss", id);
  }

  public static Vector2f getExtendedLocationFromPoint(Vector2f point, Float facing, float dist) {
    float xAxis = (float) FastTrig.cos(Math.toRadians(facing)) * dist;
    float yAxis = (float) FastTrig.sin(Math.toRadians(facing)) * dist;
    return new Vector2f(point.getX() + xAxis, point.getY() + yAxis);
  }
}
