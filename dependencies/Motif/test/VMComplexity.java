package test;

import grank.mine.Answer;
import grank.pvalue.PValue;

import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.math.MathException;

public class VMComplexity {
	
	static int tot=1;
private static void closedHist(int[] x, int b, Vector < int[] > S ) {

          int[] h1 = new int[x.length];
          System.arraycopy(x, 0, h1, 0, x.length);
          Answer a = new Answer(h1, S.size(), 0);
 
          System.out.println(tot+":"+Arrays.toString(x));
      Vector<int[]> S1 = new Vector<int[]> (S.size());

      int[] x1 = new int[x.length];
      for (int pos = b; pos < x.length; pos++) {
    	  
        int ground = x[pos]; // minimum value at pos
        Arrays.fill(x1, Integer.MAX_VALUE); // next minimum value at pos
        S1.clear();
        for (int i = 0; i < S.size(); i++) {
          int[] H = S.elementAt(i); // a supporting database histogram
          if (H[pos] > ground) {
            S1.add(H);
            for (int j = 0; j < x.length; j++) {
              if ( H[j] < x1[j]) {
                x1[j] = H[j];
              }
            }
          }
        }

        if (S1.size() <1) { // constraint of support
            continue;
          }
        // check if it violates lexicographical order
        boolean dup_flag = false;
        for (int j = 0; j < pos; j++) {
          if (x1[j] > x[j]) {
            dup_flag = true;
            break;
          }
        }
        if (dup_flag) {
          continue;
        }
        //System.out.println("Comparing PValue: z1= "+z1+"env.hz= "+env.hZ);
        tot++;
        closedHist(x1, pos,  S1 );
      }
 }

	public static void main(String[] args)
	{
		int[] h1={4,1,4};
		int[] h2={4,2,4};
		int[] h3={4,3,4};
		int[] h4={4,4,1};
		int[] h5={4,4,2};
		int[] h6={4,4,3};
		int[] h7={4,4,4};
		int[] h8={1,4,4};
		int[] h9={2,4,4};
		int[]h10= {3,4,4};
		//int[] h9={2,3,3,3};
		Vector S=new Vector();
		S.add(h1);
		S.add(h2);
		S.add(h3);
		S.add(h4);
		S.add(h5);
		S.add(h6);
		S.add(h7);
		S.add(h8);
		S.add(h9);
		S.add(h10);
		int[] x={1,1,1};
		closedHist(x, 0, S);
	}
}
