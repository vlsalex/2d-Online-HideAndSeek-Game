public class Player {

    public double x;
    public double y;
    public boolean isDied;
    public long time;
    public long timeLastOfflineUpdate;

    public double backX;
    public double backY;

    public double deltaX;
    public double deltaY;

    private boolean isFirstSetting = true;

    public Player(double x, double y, boolean isDied) {
        this.x = x;
        this.y = y;
        this.isDied = isDied;
    }

    public void setXY(double new_x, double new_y) {
        long backTime = time;
        timeLastOfflineUpdate = time = System.currentTimeMillis();
        double time_r = ( (double) (time - backTime)) / 1000;

        if(!isFirstSetting) {
            deltaX = (new_x - backX) / time_r;
            deltaY = (new_y - backY) / time_r;
        }
        else {
            deltaX = deltaY = 0.0;
            isFirstSetting = false;
        }

        x = backX = new_x;
        y = backY = new_y;
    }

    public void userUpdate() {
        double time_now = ((double) (System.currentTimeMillis() - timeLastOfflineUpdate)) / 1000;
        timeLastOfflineUpdate = System.currentTimeMillis();
        this.x += deltaX * time_now;
        this.y += deltaY * time_now;
    }

}
