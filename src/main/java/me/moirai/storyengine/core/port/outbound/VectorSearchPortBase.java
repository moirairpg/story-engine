package me.moirai.storyengine.core.port.outbound;

import java.util.ArrayList;
import java.util.List;

public interface VectorSearchPortBase {

    default List<Float> toFloatList(float[] vector) {

        var list = new ArrayList<Float>(vector.length);
        for (var v : vector) {
            list.add(v);
        }

        return list;
    }
}
