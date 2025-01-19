package sk.spsepo.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip {

    RectF rect;

    private Bitmap bitmap;

    private float length;
    private float height;

    private float x;
    private float y;

    private float shipSpeed;
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY){
        rect = new RectF();

        length = screenX / 10;
        height = screenY / 10;

        x = screenX / 2;
        y = screenY - 20;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (length), (int) (height), false);

        shipSpeed = 800;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public int getX(){
        return (int) x;
    }

    public float getLength(){
        return length;
    }

    public void setMovementState(int state){
        shipMoving = state;
    }

    public void update(long fps){
        if(shipMoving == LEFT)
            if (x > length * .5) x = x - shipSpeed / fps;
        if(shipMoving == RIGHT)
            if (x < length * 9.5) x = x + shipSpeed / fps;

        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;
        shipMoving = STOPPED;
    }

    // Add the following methods for saving and loading state
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}