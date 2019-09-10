package tool;
import java.io.FileNotFoundException;
import java.io.IOException;

import Dictionary.vertexDictionary;

import ctree.alg.*;
import graph.*;

import java.util.HashMap;
import java.util.Vector;

import openBabel.MoleculeLoader;

import joelib2.io.MoleculeIOException;
import joelib2.molecule.Molecule;
public class RebuildGraph {
	
	public static HashMap labels;		// HashMap to keep track of old / new labels
	public static Graph[] database;		// Array of graphs in database
	public static Graph[] fgroups;		// Array of graphs of functional groups
	public static boolean relabelAtoms=false;	
	/*public RebuildGraph(String data, int numdata, String funcgroups, int numgroups) throws FileNotFoundException
	{
		database = BuildGraph2.loadGraphs(data,numdata);
		fgroups = BuildGraph2.loadGraphs(funcgroups,numgroups);
		labels=(HashMap) vertexDictionary.labels.clone();
	}*/
	
	public RebuildGraph(String data, String funcgroups,boolean relabel) throws IOException, MoleculeIOException
	{
		database = BuildGraph.loadGraphs(data);
		relabelAtoms=relabel;
		if(relabelAtoms)
			MoleculeLoader.load(database);

		fgroups = BuildGraph.loadGraphs(funcgroups);
		labels=(HashMap) vertexDictionary.labels.clone();
	}
	// Copies contents of source matrix (s) to destination matrix (d)
	//		d must be larger than s
	public static void pasteMatrix(int[][] s, int[][] d)
	{
		for(int i=0;i<s.length;i++)
			d[i]=s[i].clone();
	}
	
	// Returns adjacency matrix of Graph g
	public static int[][] adjMatrix(Graph g)
	{
		int Hcount = g.H.size();
		int numEdges = g.E.length;
		int numAtoms = g.V.length;
		//System.out.println(g.id+' '+Hcount);
		//int[][] adjMatrix=new int[numAtoms-Hcount][numAtoms-Hcount];
		int[][] adjMatrix=new int[numAtoms][numAtoms];
		for(int i=0;i<numEdges;i++)
		{
			Edge e = g.E[i];
			if( Hcount==0 || (!g.H.contains(e.node1) && !g.H.contains(e.node2)))
			{
				adjMatrix[e.node1][e.node2]=e.bond;
				adjMatrix[e.node2][e.node1]=e.bond;
			}
		}
			return adjMatrix;
	}
	
	// Returns a clone of graph g
	public static Graph copyGraph(Graph g)
	{
		Vertex[] newV = new Vertex[g.V.length];
		for(int i=0; i<g.V.length; i++)
		{
			newV[i] = new Vertex(g.V[i].label  , g.V[i].id);
			newV[i].edges = (Vector) g.V[i].edges.clone();
		}
		
		Edge[] newE = new Edge[g.E.length];
		for(int i=0; i<g.E.length; i++)
			newE[i] = new Edge(g.E[i], g.E[i].node1, g.E[i].node2, g.E[i].id);
		
		Vector newH = new Vector();
		newH = (Vector)g.H.clone();
		
		Graph newG = new Graph(newV, newE, g.id, newH,g.index);
				
		return newG;
	}
	
