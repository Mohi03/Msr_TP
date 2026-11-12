import java.util.*;

class Place {
    private String name;
    private int tokens;
    private int capacity;

    public Place(String name, int initialTokens, int capacity) {
        this.name = name;
        this.tokens = initialTokens;
        this.capacity = capacity;
    }

    public String getName() { 
        return name; 
    }
    
    public int getTokens() { 
        return tokens; 
    }
    
    public void addTokens(int count) {
        this.tokens += count;
    }

    public void removeTokens(int count) {
        this.tokens -= count;
    }

    public boolean hasCapacity(int count) {
        if (capacity == Integer.MAX_VALUE) return true;
        return (tokens + count) <= capacity;
    }
}

class Transition {
    private String name;
    private Map<Place, Integer> inputPlaces;
    private Map<Place, Integer> outputPlaces;

    public Transition(String name) {
        this.name = name;
        this.inputPlaces = new HashMap<>();
        this.outputPlaces = new HashMap<>();
    }

    public void addInput(Place place, int weight) {
        inputPlaces.put(place, weight);
    }

    public void addOutput(Place place, int weight) {
        outputPlaces.put(place, weight);
    }

    public boolean isEnabled() {
        for (Map.Entry<Place, Integer> entry : inputPlaces.entrySet()) {
            if (entry.getKey().getTokens() < entry.getValue()) {
                return false;
            }
        }
        for (Map.Entry<Place, Integer> entry : outputPlaces.entrySet()) {
            if (!entry.getKey().hasCapacity(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void fire() {
        if (isEnabled()) {
            for (Map.Entry<Place, Integer> entry : inputPlaces.entrySet()) {
                entry.getKey().removeTokens(entry.getValue());
            }
            for (Map.Entry<Place, Integer> entry : outputPlaces.entrySet()) {
                entry.getKey().addTokens(entry.getValue());
            }
            System.out.println("Executed: " + name);
        } else {
            System.out.println("Blocked: " + name);
        }
    }

    public String getName() { 
        return name; 
    }
}

public class RoutingProtocolSimulation {
    public static void main(String[] args) {
        System.out.println("=== Routing Protocol Simulation ===\n");

        Place pIdle = new Place("P_Idle", 1, 1);
        Place pBusy = new Place("P_Busy", 0, 1);
        Place pPacketArrived = new Place("P_PacketArrived", 0, 10);
        Place pQueue = new Place("P_Queue", 0, 2);
        Place pRoutingTable = new Place("P_RoutingTable", 1, 1);
        Place pDropped = new Place("P_Dropped", 0, 100);

        Transition tReceive = new Transition("T1_ReceivePacket");
        tReceive.addInput(pPacketArrived, 1);
        tReceive.addInput(pIdle, 1);
        tReceive.addOutput(pBusy, 1);

        Transition tForward = new Transition("T2_ForwardPacket");
        tForward.addInput(pBusy, 1);
        tForward.addInput(pRoutingTable, 1);
        tForward.addOutput(pIdle, 1);
        tForward.addOutput(pRoutingTable, 1);

        Transition tEnqueue = new Transition("T3_EnqueuePacket");
        tEnqueue.addInput(pPacketArrived, 1);
        tEnqueue.addInput(pBusy, 1);
        tEnqueue.addOutput(pQueue, 1);
        tEnqueue.addOutput(pBusy, 1);

        Transition tDrop = new Transition("T4_DropPacket");
        tDrop.addInput(pPacketArrived, 1);
        tDrop.addInput(pBusy, 1);
        tDrop.addOutput(pDropped, 1);
        tDrop.addOutput(pBusy, 1);

        System.out.println("Scenario 1: Packet arrives while agent is idle");
        pPacketArrived.addTokens(1);
        if (tReceive.isEnabled()) {
            tReceive.fire();
        }

        System.out.println("\nScenario 2: Agent forwards the packet");
        if (tForward.isEnabled()) {
            tForward.fire();
        }

        System.out.println("\nScenario 3: Handling busy states and queue limits");
        pIdle.removeTokens(1);
        pBusy.addTokens(1);

        pPacketArrived.addTokens(1);
        if (tEnqueue.isEnabled()) tEnqueue.fire();

        pPacketArrived.addTokens(1);
        if (tEnqueue.isEnabled()) tEnqueue.fire();

        pPacketArrived.addTokens(1);
        if (tDrop.isEnabled()) {
            tDrop.fire();
        }

        System.out.println("\n=== Final Status ===");
        System.out.println("Queue Tokens: " + pQueue.getTokens());
        System.out.println("Dropped Packets: " + pDropped.getTokens());
    }
}
