import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a deer.
 * Deers age, move, eat grasss, and die.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29
 */
public class Deer
{
    // Characteristics shared by all deeres (class variables).
    
    // The age at which a deer can start to breed.
    private static final int BREEDING_AGE = 15;
    // The age to which a deer can live.
    private static final int MAX_AGE = 150;
    // The likelihood of a deer breeding.
    private static final double BREEDING_PROBABILITY = 0.08;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // The food value of a single grass. In effect, this is the
    // number of steps a deer can go before it has to eat again.
    private static final int GRASS_FOOD_VALUE = 9;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).

    // The deer's age.
    private int age;
    // Whether the deer is alive or not.
    private boolean alive;
    // The deer's position.
    private Location location;
    // The field occupied.
    private Field field;
    // The deer's food level, which is increased by eating grasss.
    private int foodLevel;

    /**
     * Create a deer. A deer can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the deer will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Deer(boolean randomAge, Field field, Location location)
    {
        age = 0;
        alive = true;
        this.field = field;
        setLocation(location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
        }
        else {
            // leave age at 0
            foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
        }
    }
    
    /**
     * This is what the deer does most of the time: it hunts for
     * grasss. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newDeers A list to return newly born deeres.
     */
    public void hunt(List<Deer> newDeers)
    {
        incrementAge();
        incrementHunger();
        if(alive) {
            giveBirth(newDeers);            
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
                newLocation = field.freeAdjacentLocation(location);
            }
            // See if it was possible to move.
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Check whether the deer is alive or not.
     * @return True if the deer is still alive.
     */
    public boolean isAlive()
    {
        return alive;
    }

    /**
     * Return the deer's location.
     * @return The deer's location.
     */
    public Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the deer at the new location in the given field.
     * @param newLocation The deer's new location.
     */
    private void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
    
    /**
     * Increase the age. This could result in the deer's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this deer more hungry. This could result in the deer's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for grasss adjacent to the current location.
     * Only the first live grass is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        List<Location> adjacent = field.adjacentLocations(location);
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Grass) {
                Grass grass = (Grass) animal;
                if(grass.isAlive()) { 
                    grass.setDead();
                    foodLevel = GRASS_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this deer is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newDeers A list to return newly born deeres.
     */
    private void giveBirth(List<Deer> newDeers)
    {
        // New deeres are born into adjacent locations.
        // Get a list of adjacent free locations.
        List<Location> free = field.getFreeAdjacentLocations(location);
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Deer young = new Deer(false, field, loc);
            newDeers.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A deer can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }

    /**
     * Indicate that the deer is no longer alive.
     * It is removed from the field.
     */
    private void setDead()
    {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }
}
