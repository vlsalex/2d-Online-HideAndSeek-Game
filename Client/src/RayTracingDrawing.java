import java.util.ArrayList;

public class RayTracingDrawing {

    private String[] map;
    private static final int rays = 30;
    private static final double maxDistance = 3.0;
    private static final double rayTracingStep = 0.5;

    public RayTracingDrawing(String[] map) {
        this.map = map;
    }

    public ArrayList<Coordinate> getPlayerRays(Player player) {
        ArrayList<Coordinate> viewBlocks = new ArrayList<>();

        for(int a = 0; a < 360; a += (360 / rays)) {
            double sin = Math.sin(Math.toRadians(a));
            double cos = Math.cos(Math.toRadians(a));
            for(double d = 0; d < maxDistance; d += rayTracingStep) {
                int y = (int) (player.y + d * sin);
                int x = (int) (player.x + d * cos);

                if(map[y].charAt(x) == 's') break;

                if(viewBlocks.size() == 0 || !(viewBlocks.get(viewBlocks.size()-1).x == x && viewBlocks.get(viewBlocks.size()-1).y == y)) {
                    viewBlocks.add(new Coordinate(x, y));
                }
            }
        }

        return viewBlocks;
    }

}
