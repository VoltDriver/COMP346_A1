
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/** Client class
 *
 * @author Kerly Titus
 */

public class Client extends Thread {
    
    private static int numberOfTransactions;   		/* Number of transactions to process */
    private static int maxNbTransactions;      		/* Maximum number of transactions */
    private static Transactions [] transaction; 	/* Transactions to be processed */
    private static Network objNetwork;          	/* Client object to handle network operations */
    private String clientOperation;    				/* sending or receiving */
    
	/** Constructor method of Client class
 	 * 
     * @return 
     * @param
     */
     Client(String operation)
     { 
       if (operation.equals("sending"))
       { 
           System.out.println("\n Initializing client sending application ...");
           numberOfTransactions = 0;
           maxNbTransactions = 100;
           transaction = new Transactions[maxNbTransactions];  
           objNetwork = new Network("client");
           clientOperation = operation; 
           System.out.println("\n Initializing the transactions ... ");
           readTransactions();
           System.out.println("\n Connecting client to network ...");
           String cip = objNetwork.getClientIP();
           if (!(objNetwork.connect(cip)))
           {   System.out.println("\n Terminating client application, network unavailable");
               System.exit(0);
           }
       	}
       else
    	   if (operation.equals("receiving"))
           { 
    		   System.out.println("\n Initializing client receiving application ...");
    		   clientOperation = operation; 
           }
     }
           
    /** 
     * Accessor method of Client class
     * 
     * @return numberOfTransactions
     * @param
     */
     public int getNumberOfTransactions()
     {
         return numberOfTransactions;
     }
         
    /** 
     * Mutator method of Client class
     * 
     * @return 
     * @param nbOfTrans
     */
     public void setNumberOfTransactions(int nbOfTrans)
     { 
         numberOfTransactions = nbOfTrans;
     }
         
    /** 
     * Accessor method of Client class
     * 
     * @return clientOperation
     * @param
     */
     public String getClientOperation()
     {
         return clientOperation;
     }
         
    /** 
     * Mutator method of Client class
	 * 
	 * @return 
	 * @param operation
	 */
	 public void setClientOperation(String operation)
	 { 
	     clientOperation = operation;
	 }
         
    /** 
     * Reading of the transactions from an input file
     * 
     * @return 
     * @param
     */
     public void readTransactions()
     {
        Scanner inputStream = null;     /* Transactions input file stream */
        int i = 0;                      /* Index of transactions array */
        
        try
        {
        	inputStream = new Scanner(new FileInputStream("transaction.txt"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File transaction.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine( ))
        {
            try
            {   transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next());            /* Read account number */
                transaction[i].setOperationType(inputStream.next());            /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
                transaction[i].setTransactionStatus("pending");                 /* Set current transaction status */
                i++;
            }
             catch(InputMismatchException e)
            {
                System.out.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }
            
        }
        setNumberOfTransactions(i);		/* Record the number of transactions processed */
        
        inputStream.close( );

     }
     
    /** 
     * Sending the transactions to the server 
     * 
     * @return 
     * @param
     */
     public void sendTransactions()
     {
         int i = 0;     /* index of transaction array */
         
         while (i < getNumberOfTransactions())
         {
             // While the network buffer is full, yield our CPU time.
             // We do the same in case the network is offline. We wait for it to come back online.
             // The same happens if the server is not connected (because otherwise, we send the transactions to the void.)
             while( objNetwork.getInBufferStatus().equals("full") ||
                    objNetwork.getNetworkStatus().equals("inactive") ||
                     objNetwork.getServerConnectionStatus().equals("disconnected") )
             {
                 yield();
             };

             transaction[i].setTransactionStatus("sent");   /* Set current transaction status */

            objNetwork.send(transaction[i]);                            /* Transmit current transaction */
            i++;
         }
         
    }
         
 	/** 
  	 * Receiving the completed transactions from the server
     * 
     * @return 
     * @param transact
     */
     public void receiveTransactions(Transactions transact)
     {
         int i = 0;     /* Index of transaction array */
         
         while (i < getNumberOfTransactions())
         {   //  
        	 while (objNetwork.getOutBufferStatus().equals("empty") || 
        			 objNetwork.getClientConnectionStatus().equals("disconnected") ||
        			 objNetwork.getNetworkStatus().equals("inactive"))
        	 {
        		 yield();
        	 };

            objNetwork.receive(transact);                               	/* Receive updated transaction from the network buffer */
            
            System.out.println(transact);                               	/* Display updated transaction */    
            i++;
         } 
    }
     
    /** 
     * Create a String representation based on the Client Object
     * 
     * @return String representation
     * @param 
     */
     public String toString() 
     {
    	 return ("\n client IP " + objNetwork.getClientIP() + " Connection status" + objNetwork.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
     }
    
    /** Code for the run method
     * 
     * @return 
     * @param
     */
    public void run()
    {   
    	Transactions transact = new Transactions();
    	long sendClientStartTime, sendClientEndTime, receiveClientStartTime, receiveClientEndTime;
    
	    /* Implement the code for the run method */

        // Checking what type of client thread we are.
        if(getClientOperation().equals("sending"))
        {
            // We are sending.

            // Initializing sending client
            sendClientStartTime = System.currentTimeMillis();

            sendTransactions();

            sendClientEndTime = System.currentTimeMillis();

            System.out.println("\n Terminating client sending thread - " + " Running time " + (sendClientEndTime - sendClientStartTime) + " milliseconds");
            this.interrupt();
        }
        else if (getClientOperation().equals("receiving"))
        {
            // We are receiving.
        	
        	// Initializing receiving client
            receiveClientStartTime = System.currentTimeMillis();

            receiveTransactions(transact);

            receiveClientEndTime = System.currentTimeMillis();

            System.out.println("\n Terminating client receiving thread - " + " Running time " + (receiveClientEndTime - receiveClientStartTime) + " milliseconds");

            objNetwork.disconnect(objNetwork.getClientIP());

            this.interrupt();
        }
    }
}
