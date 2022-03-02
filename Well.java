public class Well 
{

   //Well Attributes
   private boolean clean ;  //variable to store the status of the well (clean or dirty)
    
   //constructors
   public Well() {
      clean = true; ;
   }

   public Well(boolean d) {
    setClean(d) ;
 }
   
   //Mutator
   public void setClean(boolean c) {
      this.clean = c ;
   }
   
   //Accesses the cleanliness status of the well
   public boolean getState() {
      return this.clean ;
   }
   
   //complementary toString method
   public String toString() {
      String result = "\nIs the well water clean?: "+getState() ;
      return result;
   }
   
}