	// Sorts the functional groups (fgroups[]) in accending (direction = 0)
	//	or decending (direction = 1) order.
	//	or accending in frequency (direction = 2)
	//  or decending in frequency (direction = 3)
	public static void sortFG(int direction)
	{
		int n = fgroups.length;
		
		if(direction == 0)
		{
			for (int pass=1; pass < n; pass++) 
		    {  // count how many times
		        // This next loop becomes shorter and shorter
		        for (int i=0; i < n-pass; i++) 
		        {
		            if (fgroups[i].V.length > fgroups[i+1].V.length) 
		            {
		                // exchange elements
		                Graph temp = fgroups[i];  
		                fgroups[i] = fgroups[i+1];  
		                fgroups[i+1] = temp;
		            }
		        }
		    }
			for(int i=0; i<fgroups.length; i++)
				System.out.println(i + ") " + fgroups[i].id + ":   " + fgroups[i].V.length);
		}
		
		if(direction == 1)
		{
			for (int pass=1; pass < n; pass++) 
		    {  // count how many times
		        // This next loop becomes shorter and shorter
		        for (int i=0; i < n-pass; i++) 
		        {
		            if (fgroups[i].V.length < fgroups[i+1].V.length) 
		            {
		                // exchange elements
		                Graph temp = fgroups[i];  
		                fgroups[i] = fgroups[i+1];  
		                fgroups[i+1] = temp;
		            }
		        }
		    }
			for(int i=0; i<fgroups.length; i++)
				System.out.println(i + ") " + fgroups[i].id + ":   " + fgroups[i].V.length);
		}
		
		if(direction == 2)
		{
			int FGcount[] = freq();
			for (int pass=1; pass < n; pass++) 
		    {  // count how many times
		        // This next loop becomes shorter and shorter
		        for (int i=0; i < n-pass; i++) 
		        {
		            if (FGcount[i] > FGcount[i+1]) 
		            {
		                // exchange elements
		                Graph temp = fgroups[i];  
		                fgroups[i] = fgroups[i+1];  
		                fgroups[i+1] = temp;
		                
		                int temp2 = FGcount[i];  
		                FGcount[i] = FGcount[i+1];  
		                FGcount[i+1] = temp2;
		            }
		        }
		    }
			for(int i=0; i<fgroups.length; i++)
				System.out.println(i + ") " + fgroups[i].id + "  " + FGcount[i]);
		}
		
		if(direction == 3)
		{
			int FGcount[] = freq();
			for (int pass=1; pass < n; pass++) 
		    {  // count how many times
		        // This next loop becomes shorter and shorter
		        for (int i=0; i < n-pass; i++) 
		        {
		            if (FGcount[i] < FGcount[i+1]) 
		            {
		                // exchange elements
		                Graph temp = fgroups[i];  
		                fgroups[i] = fgroups[i+1];  
		                fgroups[i+1] = temp;
		                
		                int temp2 = FGcount[i];  
		                FGcount[i] = FGcount[i+1];  
		                FGcount[i+1] = temp2;
		            }
		        }
		    }
			for(int i=0; i<fgroups.length; i++)
				System.out.println(i + ") " + fgroups[i].id + "  " + FGcount[i]);
		}
		
	}
	
	public static int[][] OLDadjMatrix(Graph g)
	{
		int numEdges = g.E.length;
		int numAtoms = g.V.length;
		int[][] adjMatrix=new int[numAtoms][numAtoms];
		for(int i=0;i<numEdges;i++)
		{
			adjMatrix[g.E[i].node1][g.E[i].node2]=g.E[i].bond;
			adjMatrix[g.E[i].node2][g.E[i].node1]=g.E[i].bond;
		}
			return adjMatrix;
	}
	
