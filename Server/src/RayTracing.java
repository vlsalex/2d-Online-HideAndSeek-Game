public class RayTracing {

    private String [] map;

    private static final double tracingStep = 0.5;
    private static final double maxDistance = 3.0;

    public RayTracing(String [] map) {
        this.map = map;
    }

    public boolean isObstacle(Movement first, Movement second) {
        double x_r = second.x - first.x;
        double y_r = second.y - first.y;
        double r = Math.sqrt(x_r*x_r+y_r*y_r);
        if(r > maxDistance) return true;

        double sin = y_r/r;
        double cos = x_r/r;

        for(double s = 0; s < r; s += tracingStep) {
            int x = (int) (first.x + s * cos);
            int y = (int) (first.y + s * sin);

            if(map[y].charAt(x) == 's') {
                return true;
            }
        }

        return false;
    }

}
