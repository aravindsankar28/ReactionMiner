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

public class RandomWalkV3 extends RandomWalk{

	/**
	 * @param args
	 */
	Vector[] histSet;
	Graph[] graphdb;
	public RandomWalkV3(double restart)
	{
		super(restart);
	}	
	
	public Vector[] getHistsBySet(Graph[] graphdb) {
		// TODO Auto-generated method stub
		//profile=new double[V.length][offset+topEdges.size()];
		System.out.println("graph length "+graphdb.length);
		for(int i=0;i<graphdb.length;i++)
		{
			System.out.println(graphdb[i]);
			walkSet2(graphdb[i]);
		}
		//for(int i=0;i<histSet.length;i++)
			//System.out.println(vertexDictionary.labels.get(i)+"-"+histSet[i].size());
		return histSet;
	}
	  private void walkSet(Graph g) {
		// TODO Auto-generated method stub
		  
		  Matrix mat=getAdjMatrix(g);
		 
		  
		  for(int i=0;i<g.V.length;i++)
		  {
			  double[][] s = new double[g.V.length+1][1];
			  s[g.V.length][0] = 1;
			  Matrix S = new Matrix(s);
			  System.out.println(g.V[i].label);
			  double[][] stat=walk(mat,S,i);
		  }

	}

	public Matrix getAdjMatrix(Graph g)
	  {
			//A=new Matrix(start,start);
		  
	        //System.out.println("coming adjacent 1");
		System.out.println(g);
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
		    while(this.Dist(X, Xcopy)>0.0005 || m++==0)
		    {
		     	Matrix N=M.transpose();
		  	  	//System.out.println("Check Distance:"+this.Dist(N, M));

		        Xcopy=new Matrix(X.getArray());
		        
		        X = (N.times(X)).times(1-restart).plus(S.times(restart));
		        //if(d.Dist(X, Xcopy) < 0.00000000001)
		        
		  
		            //System.out.print(d.Dist(X, Xcopy));
		        
		    }
			  
		    //System.out.println("Distance:"+this.Dist(X, Xcopy));
			double[][] x= X.getArray();
			double sum=0;
			for(int i=0;i<x.length-1;i++)
				//System.out.print(x[i][0]+"\t");
				sum+=x[i][0];
			System.out.println(x[x.length-1][0]);
			//x[c][0]+=restart;
			for(int i=0;i<x.length-1;i++)
				System.out.print(x[i][0]+"\t");
			System.out.println();
			for(int i=0;i<x.length-1;i++)
				System.out.print((x[i][0]/sum)+"\t");
			System.out.println("\n------------------------------");
		    return X.getArray();
			
		}
	
		public void walkSet2(Graph g)
		{
			//double[][] adj=g.getAdjMatrix();
			//System.out.println("Performing Random Walk Version 1");
			
			Vertex[] V=g.V;
			Edge[] E=g.E;
			
			for(int i=0;i<V.length;i++)
			{
				//System.out.println(i);
				//Arrays.fill(profile[i],0); //initial profile counter to 0
				//int tours=0;
				int steps=0;
				int actsteps=0;
				double[] profile=new double[V.length];
				double[] eprofile=new double[E.length];
				if(V[i].edges.size()>0)
				{
					Vertex curr=V[i]; //current Vertex
					while(steps<10000)
					{
							double prob=Math.random();
							if(prob<=restart) //if probability is less than restart, go back to starting vertex
							{
								curr=V[i];
								profile[curr.id]++;
								
								//System.out.println("restart happened"+i);
							}
							else
							{
								actsteps++;
								prob-=restart;
								double delta=(1-restart)/(curr.edges.size());
								int next=new Double(Math.floor(prob/delta)).intValue(); //edge ID
								Edge selected=E[(Integer)curr.edges.elementAt(next)];
								curr=V[selected.getNeighbor(curr.id)];//next Vertex
								eprofile[selected.id]++;
								profile[curr.id]++;

							}
							steps++;
						}
						//System.out.println(dist);
					}
					for(int j=0;j<profile.length;j++)
					{
						profile[j]/=steps;
					}
					for(int j=0;j<eprofile.length;j++)
					{
						eprofile[j]/=steps;
					}	
					
					for(int j=0;j<profile.length;j++)
					{
						//System.out.print(profile[j]+"\t");
					}
					System.out.println();
					int[] ieprofile=approximate(eprofile,10);
					for(int j=0;j<ieprofile.length;j++)
					{
						System.out.print(ieprofile[j]+"\t");
					}	
					System.out.println("-------------------------");
				
			}
			
			//return approximate(profile,10);
		}

}
