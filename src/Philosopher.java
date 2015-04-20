 import java.util.Random;
import java.util.concurrent.locks.*;
 
public class Philosopher extends Thread {
    private static int WAITING=0, EATING=1, THINKING=2;
    private Lock lock;
    private Condition phil [];
    private int states [];
    private static int eatCount [];
    private int NUM_PHILS;
    private int id;
    private final int TURNS = 20;
    
    public Philosopher (Lock l, Condition p[], int st[], int num, int id, int ec[]) {
        lock = l; phil = p; states = st; this.id = id; eatCount = ec;
        NUM_PHILS = num;
    }
    
    // Random sleeps are necessary because each philosopher is coupled with the two philosophers their left and right side
    // if there is a consistent time between taking sticks and putting sticks then there will always be a patter especially 
    // when the number of philosophers at the table is low. 
    public void run () {
        for (int k=0; k<TURNS; k++) {
        Random rand = new Random();
        int eatFor = rand.nextInt(21) + 5;
        int waitFor = rand.nextInt(51) + 5;
        takeSticks(id); 
        try { sleep(eatFor); } catch (Exception ex) { }
        putSticks(id);
        try { sleep(waitFor); } catch (Exception ex) { }
        //output();
        }
    }
    
    // This method finds the lowest eatCount value then compares it to the current threads 
    // eatCount if this current thread has not violated the policy which is each philosopher 
    // can only eat at most five more then its peers then the method returns true else returns false
    public boolean checkPhase(int eatC)
    {
    	int hungriest = eatC;
    	
    	// Iterate through eatCount array to find the lowest eatCount value
    	for(int i = 0; i<eatCount.length; i++)
    	{
    		if(eatCount[i] < hungriest)
    		{
    			hungriest = eatCount[i];
    		}
    	}
    	
    	// After finding the lowest eat count and calling that hungriest we take 
    	// the difference to see if the thread currently trying to eat has already eaten 
    	// this round. This logic ensures that every philosopher eats an equal amount of times
    	// compared to his peers. Therefore no thread falls behind and no thread starves.
    	if(eatC == 0)
    	{
    		return true;
    	}
    	else if((Math.abs((eatC - hungriest)) % 5 == 0) && eatC != hungriest)
		{
			return false;
		}
    	return true;
    	/*
    	 * if((Math.abs((eatC - hungriest)) >= 1)
    	 * To make our policy that each thread can only eat one more times then its peers
    	 */
    }
    
    public void takeSticks (int id) {
        lock.lock();
        try {
            if (states[leftof(id)]!=EATING && states[rightof(id)]!=EATING && checkPhase(eatCount[id]))
            	{
            		states[id] = EATING;
            		eatCount[id]++;
            		//System.out.println("Phil " + id + " eats" + " this many times " + eatCount[id]);
            	}
            else {
            states[id] = WAITING;
            //System.out.println("Phil " + id + " waiting" + " this many times " + eatCount[id]);
            phil[id].await();
            }
        }
        catch (InterruptedException e) {
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    } 
    
    public void output() {
        lock.lock();
        for (int k=0; k<states.length; k++)
        {
        	if(states[k] == 0)
        	{
        		System.out.println("P" + k + " is Waiting " + ", he has Eaten " + eatCount[k] + " times");
        	}
        	else if(states[k] == 1)
        	{
        		System.out.println("P" + k + " is Eating " + ", he has Eaten " + eatCount[k] + " times");
        	}
        	else
        	{
        		System.out.println("P" + k + " is Thinking " + ", he has Eaten " + eatCount[k] + " times");
        	}
        }
        System.out.println("Phase Over");
            
        lock.unlock();
        System.out.println();
    }
    
    public void putSticks (int id) {
        lock.lock();
        try {
            states[id] = THINKING;
            if (states[leftof(id)]==WAITING
            && states[leftof(leftof(id))]!=EATING && checkPhase(eatCount[leftof(id)])) {
                phil[leftof(id)].signal();
                states[leftof(id)] = EATING;
                eatCount[leftof(id)]++;
                //System.out.println("Phil " + leftof(id) + " eats" + " this many times " + eatCount[leftof(id)]);
            }
            if (states[rightof(id)] == WAITING
            && states[rightof(rightof(id))] != EATING && checkPhase(eatCount[rightof(id)])) {
                phil[rightof(id)].signal(); 
                states[rightof(id)] = EATING;
                eatCount[rightof(id)]++;
                //System.out.println("Phil " + rightof(id) + " eats" + " this many times " + eatCount[rightof(id)]);
            }
        } finally {
            lock.unlock();
        }
    }
    
    private int leftof (int id) { // clockwise
        int retval = id-1;
        if (retval < 0) // not valid id
            retval = NUM_PHILS-1;
        return retval;
    }
    
    private int rightof (int id) {
        int retval = id+1;
        if (retval == NUM_PHILS) // not valid id
            retval=0;
        return retval;
    }
}
