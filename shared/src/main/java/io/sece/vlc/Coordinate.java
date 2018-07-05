package io.sece.vlc;

public interface Coordinate<T extends Coordinate> {
    double euclideanDistanceTo(T other);

    default T nearestNeighbor(T... neighbors) {
        double d, min = Double.MAX_VALUE;
        T nearest = null;

        if (neighbors.length == 0)
            throw new IllegalArgumentException("Missing list of neighbors");

        for (T neighbor : neighbors) {
            d = Math.abs(this.euclideanDistanceTo(neighbor));
            if (d < min) {
                min = d;
                nearest = neighbor;
            }
            if (d == 0) break;
        }
        return nearest;
    }
}
