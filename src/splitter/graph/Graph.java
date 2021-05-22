package splitter.graph;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.BiFunction;

public class Graph {
    Map<Integer, BigDecimal>[] fromEdges;

    public Graph(int vCount) {
        fromEdges = new HashMap[vCount];
    }

    private BigDecimal getEdge(int a, int b) {
        return  fromEdges[a] == null ? null : fromEdges[a].get(b);
    }

    private void setEdge(int from, int to, BigDecimal amount) {
        if (fromEdges[from] == null) {
            fromEdges[from] = new HashMap<>();
        }
        fromEdges[from].put(to, amount);

    }

    public void addEdge(int a, int b, BigDecimal increment) {
        if (increment == null || increment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidParameterException("Increment must be greater then zero");
        }
        BigDecimal curValue = getEdge(a, b);
        if (curValue == null) {
            setEdge(a, b, increment);
        } else {
            setEdge(a, b, curValue.add(increment));
        }
    }

    private void subtractEdge(int a, int b, BigDecimal decrement) {
        BigDecimal curValue = getEdge(a, b);
        if (curValue == null) {
            throw new InvalidParameterException("There is no edge from " + a + " to " + b);
        }
        BigDecimal newValue = curValue.subtract(decrement);
        int compareResult = newValue.compareTo(BigDecimal.ZERO);
        if (compareResult > 0) {
            fromEdges[a].put(b, newValue);
        } else if (compareResult == 0) {
            fromEdges[a].remove(b);
        } else {
            throw new InvalidParameterException("The edge from " + a + " to " + b + " can not be negative");
        }
    }

    private boolean optimize(int a, int b, int c) {
        if (a < 0 || b < 0 || c < 0 || a >= fromEdges.length || b >= fromEdges.length || c >= fromEdges.length) {
            return false;
        }
        BigDecimal ab = getEdge(a, b);
        BigDecimal bc = getEdge(b, c);

        if ( ab == null || bc == null) {
            return false;
        }

        BigDecimal maxFlow = ab.compareTo(bc) < 0 ? ab : bc;
        subtractEdge(a, b, maxFlow);
        subtractEdge(b, c, maxFlow);
        if (a != c) {
            addEdge(a, c, maxFlow);
        }

        return true;
    }

    private boolean optimize(int a, int b) {
        boolean changed = false;
        if (fromEdges[b] == null) {
            return false;
        } else {
            for (Map.Entry<Integer, BigDecimal> entry : new HashSet<>(fromEdges[b].entrySet())) {
                boolean curChanged = optimize(a, b, entry.getKey());
                if (curChanged && getEdge(a, b) == null) {
                    return true;
                }
                changed |= curChanged;
            }

        }
        return changed;
    }

    private boolean optimize(int a) {
        boolean changed = false;
        boolean curChanged = false;
        for (Map.Entry<Integer, BigDecimal> entry : new HashSet<>(fromEdges[a].entrySet())) {
            while (fromEdges[a].get(entry.getKey()) != null && (curChanged = optimize(a, entry.getKey())));
            changed |= curChanged;
        }
        return changed;
    }

    public void optimize() {
        for (int i = 0; i < fromEdges.length; i++) {
            while (optimize(i));
        }
    }

    public <T> Map<T, BigDecimal> toMap(BiFunction<Integer, Integer, T> keyFactory) {
        Map<T, BigDecimal> result = new HashMap<>();
        for (int i = 0; i < fromEdges.length; i++) {
            if (fromEdges[i] != null) {
                for (Map.Entry<Integer, BigDecimal> entry: fromEdges[i].entrySet()) {
                    result.put(keyFactory.apply(i, entry.getKey()), entry.getValue());
                }
            }
        }

        return result;
    }
}
