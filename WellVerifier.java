import java.lang.*;
import java.lang.Math;
import java.util.Arrays;

import javax.lang.model.util.ElementScanner6;

/*This is the AI agent*/
public class WellVerifier {

    public Well[] model;            //mental model of the well-field
    public int testsSoFar;          //keeps track of how many tests we've used
    public int currentReward;       //keeps track of how far ahead/behind the trivial strategy we are
    public boolean defaultState;    //keeps track of what state we're in
    public int cleans;              //keeps track of how many clean wells we've seen so far
    public double estP;             //our current best-estimate for the unknown critical probability of cleanliness
    public WellGenerator generator; //dynamically generates new groups of wells using the unknown probability
    public int algorithm;           //use which algorithm?

    public int nextGroupSize;
    public Well[] nextGroup;

    //default constructor
    public WellVerifier()
    {
        model = new Well[0];
        testsSoFar = 0;
        currentReward = 0;
        defaultState = true;
        cleans = 0;
        estP = 0;
        generator = new WellGenerator();
        nextGroupSize = 1;
        algorithm = 2;
    }

    //constructor for custom probabilities
    public WellVerifier(WellGenerator gen, int alg)
    {
        model = new Well[0];
        testsSoFar = 0;
        currentReward = 0;
        defaultState = true;
        cleans = 0;
        estP = 0;
        generator = gen;
        nextGroupSize = 1;
        algorithm = alg;
    }

    /*Best Group Size function for algorithm 1*/
    public int bestGroupSize_alg1()
    {
        int size = 1;
        if (estP < 0.69336127435063470484335227478596179544593511345775403656586369340003543713242292453535221226358582890955719736219) {size = 1 ;}
        else {size = (int) Math.floor(1/(1-estP)) ;}
        return size;
    }

    /*Expected Reward function and Best Group Size function for algorithm 2*/
    public int bestGroupSize_alg2()
    {
        int size = 0;
        double current = 0;
        double next = 0;
        do
        {
            size++;
            current = expectedReward_alg2(size);
            next = expectedReward_alg2(size+1);
        } while (current <= next);
        return size;
    }
    public double expectedReward_alg2(int groupSize)
    {
        if (groupSize==1)
        {
            return 0;
        }
        else
        {
            double[] coeff = new double[groupSize+1];
            double coe = 0;
            for (int i=0; i<coeff.length; i++)
            {
                if (i==0)
                {
                    coeff[i] = groupSize - 1;
                    coe = groupSize - 1;
                }
                else if (i==1)
                {
                    coeff[i] = coe - 1;
                    coe = coe - 1;
                }
                else if (i == coeff.length-1)
                {
                    coeff[i] = 1 - groupSize;
                    coe = 1 - groupSize;
                }
                else
                {
                    coeff[i] = coe - 2;
                    coe = coe - 2;
                }
            }

            double[] terms = new double[groupSize+1];
            for (int i=0; i<terms.length; i++)
            {
                if (i==0)
                {
                    terms[i] = (double) (Math.pow(estP, groupSize))*coeff[i] ;
                }
                else
                {
                    double q = (double) (1-estP) ;
                    terms[i] = (double) (Math.pow(estP, groupSize-i))*q*coeff[i]  ;
                }
            }

            double finalSum = 0;
            for (int i=0; i<terms.length; i++)
            {
                finalSum += terms[i];
            }
            //System.out.println(finalSum);
            return finalSum;
        }
    }

