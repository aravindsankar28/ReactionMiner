package tool;



import grank.transform.Hist;
import graph.Edge;
import graph.Graph;
import graph.Vertex;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import Dictionary.argumentsDictionary;
import Dictionary.vertexDictionary;
import Jama.Matrix;

public class RandomWalkV2 extends RandomWalk{

	/**
	 * @param args
	 */
	Vector[] histSet;
	Graph[] graphdb;
	public RandomWalkV2(double restart)
	{
		super(restart);
	}	
	
	public Vector[] getHistsBySet(Graph[] graphdb) {
		// TODO Auto-generated method stub
		//profile=new double[V.length][offset+topEdges.size()];
		int offset=vertexDictionary.labels.size();
		histSet=new Vector[offset];
		this.graphdb=graphdb;
		for(int i=0;i<offset;i++)
		{
			histSet[i]=new Vector<Hist>();
		}
		for(int i=0;i<graphdb.length;i++)
		{
			walkSet(i);
		}
		//for(int i=0;i<histSet.length;i++)
			//System.out.println(vertexDictionary.labels.get(i)+"-"+histSet[i].size());
		return histSet;
	}
	  private void walkSet(int index) {
		// TODO Auto-generated method stub
		  Matrix mat=getAdjMatrix(index);
		  Graph g=graphdb[index];
		  
		  for(int i=0;i<g.V.length;i++)
		  {
			  double[][] s = new double[g.V.length+1][1];
			  s[g.V.length][0] = 1;
			  Matrix S = new Matrix(s);
			  double[][] stat=walk(mat,S,i);
		  }

	}

	public Matrix getAdjMatrix(int index)
	  {
			//A=new Matrix(start,start);
		  
	        //System.out.println("coming adjacent 1");
	        Graph g=graphdb[index];
	        int size=g.V.length;//vertexDictionary.labels.size()+topEdges.size();
	        
			for(int i=0;i<g.E.length;i++)
				if(topEdges.containsKey(g.E[i].type))
					size++; 
			int[] edgeLabels=new int[size*2];
		    //double[][] mat=new double[size*2+g.V.length+1][size*2+g.V.length+1];
			double[][] mat=new double[g.V.length+1][g.V.length+1];
		    //Arrays.fill(mat,0.0);
			for(int i=0;i<mat.length;i++)
			{
					mat[i][mat.length-1]=restart;
			}

			int pos=0;
			for(int i=0;i<g.V.length;i++)
			{
				Vertex curr=g.V[i];
				if(g.V[i].edges.size()>0)
				{
					double prob=(1-restart)/g.V[i].edges.size();
					for(int j=0;j<curr.edges.size();j++)
					{
						Edge selected=g.E[(Integer)curr.edges.elementAt(j)];
						Integer edgePos=(Integer)topEdges.get(selected.type);
						/*if(edgePos!=null)
						{
							edgeLabels[pos]=selected.type;
							mat[i][g.V.length+pos]=prob;
							mat[pos][selected.getNeighbor(curr.id)]=1;
							pos++;
						}
						else*/
						{
							mat[i][selected.getNeighbor(curr.id)]=prob;
						}
					}
				}
			}
	 
			return new Matrix(mat);
	  }
	  public double Dist(Matrix A, Matrix B)
	  {
	      double sum = 0;
	      Matrix C = A.minus(B);
	      for(int i  = 0; i < A.getRowDimension(); i++ )
	          for(int j = 0; j< A.getColumnDimension(); j++)
	              sum += Math.abs(C.get(i, j));
	      return sum;         
	  }
	  public double[][] walk(Matrix M, Matrix S, int c) {
			// TODO Auto-generated method stub
		    Matrix X = S.copy();
		    Matrix Xcopy = S.copy();
		    
		    for(int i=0;i<M.getColumnDimension();i++)
		    	M.set(M.getColumnDimension() -1, i, 0);
		    
		    M.set(M.getColumnDimension() -1, c, 1);
		    int m=0;
		    while(this.Dist(X, Xcopy)>0.05 || m++==0)
		    {
		     	Matrix N=M.transpose();
		  	  	//System.out.println("Check Distance:"+this.Dist(N, M));

		        Xcopy=new Matrix(X.getArray());
		        
		        X = (N.times(X)).times(1-restart).plus(S.times(restart));
		        //if(d.Dist(X, Xcopy) < 0.00000000001)
		        
		  
		            //System.out.print(d.Dist(X, Xcopy));
		        
		    }
			  
		    //System.out.println("Distance:"+this.Dist(X, Xcopy));

		    return X.getArray();
			
		}
	


}