	public static boolean OLDmappable(Graph q, Vertex Q, Graph d, Vertex D)
	{
		if(Q.label!=D.label)	return false;
		if(Q.edges.size() > D.edges.size())		return false;
		
		//check Database vertex has the distinct bond types contained in Query vertex
		int bondsQ = Q.edges.size();
		int bondsD = D.edges.size();
		HashMap typesQ=new HashMap();
		HashMap typesD=new HashMap();
		
		for(int i=0;i<bondsQ;i++)
		{
				Integer key = q.E[Q.edges.elementAt(i)].type;
				if(typesQ.containsKey(key))
				{
					Integer val = (Integer)typesQ.get(key);
					typesQ.put(key, ++val);
				}
				else	typesQ.put(key, 1);
		}
		
		for(int i=0;i<bondsD;i++)
		{
				Integer key = d.E[D.edges.elementAt(i)].type;
				if(typesD.containsKey(key))
				{
					Integer val = (Integer)typesD.get(key);
					typesD.put(key, ++val);
				}
				else	typesD.put(key, 1);
		}
		
		if(typesQ.size() > typesD.size())	return false;
		
		// database vertex must of more (or same) number of each distinct bond type than functional group vertex
		Integer keys[] = (Integer[])typesQ.keySet().toArray(new Integer[0]);
		int numTypes = keys.length;
		for(int i=0; i<numTypes; i++)
			if ( !typesD.containsKey(keys[i]) ||
				((Integer)typesD.get(keys[i]) < (Integer)typesQ.get(keys[i])))
					return false;
		
		return true;
	}
	
//	 Input: Array of Graphs in Database, Array of Functional Groups
	// Output:  Array of Graphs with functional groups replaced
	public static Graph[] OLDreplaceFG()
	{
		// newData is array of new graphs with replaced Functional Groups
		Graph[] newData = new Graph[database.length];
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
				
		// Generate all of the Adjacency Matrices for Functional Groups
		int[][][] FGadjMatrices = new int[fgroups.length][][];
		for(int i=0; i<fgroups.length; i++)
			FGadjMatrices[i] = OLDadjMatrix(fgroups[i]);
		
		// For each graph is database
		for(int h=0; h < database.length; h++)
		{
			
			Graph D =  copyGraph(database[h]);			// Make copy of Database Graph
			int[][] adjD = OLDadjMatrix(D);			// Generate Mapping matrix of Database Graph
			Vector<Vertex> V = new Vector<Vertex>();	// V is vector of new Verticies of new graph
			Vector<Edge> E = new Vector<Edge>();		// E is vector of new Edges of the new graph
			int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
						
			// For each Functional Group
			for(int k=0; k < fgroups.length; k++)
			{
				// if Functional Group if bigger than Database graph, goto the next Functional Group
				int Qsize = fgroups[k].V.length;
				if (Qsize > Dsize)	continue; 
	
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(OLDmappable(fgroups[k], fgroups[k].V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
			
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(FGadjMatrices[k], adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
					
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
					
					//Add vertex with FG name to the new vertex vector (V)
					V.add( new Vertex(fgroups[k].id, V.size()) );
					
					//For all mapped Verticies
					for(int i=0; i<map.length; i++)				
					{											
						D.V[map[i]].label = -1;					//  label it -1 (because it wont appear in new Graph)
						D.V[map[i]].id = V.lastElement().id;	//	and replace its ID with ID of functional group
					}
				}
			}
	
			// If no mappings, new Graph = old Graph
			if(V.size() == 0) {newData[h] = D; continue;}

			//Create new Vertex vector
			//For each Vertex in old graph
			for(int i=0; i<D.V.length; i++)
			{
				//If Vertex has been mapped
				if( D.V[i].label == -1 )
				{
					//For each edge of that vertex
					for(int j=0; j<D.V[i].edges.size(); j++)
					{
						//If neighboring vertex is mapped, change ID of edge to -2
						//	because it wont appear in new Graph
						if( D.V[i].id == D.V[D.E[D.V[i].edges.elementAt(j)].getNeighbor(i)].id ) 
							D.E[D.V[i].edges.elementAt(j)].id = -2;
					}
				}
				//If Vertex has NOT been mapped
				else
				{ 	//Add vertex to vector of new Verticies (V)
					V.add( new Vertex((String)vertexDictionary.labels.get(D.V[i].label), V.size()) );
					D.V[i].id = V.lastElement().id;				
				}
			}
			
			//	copy Vertex vector into new graph verticies
			Vertex[] newV = new Vertex[ V.size() ];	
			V.copyInto(newV);	
			
			//For each edge in old graph
			for(int i=0;i<D.E.length;i++)
				if(D.E[i].id!=-2) //If Edge has unmapped nodes
				{	
					//Put edge in new Edge vector (E) and update new verticies in new graph
					Edge e = D.E[i];
					int vlabel1= newV[D.V[e.node1].id].label;
					int vlabel2= newV[D.V[e.node2].id].label;
					int node1 =  newV[D.V[e.node1].id].id;
					int node2 =  newV[D.V[e.node2].id].id;
					if(vlabel1>vlabel2)
					{
						int temp=vlabel1;
						vlabel1=vlabel2;
						vlabel2=temp;
					}
					newV[ D.V[e.node1].id ].edges.add(E.size());
					newV[ D.V[e.node2].id ].edges.add(E.size());		
					E.add(new Edge(node1, node2, e.bond, vlabel1+"-"+vlabel2+"-"+e.bond, E.size() ));
				}
			
						

			// Copy new Edge vector into new graph Edges
			Edge[] newE = new Edge[ E.size() ];
			E.copyInto(newE);
			
			//	Prepare new Graph
			newData[h] = new Graph(newV, newE, D.id,D.index);			
		}
		
		return newData;
	}

	
	// Returns true if Vertex Q in graph q is mappable to Vertex D in Graph d
	// Returns true if Vertex Q in graph q is mappable to Vertex D in graph d
	public static boolean mappable(Graph q, Vertex Q, Graph d, Vertex D)
	{
		int Hcount = q.H.size();
		if(Q.label!=D.label)							return false;
		if(Q.edges.size()-Hcount > D.edges.size())		return false;
		int Hbond = 0;
		//check Database vertex has the distinct bond types contained in Query vertex
		int bondsQ = Q.edges.size();
		int bondsD = D.edges.size();
		HashMap typesQ=new HashMap();
		HashMap typesD=new HashMap();		
		
		for(int i=0;i<bondsQ;i++)
		{
			//System.out.println(labels.get(Q.label) + "  " + labels.get( q.V[q.E[Q.edges.elementAt(i)].getNeighbor(Q.id)].label));
				if((Hcount!=0) && ((String) (labels.get( q.V[q.E[Q.edges.elementAt(i)].getNeighbor(Q.id)].label ))).equals("h") )
					Hbond++;
				else
				{
					Integer key = q.E[Q.edges.elementAt(i)].type;
					if(typesQ.containsKey(key))
					{
						Integer val = (Integer)typesQ.get(key);
						typesQ.put(key, ++val);
					}
					else	typesQ.put(key, 1);
				}
		}
		
		if(Hbond > 0)
		{
			int valence = 0;
			String label = (String)labels.get(D.label);
			if(label.equals("b"))	valence = 5;
			if(label.equals("c") || label.equals("si"))	valence = 4;
			if(label.equals("n") || label.equals("p"))	valence = 3;
			if(label.equals("o") || label.equals("s")) valence = 2; 
		
			int bonds = 0;
			for(int i = 0; i<bondsD; i++)
			{
				int type=d.V[d.E[D.edges.elementAt(i)].getNeighbor(D.id)].label ;
				if( type==-1 || ! ((String) (labels.get(type))).equals("h"))	
					bonds = bonds + d.E[D.edges.elementAt(i)].bond;
			}
			//System.out.println("HCOUNT: " + (valence - bonds) + "   " + Hcount);
			if( (valence!=0) && ((valence-bonds)!=Hbond) )
				return false;
		}
		
		for(int i=0;i<bondsD;i++)
		{
				Integer key = d.E[D.edges.elementAt(i)].type;
				if(typesD.containsKey(key))
				{
					Integer val = (Integer)typesD.get(key);
					typesD.put(key, ++val);
				}
				else	typesD.put(key, 1);
		}
		
		if(typesQ.size() > typesD.size())	return false;
	
		// database vertex must of more (or same) number of each distinct bond type than functional group vertex
		Integer keys[] = (Integer[])typesQ.keySet().toArray(new Integer[0]);
		int numTypes = keys.length;
		for(int i=0; i<numTypes; i++)
			if ( !typesD.containsKey(keys[i]) ||
				((Integer)typesD.get(keys[i]) < (Integer)typesQ.get(keys[i])))
					return false;
		
		return true;
	}
	
		
	// Input: Array of Graphs in Database, Array of Functional Groups
	// Output:  Array of Graphs with functional groups replaced
	public static Graph[] replaceFG()
	{
		// newData is array of new graphs with replaced Functional Groups
		Graph[] newData = new Graph[database.length];
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
		
		// Generate all of the Adjacency Matrices for Functional Groups
		int[][][] FGadjMatrices = new int[fgroups.length][][];
		for(int i=0; i<fgroups.length; i++)
			FGadjMatrices[i] = adjMatrix(fgroups[i]);
		
		// For each graph is database
		for(int h=0; h < database.length; h++)
		{
			Graph D =  copyGraph(database[h]);			// Make copy of Database Graph
			Graph LD=null;
			if(relabelAtoms)
			{
				LD=copyGraph(database[h]);
				LD.labelAtomsWithJOELibTyper();
			}
			int[][] adjD = adjMatrix(D);			// Generate Mapping matrix of Database Graph
			Vector<Vertex> V = new Vector<Vertex>();	// V is vector of new Verticies of new graph
			Vector<Edge> E = new Vector<Edge>();		// E is vector of new Edges of the new graph
			int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
						
			// For each Functional Group
			for(int k=0; k < fgroups.length; k++)
			{
				// if Functional Group if bigger than Database graph, goto the next Functional Group
				int Qsize = fgroups[k].V.length;
				int Hcount = fgroups[k].H.size();
				if (Qsize-Hcount > Dsize)	continue;
				
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(mappable(fgroups[k], fgroups[k].V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
				
				
				
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(FGadjMatrices[k], adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
		
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
					
					//Add vertex with FG name to the new vertex vector (V)
					V.add( new Vertex(fgroups[k].id, V.size()) );
					
					//For all mapped Verticies
					for(int i=0; i<map.length; i++)				
					{											
						D.V[map[i]].label = -1;					//  label it -1 (because it wont appear in new Graph)
						D.V[map[i]].id = V.lastElement().id;	//	and replace its ID with ID of functional group
					}
				}
			}
	
			// If no mappings, new Graph = old Graph
			if(V.size() == 0) {newData[h] = D; continue;}

			//For each Vertex in old graph
			for(int i=0; i<D.V.length; i++)
			{
				//If Vertex has been mapped
				if( D.V[i].label == -1 )
				{
					//For each edge of that vertex
					for(int j=0; j<D.V[i].edges.size(); j++)
					{
						//If neighboring vertex is mapped, change ID of edge to -2
						//	because it wont appear in new Graph
						if( D.V[i].id == D.V[D.E[D.V[i].edges.elementAt(j)].getNeighbor(i)].id ) 
							D.E[D.V[i].edges.elementAt(j)].id = -2;
					}
				}
				//If Vertex has NOT been mapped
				else
				{
					String newLabel;
					if(relabelAtoms)
					{
						newLabel=(String)vertexDictionary.labels.get(LD.V[i].label);
					}
					else
					{
						newLabel=(String)vertexDictionary.labels.get(D.V[i].label);
					}
					V.add( new Vertex(newLabel, V.size()) );
					D.V[i].id = V.lastElement().id;		
					/*		 
					//Add vertex to vector of new Verticies (V)
					V.add( new Vertex((String)vertexDictionary.labels.get(D.V[i].label), V.size()) );
					D.V[i].id = V.lastElement().id;				
					*/
				}
			}
			
			//	copy Vertex vector into new graph verticies
			Vertex[] newV = new Vertex[ V.size() ];	
			V.copyInto(newV);	
			
			//For each edge in old graph
			for(int i=0;i<D.E.length;i++)
				if(D.E[i].id!=-2) //If Edge has unmapped nodes
				{	
					//Put edge in new Edge vector (E) and update new verticies in new graph
					Edge e = D.E[i];
					int vlabel1= newV[D.V[e.node1].id].label;
					int vlabel2= newV[D.V[e.node2].id].label;
					int node1 =  newV[D.V[e.node1].id].id;
					int node2 =  newV[D.V[e.node2].id].id;
					if(vlabel1>vlabel2)
					{
						int temp=vlabel1;
						vlabel1=vlabel2;
						vlabel2=temp;
					}
					newV[ D.V[e.node1].id ].edges.add(E.size());
					newV[ D.V[e.node2].id ].edges.add(E.size());		
					E.add(new Edge(node1, node2, e.bond, vlabel1+"-"+vlabel2+"-"+e.bond, E.size() ));
				}
			
			// Copy new Edge vector into new graph Edges
			Edge[] newE = new Edge[ E.size() ];
			E.copyInto(newE);
			
			//	Prepare new Graph
			newData[h] = new Graph(newV, newE, D.id,D.index);	
		}
		return newData;
	}
	
	public static int[] freq()
	{
		//		 newData is array of new graphs with replaced Functional Groups
		int[] FGcount = new int[fgroups.length];
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
			
		// For each FG
		for(int h=0; h < fgroups.length; h++)
		{
			Graph Q = fgroups[h];
			System.out.print(h + ") " + Q.id + "  ");
			int Qsize = Q.V.length;
			int[][] adjQ = adjMatrix(fgroups[h]);
			
			// For each graph in database
			for(int k=0; k < database.length; k++)
			{
				Graph D =  database[k];						// Make copy of Database Graph
				int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
				if (Qsize > Dsize)	continue; 				// Generate adj matrix of Database Graph
				int[][] adjD = adjMatrix(D);				
	
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(mappable(Q, Q.V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
			
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(adjQ, adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
					
					////////////////////////////////////////////
					FGcount[h]++;///////////////////////////////
					////////////////////////////////////////////
					
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
				}
			}
			/////////////////////////////////////////////////////////////////
			System.out.println(FGcount[h]);
			/////////////////////////////////////////////////////////////////
		}		
		return FGcount;
	}
	
	public static Graph[] OLDreplaceTEST()
	{
		// newData is array of new graphs with replaced Functional Groups
		Graph[] newData = new Graph[database.length];
		int[] FGcount = new int[fgroups.length];
		int numAtoms = 0;
		int oldNum = 0;
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
				
		// Generate all of the Adjacency Matrices for Functional Groups
		int[][][] FGadjMatrices = new int[fgroups.length][][];
		for(int i=0; i<fgroups.length; i++)
			FGadjMatrices[i] = OLDadjMatrix(fgroups[i]);
		
		// For each graph is database
		for(int h=0; h < database.length; h++)
		{
			
			Graph D =  copyGraph(database[h]);			// Make copy of Database Graph
			
			int[][] adjD = OLDadjMatrix(D);				// Generate adj matrix of Database Graph
			Vector<Vertex> V = new Vector<Vertex>();	// V is vector of new Verticies of new graph
			Vector<Edge> E = new Vector<Edge>();		// E is vector of new Edges of the new graph
			int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
			oldNum = oldNum + Dsize;					// keep track of original number of atoms
			
			// For each Functional Group
			for(int k=0; k < fgroups.length; k++)
			{
				// if Functional Group if bigger than Database graph, goto the next Functional Group
				int Qsize = fgroups[k].V.length;
				if (Qsize > Dsize)	continue; 
	
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(OLDmappable(fgroups[k], fgroups[k].V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
			
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(FGadjMatrices[k], adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
					
					////////////////////////////////////////////
					FGcount[k]++;///////////////////////////////
					////////////////////////////////////////////
					
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
					
					//Add vertex with FG name to the new vertex vector (V)
					V.add( new Vertex(fgroups[k].id, V.size()) );
					
					//For all mapped Verticies
					for(int i=0; i<map.length; i++)				
					{											
						D.V[map[i]].label = -1;					//  label it -1 (because it wont appear in new Graph)
						D.V[map[i]].id = V.lastElement().id;	//	and replace its ID with ID of functional group
					}
				}
			}
	
			// If no mappings, new Graph = old Graph
			if(V.size() == 0) {newData[h] = D; continue;}

			//For each Vertex in old graph
			for(int i=0; i<D.V.length; i++)
			{
				//If Vertex has been mapped
				if( D.V[i].label == -1 )
				{
					//For each edge of that vertex
					for(int j=0; j<D.V[i].edges.size(); j++)
					{
						//If neighboring vertex is mapped, change ID of edge to -2
						//	because it wont appear in new Graph
						if( D.V[i].id == D.V[D.E[D.V[i].edges.elementAt(j)].getNeighbor(i)].id ) 
							D.E[D.V[i].edges.elementAt(j)].id = -2;
					}
				}
				//If Vertex has NOT been mapped
				else
				{ 	//Add vertex to vector of new Verticies (V)
					V.add( new Vertex((String)vertexDictionary.labels.get(D.V[i].label), V.size()) );
					D.V[i].id = V.lastElement().id;				
				}
			}

			//	copy Vertex vector into new graph verticies
			Vertex[] newV = new Vertex[ V.size() ];	
			V.copyInto(newV);	
			
			//For each edge in old graph
			for(int i=0;i<D.E.length;i++)
				if(D.E[i].id!=-2) //If Edge has unmapped nodes
				{	
					//Put edge in new Edge vector (E) and update new verticies in new graph
					Edge e = D.E[i];
					int vlabel1= newV[D.V[e.node1].id].label;
					int vlabel2= newV[D.V[e.node2].id].label;
					int node1 =  newV[D.V[e.node1].id].id;
					int node2 =  newV[D.V[e.node2].id].id;
					if(vlabel1>vlabel2)
					{
						int temp=vlabel1;
						vlabel1=vlabel2;
						vlabel2=temp;
					}
					newV[ D.V[e.node1].id ].edges.add(E.size());
					newV[ D.V[e.node2].id ].edges.add(E.size());		
					E.add(new Edge(node1, node2, e.bond, vlabel1+"-"+vlabel2+"-"+e.bond, E.size() ));
				}
			
						

			// Copy new Edge vector into new graph Edges
			Edge[] newE = new Edge[ E.size() ];
			E.copyInto(newE);
			
			//	Prepare new Graph
			newData[h] = new Graph(newV, newE, D.id,D.index);	
			////////////////////////////////////////////////
			numAtoms = numAtoms + newData[h].V.length;//////
			////////////////////////////////////////////////
		}
		///////////////////////////////////////////////////////////////////
		System.out.println("Number of Atoms in Old Database: " + oldNum + "  New Database: " + numAtoms + 
				"   new/old: " + numAtoms/(double)oldNum); 
		for(int i=0; i<fgroups.length; i++)
			System.out.println(i + ") " + fgroups[i].id + "   " + FGcount[i] + "   " + FGcount[i]/(double)numAtoms);
		////////////////////////////////////////////////////////////////////
		return newData;
	}
	
	public static Graph[] replaceTEST()
	{
		// newData is array of new graphs with replaced Functional Groups
		Graph[] newData = new Graph[database.length];
		int[] FGcount = new int[fgroups.length];
		int numAtoms = 0;
		int oldNum = 0;
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
				
		// Generate all of the Adjacency Matrices for Functional Groups
		int[][][] FGadjMatrices = new int[fgroups.length][][];
		for(int i=0; i<fgroups.length; i++)
			FGadjMatrices[i] = adjMatrix(fgroups[i]);
		
		// For each graph is database
		for(int h=0; h < database.length; h++)
		{
			Graph D =  copyGraph(database[h]);			// Make copy of Database Graph
			
			int[][] adjD = adjMatrix(D);				// Generate adj matrix of Database Graph
			Vector<Vertex> V = new Vector<Vertex>();	// V is vector of new Verticies of new graph
			Vector<Edge> E = new Vector<Edge>();		// E is vector of new Edges of the new graph
			int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
			oldNum = oldNum + Dsize;					// keep track of original number of atoms
			
			// For each Functional Group
			for(int k=0; k < fgroups.length; k++)
			{
				// if Functional Group if bigger than Database graph, goto the next Functional Group
				int Qsize = fgroups[k].V.length;
				if (Qsize-fgroups[k].H.size() > Dsize)	continue; 
	
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(mappable(fgroups[k], fgroups[k].V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
			
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(FGadjMatrices[k], adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
					
					////////////////////////////////////////////
					FGcount[k]++;///////////////////////////////
					////////////////////////////////////////////
					
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
					
					//Add vertex with FG name to the new vertex vector (V)
					V.add( new Vertex(fgroups[k].id, V.size()) );
					
					//For all mapped Verticies
					for(int i=0; i<map.length; i++)				
					{											
						D.V[map[i]].label = -1;					//  label it -1 (because it wont appear in new Graph)
						D.V[map[i]].id = V.lastElement().id;	//	and replace its ID with ID of functional group
					}
				}
			}
	
			// If no mappings, new Graph = old Graph
			if(V.size() == 0) {newData[h] = D; continue;}

			//For each Vertex in old graph
			for(int i=0; i<D.V.length; i++)
			{
				//If Vertex has been mapped
				if( D.V[i].label == -1 )
				{
					//For each edge of that vertex
					for(int j=0; j<D.V[i].edges.size(); j++)
					{
						//If neighboring vertex is mapped, change ID of edge to -2
						//	because it wont appear in new Graph
						if( D.V[i].id == D.V[D.E[D.V[i].edges.elementAt(j)].getNeighbor(i)].id ) 
							D.E[D.V[i].edges.elementAt(j)].id = -2;
					}
				}
				//If Vertex has NOT been mapped
				else
				{ 	//Add vertex to vector of new Verticies (V)
					V.add( new Vertex((String)vertexDictionary.labels.get(D.V[i].label), V.size()) );
					D.V[i].id = V.lastElement().id;				
				}
			}

			//	copy Vertex vector into new graph verticies
			Vertex[] newV = new Vertex[ V.size() ];	
			V.copyInto(newV);	
			
			//For each edge in old graph
			for(int i=0;i<D.E.length;i++)
				if(D.E[i].id!=-2) //If Edge has unmapped nodes
				{	
					//Put edge in new Edge vector (E) and update new verticies in new graph
					Edge e = D.E[i];
					int vlabel1= newV[D.V[e.node1].id].label;
					int vlabel2= newV[D.V[e.node2].id].label;
					int node1 =  newV[D.V[e.node1].id].id;
					int node2 =  newV[D.V[e.node2].id].id;
					if(vlabel1>vlabel2)
					{
						int temp=vlabel1;
						vlabel1=vlabel2;
						vlabel2=temp;
					}
					newV[ D.V[e.node1].id ].edges.add(E.size());
					newV[ D.V[e.node2].id ].edges.add(E.size());		
					E.add(new Edge(node1, node2, e.bond, vlabel1+"-"+vlabel2+"-"+e.bond, E.size() ));
				}
			
			// Copy new Edge vector into new graph Edges
			Edge[] newE = new Edge[ E.size() ];
			E.copyInto(newE);
			
			//	Prepare new Graph
			newData[h] = new Graph(newV, newE, D.id,D.index);
			
			////////////////////////////////////////////////
			numAtoms = numAtoms + newData[h].V.length;//////
			////////////////////////////////////////////////
		}
		///////////////////////////////////////////////////////////////////
		System.out.println("Number of Atoms in Old Database: " + oldNum + "  New Database: " + numAtoms + 
				"   new/old: " + numAtoms/(double)oldNum); 
		for(int i=0; i<fgroups.length; i++)
			System.out.println(i + ") " + fgroups[i].id + "   " + FGcount[i] + "   " + FGcount[i]/(double)numAtoms);
		////////////////////////////////////////////////////////////////////
		return newData;
	}
	
	public static Graph[] relabelFG()
	{
		// newData is array of new graphs with replaced Functional Groups
		Graph[] newData = new Graph[database.length];
		
		// Reset vertexDictionary for new verticies
		//	use HashMap "RebuildGraph.labels" to keep track of old labels
		vertexDictionary.reset();
				
		// Generate all of the Adjacency Matrices for Functional Groups
		int[][][] FGadjMatrices = new int[fgroups.length][][];
		for(int i=0; i<fgroups.length; i++)
			FGadjMatrices[i] = adjMatrix(fgroups[i]);
		
		// For each graph is database
		for(int h=0; h < database.length; h++)
		{
			
			Graph D =  copyGraph(database[h]);			// Make copy of Database Graph
			int[][] adjD = adjMatrix(D);				// Generate adj matrix of Database Graph
			int Dsize = D.V.length;						// Dsize = number of Verticies in database graph
						
			// For each Functional Group
			for(int k=0; k < fgroups.length; k++)
			{
				// if Functional Group if bigger than Database graph, goto the next Functional Group
				int Qsize = fgroups[k].V.length;
				if (Qsize > Dsize)	continue; 
	
				//  Get possible mapping matrix for Ullman3 Algorithm
				int[][] mapMatrix = new int[Qsize][Dsize];
				for(int i=0; i<Qsize; i++)
					for(int j=0; j<Dsize; j++)
						if(mappable(fgroups[k], fgroups[k].V[i], D, D.V[j]))
							mapMatrix[i][j]=1; 
			
				//  Get all possible mappings
				int[][] copy = new int[mapMatrix.length][];
				pasteMatrix(mapMatrix, copy);
	
				while(true)
				{	
					//get new mapping from FG to D
					int map[] = Ullmann3.subgraphIsomorphism(FGadjMatrices[k], adjD, mapMatrix);
					if(map == null) break;		// If no mapping, goto next FG
					
					//Remove the new mapping from the copy of mapMatrix
					int count = map.length;
					for(int i=0; i<count; i++)
						for(int j=0; j<count; j++)
							copy[j][map[i]]=0;	
					
					//Get ready for next map: Reset mapMatrix with previous mapping removed
					pasteMatrix(copy, mapMatrix);
					
					//For all mapped Verticies
					for(int i=0; i<map.length; i++)				
					{											
						//Label it same as FG name
						D.V[map[i]].changeLabel(fgroups[k].id);		
					}
				}
			}
			newData[h] = D;
		}
		return newData;
	}
	
	public static void main(String[] args) throws IOException, MoleculeIOException {
		
		RebuildGraph r=new RebuildGraph("Htest.txt", "Hgrouptest.txt",false);
		Graph[] copy = new Graph[database.length];

		copy = replaceFG();

			
	}
	
}
