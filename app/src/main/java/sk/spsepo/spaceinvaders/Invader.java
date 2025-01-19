package sk.spsepo.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Invader {

    RectF rect;

    Random generator = new Random();

    private Bitmap bitmap1;
    private Bitmap bitmap2;

    private float length;
    private float height;

    private float x;
    private float y;

    private float shipSpeed;

    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = RIGHT;

    boolean isVisible;

    public Invader(Context context, int row, int column, int screenX, int screenY) {

        rect = new RectF();

        length = screenX / 27;
        height = screenY / 27;

        isVisible = true;

        int paddingX = screenX / 25;
        int paddingY = 90;

        x = column * (length + paddingX);
        y = row * (length + paddingX / 4) + paddingY;

        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);

        bitmap1 = Bitmap.createScaledBitmap(bitmap1, (int) (length), (int) (height), false);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, (int) (length), (int) (height), false);

        shipSpeed = 50;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap1;
    }

    public Bitmap getBitmap2(){
        return bitmap2;
    }

    public int getX(){
        return (int) x;
    }

    public int getY(){
        return (int) y;
    }

    public float getLength(){
        return length;
    }

    public void update(long fps){
        if(shipMoving == LEFT)
            x = x - shipSpeed / fps;
        if(shipMoving == RIGHT)
            x = x + shipSpeed / fps;

        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;
    }

    public void dropDownAndReverse(){
        if(shipMoving == LEFT)
            shipMoving = RIGHT;
        else
            shipMoving = LEFT;

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength){

        int randomNumber = -1;
        if((playerShipX + playerShipLength > x && playerShipX + playerShipLength < x + length) ||
                (playerShipX > x && playerShipX < x + length)) {
            randomNumber = generator.nextInt(40);
            if(randomNumber == 0)
                return true;
        }

        randomNumber = generator.nextInt(750);
        if(randomNumber == 0)
            return true;
        else
            return false;
    }

    // Add the following methods for saving and loading state
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }
}