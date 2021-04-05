import java.util.HashMap;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 *
 * Code modified by Joel Lajoie-Corriveau, 40112335
 *
 * Some of the code was taken from class by Dr. Aiman Hanna
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	// Statuses of our philosophers. Note:
	// Thinking is the neutral state
	// hungry is the state where a philosopher wants to eat
	private enum Status { eating, hungry, thinking };
	// Number of chopsticks on the table. Also represents the number of philosophers.
	private int m_nbOfChopsticks = 0;
	// Map of the philosopher ids
	private HashMap<Integer, Integer> m_philosopherIds;
	private int m_currentPiId = 0;

	private Status[] m_philosophersState;

	private boolean m_someoneIsTalking = false;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		// Setting the number of chopsticks. 1 per philosopher
		m_nbOfChopsticks = piNumberOfPhilosophers;

		// Initializing our philosophers' statuses
		m_philosophersState = new Status[piNumberOfPhilosophers];

		for (int i = 0; i < m_philosophersState.length; i++) {
			m_philosophersState[i] = Status.thinking;
		}

		// Initializing our map
		m_philosopherIds = new HashMap<Integer, Integer>();
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		// Getting our internal ID
		int id = getId(piTID);

		// Declaring our intention to eat.
		m_philosophersState[id] = Status.hungry;

		// Calculating our neighbors
		int leftNeighbor, rightNeighbor;
		// Special cases if we are the first or last
		if(id == 0)
		{
			leftNeighbor = m_nbOfChopsticks - 1;
			rightNeighbor = id + 1;
		}
		else if(id == m_nbOfChopsticks - 1)
		{
			leftNeighbor = id - 1;
			rightNeighbor = 0;
		}
		else
		{
			leftNeighbor = id - 1;
			rightNeighbor = id + 1;
		}

		// If one of our neighbors is eating, we can't pickup the sticks.
		if(m_philosophersState[leftNeighbor] == Status.eating ||
		   m_philosophersState[rightNeighbor] == Status.eating)
		{
			do
			{
				try{
					wait();
				}
				catch (InterruptedException e)
				{
					// This signifies we have been woken up.

				}
			}
			// If we were woken up, and a neighbor is still eating, we go back to sleep.
			while(m_philosophersState[leftNeighbor] == Status.eating ||
					m_philosophersState[rightNeighbor] == Status.eating);
		}

		// If we are here, it means our neighbors are not eating!
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		// Getting our internal ID
		int id = getId(piTID);

		// Declaring we are done eating.
		m_philosophersState[id] = Status.thinking;

		// We notify everyone. We can't, in this case, choose the threads to notify,
		// and since some threads will wait on talking, and others on eating, we have to notify everyone.
		// It's okay though, because all threads will double check they can proceed before doing anything.
		notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		// If someone is already talking, we wait for them to finish, politely.
		while(m_someoneIsTalking)
		{
			try{
				wait();
			}
			catch (InterruptedException e)
			{
				// This signifies we have been woken up.

			}
		}

		// If we are here, it means no one is talking anymore!
		m_someoneIsTalking = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		// We signify no one is talking
		m_someoneIsTalking = false;
	}

	private synchronized int getId(final int piTID)
	{
		// Assigning IDs
		if(!m_philosopherIds.containsKey(piTID))
		{
			// If we haven't assigned all our ids yet
			if(m_currentPiId < m_nbOfChopsticks)
			{
				m_philosopherIds.put(piTID, m_currentPiId);
				m_currentPiId++;
			}
			else
			{
				// We have assigned all our ids, yet a new philosopher is at the table. This is invalid.
				throw new IllegalArgumentException("Couldn't assign an ID. Too many philosophers at the table for the number specified!");
			}
		}

		return m_philosopherIds.get(piTID);
	}
}

// EOF
