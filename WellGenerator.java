import java.util.Random ;

/*This is our middle-man.  He knows the critP so the agent doesn't have to.
Does all the work of generating wells for our agent to make estimates with*/
public class WellGenerator
{
   private double critP;

   //default constructor
   public WellGenerator()
   {
      critP = 1;
   }

   //constructor for custom probabilities
   public WellGenerator(double input)
   {
      critP = input;
   }

   //We'd like this to populate a new subgroup of happy little wells
   public Well[] generate(int size)
   {
      Random r = new Random();
      Well[] newWells = new Well[size];

      for (int i=0; i<size; i++)
      {      
         if (r.nextDouble() < critP) 
         {
            newWells[i] = new Well();
            //System.out.print("O") ;
         }
         else 
         {
            newWells[i] = new Well(false) ; 
            //System.out.print("X") ;
         }
      }
      //System.out.println();
      return newWells;
   }
}