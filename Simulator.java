import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field containing 
 * grass and trees.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 12;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 12;
    // The probability that a tree will be created in any given grid position.
    private static final double TREE_CREATION_PROBABILITY = 0.25;
    // The probability that a grass will be created in any given position.
    private static final double GRASS_CREATION_PROBABILITY = 0.5;    
    // The probability that a grass will be created in any given position.
    private static final double DEER_CREATION_PROBABILITY = 0.5; 
    // The probability that a grass will be created in any given position.
    private static final double FIRE_CREATION_PROBABILITY = 0.5; 

    // Lists of animals in the field.
    private List<Grass> grasses;
    private List<Tree> trees;
    private List<Deer> deers;
    private List<Fire> fires;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        grasses = new ArrayList<>();
        trees = new ArrayList<>();
        fires = new ArrayList<>();
        deers = new ArrayList<>();
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Deer.class, Color.ORANGE);
        view.setColor(Tree.class, Color.BLUE);
        view.setColor(Grass.class, Color.GREEN);
        view.setColor(Fire.class, Color.RED);
        
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long 
     * period (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(4000);
    }
    
    /**
     * Run the simulation for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step=1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
            // delay(60);   // uncomment this to run more slowly
        }
    }
    
    /**
     * Run the simulation from its current state for a single step. Iterate
     * over the whole field updating the state of each tree and grass.
     */
    public void simulateOneStep()
    {
        step++;

        // Provide space for newborn grass.
        List<Grass> newGrasses = new ArrayList<>();        
        // Let all grass act.
        for(Iterator<Grass> it = grasses.iterator(); it.hasNext(); ) {
            Grass grass = it.next();
            grass.run(newGrasses);
            if(! grass.isAlive()) {
                it.remove();
            }
        }
        
        // Provide space for newborn trees.
        List<Tree> newTrees = new ArrayList<>();        
        // Let all trees act.
        for(Iterator<Tree> it = trees.iterator(); it.hasNext(); ) {
            Tree tree = it.next();
            tree.run(newTrees);
            if(! tree.isAlive()) {
                it.remove();
            }
        }
        
        List<Fire> newFires = new ArrayList<>();        
        // Let all trees act.
        for(Iterator<Fire> it = fires.iterator(); it.hasNext(); ) {
            Fire fire = it.next();
            fire.hunt(newFires);
            if(! fire.isAlive()) {
                it.remove();
            }
        }
        
        List<Deer> newDeers = new ArrayList<>();        
        // Let all trees act.
        for(Iterator<Deer> it = deers.iterator(); it.hasNext(); ) {
            Deer deer = it.next();
            deer.hunt(newDeers);
            if(! deer.isAlive()) {
                it.remove();
            }
        }
        
        // Add the newly born trees and grass to the main lists.
        grasses.addAll(newGrasses);
        trees.addAll(newTrees);
        deers.addAll(newDeers);
        fires.addAll(newFires);

        view.showStatus(step, field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        grasses.clear();
        trees.clear();
        deers.clear();
        fires.clear();
        populate();
        
        // Show the starting state in the view.
        view.showStatus(step, field);
    }
    
    /**
     * Randomly populate the field with trees and grass.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= TREE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Tree tree = new Tree(true, field, location);
                    trees.add(tree);
                }
                else if(rand.nextDouble() <= GRASS_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Grass grass = new Grass(true, field, location);
                    grasses.add(grass);
                }
                else if(rand.nextDouble() <= DEER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Deer deer = new Deer(true, field, location);
                    deers.add(deer);
                }
                else if(rand.nextDouble() <= FIRE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Fire fire = new Fire(true, field, location);
                    fires.add(fire);
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
}
