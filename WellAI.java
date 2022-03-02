// *****************************************************************************************
// Title:  Well Identification System
// File:   WellAI.java
// Author: Brian Burtner, Natalie Shafer, Rachel Shafer
// Description:  
/*                This program will simulate the process of testing the water of
                  a dynamically generated field of water wells, with an unknown but constant
                  probability p of cleanliness (and probability q of contamination) by aggregating 
                  water samples to verify the wells' status as rapidly/efficiently as possible 
*/
//*******************************************************************************************

import java.util.Random ;
import java.util.Scanner ;
import javax.swing.JOptionPane ;
import java.io.File ;
import java.io.IOException ;
import java.io.PrintWriter ;

public class WellAI {

   public static void main(String[] args) 
   {
      Scanner query = new Scanner(System.in) ; 

      /*Which algorithm should the agent use? (2 is optimal; 1 is decent but suboptimal)*/
      System.out.println("Which algorithm will be used (1 or 2)?");
      int algorithm = query.nextInt(); query.nextLine();

      /*Ok, so what we're doing here is some division of labor.  We don't want the WellVerifier Agent to have 
      direct access to the critP information, so instead we pass this info to a seperate object (a generator).
      The generator stores critP as a private variable and uses it to generate the real field of wells.
      Then we give the Agent an instance of that generator to use as needed (allowing us to grow the real field
      dynamically as opposed to initializing it all at once). */
      System.out.println("Enter the critical probability of well-cleanliness:");
      WellGenerator generator = new WellGenerator(query.nextDouble()); query.nextLine();
      WellVerifier Agent = new WellVerifier(generator, algorithm);

      /*Here begins the 'main menu' do-while loop, where we will tell our AI what to do. */
      System.out.println("Enter 'loop' to perform a fixed number of tests, or enter 'single' to perform them one at a time:");
      String mode = query.nextLine();

      if (mode.contentEquals("single"))
      {
         boolean run = true;
         do
         {
            System.out.println("Enter 'true' to perform the next advisable test.");
            if (query.nextBoolean() == true)
            {
               //continue the loop
            } else {run = false;}
            if (run == false) {break;}
            System.out.println();
            Agent.examineNewGroup();
            System.out.println("Wells verified: "+Agent.model.length);
         } while (run == true);
      }
      else if (mode.contentEquals("loop"))
      {
         System.out.println("Enter the number of tests to be performed:");
         int number = query.nextInt();  query.nextLine();
         for (int i=0; i<number; i++)
         {
            System.out.println();
            Agent.examineNewGroup();
         }
         System.out.println("Wells verified: "+Agent.model.length);
      }
      query.close();
   }
}