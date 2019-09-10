package tool;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import Dictionary.edgeDictionary;
import Dictionary.pathsDictionary;
import Dictionary.vertexDictionary;

import graph.*;

public class PathExploration {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	static HashMap paths;
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String fileName="aido99_all.txt";
		Graph[] graphdb=BuildGraph.loadGraphs(fileName);
		//Graph g=graphdb[0];
		explorePaths(graphdb,3);
	}

	public static int[][] explorePaths(Graph[] graphdb, int depth)
	{
		Vector<HashMap> maps=new Vector<HashMap>();
		long time=System.currentTimeMillis();
		for(int i=0;i<graphdb.length;i++)
		{
			
			Graph g=graphdb[i];
			//System.out.println(i);
			//maps.add()=new HashMap[g.V.length];
			for(int j=0;j<g.V.length;j++)
			{
				paths=new HashMap();
				Vertex v=g.V[j];
				explore(g,j,depth,j,v.label+"");
				maps.add((HashMap)paths.clone());
				//System.out.println(j+" : "+paths.toString());
			}

		}
		//System.out.println(pathsDictionary.types.toString());
		System.out.println(pathsDictionary.types.size());
		int[][] profile=convertToInt(maps);
		System.out.print(System.currentTimeMillis()-time);
		return profile;		
	}
	private static int[][] convertToInt(Vector<HashMap> maps) {
		// TODO Auto-generated method stub
		int[][] profile=new int[maps.size()][pathsDictionary.types.size()];
		//Arrays.fill(profile,0);
		for(int i=0;i<maps.size();i++)
		{
			
			Integer[] keyset=(Integer[])maps.elementAt(i).keySet().toArray(new Integer[0]);
			
			for(int j=0;j<keyset.length;j++)
			{
				Integer val=(Integer)((HashMap)maps.elementAt(i)).get(keyset[j]);
				if(val!=null)
				{
					if(i==10)
						System.out.println(keyset[j]);
					profile[i][keyset[j]]=val;
				}
			}
		}
		System.out.println(Arrays.toString(profile[0]));
		System.out.println(Arrays.toString(profile[10]));
		return profile;
	}

	private static void explore(Graph g,int id, int d,int cid, String path) {
		// TODO Auto-generated method stub
		//Vector[] paths=new Vector[v.edges.size()];
		Vertex v=g.V[id];
		
		if(id!=cid && (d==0 || v.edges.size()==1))
		{
			//System.out.println(path);
			int index=pathsDictionary.getID(path);
			Integer val=(Integer)paths.get(index);
			//System.out.println(val);
			if(val!=null)
			{
				
				paths.put(index, val+1);
			}
			else
				paths.put(index, 1);
			//System.out.println();
			return;
		}
		for(int i=0;i<v.edges.size();i++)
		{
			Edge e=g.E[v.edges.elementAt(i)];
			//System.out.print(edgeDictionary.types.get(e.type)+" ");
			int nid=e.getNeighbor(id);
			if(nid!=cid)
			{
				explore(g,nid,d-1,id,path+"-"+e.bond+"-"+g.V[nid].label);
			}
		}
		return;
	}

}