    /*Group Examination protocol*/
    public void examineNewGroup()
    {
        if (defaultState == true)
        {
            switch (algorithm)
            {
                case 1: nextGroupSize = bestGroupSize_alg1(); break;
                case 2: nextGroupSize = bestGroupSize_alg2(); break;
            }
            System.out.println("Current estimate for P: "+estP);
            System.out.println("Sample will be taken of size "+nextGroupSize+":");
            nextGroup = generator.generate(nextGroupSize);

            if (nextGroupSize==1)
            {
                testsSoFar++;
                if (nextGroup[0].getState()==false)
                {
                    System.out.println("A contaminated well.");
                    model = concat(nextGroup);
                    printModel();
                    reportTests();
                    reportScore();
                    updateestP(0);
                }
                else 
                {
                    System.out.println("A clean well");
                    model = concat(nextGroup);
                    printModel();
                    reportTests();
                    reportScore();
                    updateestP(1);
                }
            }
            else
            {
                for (int i=0; i<nextGroupSize; i++)
                {
                    if (nextGroup[i].getState() == false)
                    {
                        //increment testsSoFar and augment reward
                        testsSoFar++;
                        currentReward--;
                        System.out.println("A contaminated sample of "+nextGroupSize+".");
                        printModel();
                        reportTests();
                        reportScore();
                        //transition and exit the method
                        transition();
                        return;
                    }
                    else if (i == nextGroupSize-1)
                    {
                        //increment testsSoFar and augment reward
                        testsSoFar++;
                        //System.out.println(testsSoFar);
                        currentReward += (nextGroupSize - 1);
                        System.out.println("A clean sample of "+nextGroupSize+".");
                        model = concat(nextGroup);
                        printModel();
                        //update the estimate for the p-value
                        updateestP(nextGroupSize);
                        //System.out.println(cleans);
                        //System.out.println("current estimate for p: " + estP);
                        reportTests();
                        reportScore();
                    }
                }
            }
        }
        else
        {
            nextGroupSize--;
            System.out.println("Current estimate for P: "+estP);
            System.out.println("Sample will be taken of size "+nextGroupSize+":");
            if (nextGroupSize==1) 
            {
                if (nextGroup[0].getState()==false)
                {
                    testsSoFar++;
                    System.out.println("A contaminated well.");
                    model = concat(Arrays.copyOfRange(nextGroup, 0, 1));
                    printModel();
                    reportTests();
                    reportScore();
                    updateestP(0);
                }
                else
                {
                    testsSoFar++;
                    currentReward++;
                    System.out.println("A clean well. The next one must be contaminated.");
                    model = concat(Arrays.copyOfRange(nextGroup, 0, 2));
                    printModel();
                    reportTests();
                    reportScore();
                    updateestP(1);
                }
                transition();
            }
            else
            {
                for (int i=0; i<nextGroupSize; i++)
                {
                    if (nextGroup[i].getState() == false)
                    {
                        //increment testsSoFar and augment reward
                        testsSoFar++;
                        currentReward--;
                        System.out.println("A contaminated sample of "+nextGroupSize+".");
                        printModel();
                        reportTests();
                        reportScore();
                        //exit the method
                        return;
                    }
                    else if (i == nextGroupSize - 1)
                    {
                        //increment testsSoFar and augment reward
                        testsSoFar++;
                        //System.out.println(testsSoFar);
                        currentReward += nextGroupSize;
                        System.out.println("A clean sample of "+nextGroupSize+". The next one must be contaminated.");
                        model = concat(Arrays.copyOfRange(nextGroup, 0, nextGroupSize+1));
                        printModel();
                        //update the estimate for the p-value
                        updateestP(nextGroupSize);
                        //System.out.println(cleans);
                        //System.out.println("current estimate for p: " + estP);
                        reportTests();
                        reportScore();
                        transition();
                    }
                }
            }
        }
    }

    //transitions the agent from one state to the other
    public void transition()
    {
        if (defaultState == true) {defaultState = false;}
        else {defaultState = true;} 
    }

    //updates the estimate for p
    public void updateestP(int newCleans)
    {
        cleans += newCleans;
        estP = (double) cleans/(model.length + 1);
    }

    //updates the model
    public Well[] concat(Well[] newGroup)
    {
        int modelLength = model.length;
        int newGroupLength = newGroup.length;
        Well[] newModel = new Well[modelLength + newGroupLength];
        System.arraycopy(model, 0, newModel, 0, modelLength);
        System.arraycopy(newGroup, 0, newModel, modelLength, newGroupLength);

        return newModel;
    }

    //prints the model
    public void printModel()
    {
        for (int i=0; i<model.length; i++)
        {
            if (model[i].getState()==false) {System.out.print("X");}
            else {System.out.print("O");}
        }
        System.out.println();
    }

    //reports the number of tests ahead of the trivial strategy we are
    public void reportScore()
    {
        System.out.println("Score: "+currentReward);
    }

    //reports the number of tests used
    public void reportTests()
    {
        System.out.println("Tests so far: "+testsSoFar);
    }
}