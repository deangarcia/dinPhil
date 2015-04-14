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
    private static int starving = 0;
    public Philosopher (Lock l, Condition p[], int st[], int num, int id, int ec[]) {
        lock = l; phil = p; states = st; this.id = id; eatCount = ec;
        NUM_PHILS = num;
    }
    public void run () {
        for (int k=0; k<TURNS; k++) {
        //try { sleep(100); } catch (Exception ex) { /* lazy */}
        takeSticks(id); 
        try { sleep(20); } catch (Exception ex) { }
        putSticks(id);
        output();
        }
    }
    
    public boolean checkPhase(int eatC)
    {
    	int hungriest = eatC;
    	int hungryPos = 0;
    	for(int i = 0; i<eatCount.length; i++)
    	{
    		if(eatCount[i] < hungriest)
    		{
    			hungriest = eatCount[i];
    			hungryPos = i;
    		}
    	}
    	if((Math.abs((eatC - hungriest)) % 5 == 0) && eatC > hungriest)
		{
    		starving = hungryPos;
			return false;
		}
    	return true;
    }
    public void takeSticks (int id) {
        lock.lock();
        try {
            if (states[leftof(id)]!=EATING && states[rightof(id)]!=EATING && checkPhase(eatCount[id]))
            	{
            		states[id] = EATING;
            		eatCount[id]++;
            	}
            else {
            states[id] = WAITING;
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
            }
            if (states[rightof(id)] == WAITING
            && states[rightof(rightof(id))] != EATING && checkPhase(eatCount[rightof(id)])) {
                phil[rightof(id)].signal(); 
                states[rightof(id)] = EATING;
                eatCount[rightof(id)]++;
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