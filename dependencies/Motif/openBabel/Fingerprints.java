package openBabel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import joelib2.feature.FeatureException;
import joelib2.feature.result.BitResult;
import joelib2.feature.types.SSKey3DS;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;

import tool.BuildGraph;
import tool.FileUtils;
import tool.NeighborHood;
import tool.RandomWalk;
import tool.RandomWalkV1;
import Dictionary.argumentsDictionary;
import Dictionary.vertexDictionary;
import grank.transform.Hist;
import graph.Graph;

public class Fingerprints {

	public static Hist[] loadHistsFromFingerPrints(String fileName) throws FeatureException, IOException, MoleculeIOException {
		// TODO Auto-generated method stub
		Graph[] graphdb=BuildGraph.loadGraphs(fileName);
		vertexDictionary.print();
		Vector<Hist> buf = new Vector<Hist> ();
		double time=System.currentTimeMillis();
		//RandomWalk r=new RandomWalkV1(argumentsDictionary.restart);
		StringBuffer smartsPattern=new StringBuffer("");
		for(int i=0;i<graphdb.length;i++)
		{
			if(!vertexDictionary.labels.get(graphdb[i].V[0].label).equals("a"))
				smartsPattern.append(BuildGraph.graphdb[i].getSmiles().getSmileRep()+" "+BuildGraph.graphdb[i].id+"\n");
		}
		String smilesFileName=fileName.substring(0,fileName.lastIndexOf("."))+".smiles";
		FileUtils.writeToFile(smilesFileName, smartsPattern.toString());

		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SMILES");
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		BasicReader reader=new BasicReader(smilesFileName);
		int i=0;
		while(reader.readNext(mol))
		{
			int[] fingerprint = convertBitToInt((BitResult) new SSKey3DS().calculate(mol));
			//int[] fingerprint = (BitResult) new SSKey3DS().calculate(mol);
			buf.add(new Hist(graphdb[i].id+"-"+(i++), fingerprint));
			//System.out.println(graphdb[i].id+"-"+i);
			//System.out.println(Arrays.toString(fingerprint));
			//new Scanner(System.in).next();
		}
		System.out.println("Time taken: "+(System.currentTimeMillis()-time)/1000.0);
	    Hist[] array = new Hist[buf.size()];
	    //System.out.println("Total number of nodes: "+buf.size());
	    //new Scanner(System.in).next();
	    buf.toArray(array);
	    return array;
	}

	public static Hist[] loadHistsFromFingerPrintsAsBits(String fileName) throws FeatureException, IOException, MoleculeIOException {
		// TODO Auto-generated method stub
		Graph[] graphdb=BuildGraph.loadGraphs(fileName);
		vertexDictionary.print();
		Vector<Hist> buf = new Vector<Hist> ();
		double time=System.currentTimeMillis();
		//RandomWalk r=new RandomWalkV1(argumentsDictionary.restart);
		StringBuffer smartsPattern=new StringBuffer("");
		for(int i=0;i<graphdb.length;i++)
		{
			if(!vertexDictionary.labels.get(graphdb[i].V[0].label).equals("a"))
				smartsPattern.append(BuildGraph.graphdb[i].getSmiles().getSmileRep()+" "+BuildGraph.graphdb[i].id+"\n");
		}
		String smilesFileName=fileName.substring(0,fileName.lastIndexOf("."))+".smiles";
		FileUtils.writeToFile(smilesFileName, smartsPattern.toString());

		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SMILES");
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		BasicReader reader=new BasicReader(smilesFileName);
		int i=0;
		while(reader.readNext(mol))
		{
			int[] fingerprint = convertBitToIntBits((BitResult) new SSKey3DS().calculate(mol));
			//int[] fingerprint = (BitResult) new SSKey3DS().calculate(mol);
			buf.add(new Hist(graphdb[i].id+"-"+(i++), fingerprint));
			System.out.println(graphdb[i-1].id+"-"+(i-1));
			//System.out.println(Arrays.toString(fingerprint));
			//new Scanner(System.in).next();
		}
		System.out.println("Time taken: "+(System.currentTimeMillis()-time)/1000.0);
	    Hist[] array = new Hist[buf.size()];
	    //System.out.println("Total number of nodes: "+buf.size());
	    //new Scanner(System.in).next();
	    buf.toArray(array);
	    return array;
	}

	public static int[] convertBitToInt(BitResult result) {
		// TODO Auto-generated method stub
		//System.out.println(result.toString());
		int[] intResult=new int[result.maxBitSize];
		/*
		 int[] intResult=new int[result.maxBitSize-19];
		for(int i=0;i<5;i++)
		{
			if(result.value.get(i))
				intResult[0]=i+1;
		}	
		
		if(result.value.get(5)==true)
		{
			intResult[1]=3;
		}
		if(result.value.get(6)==true)
		{
			intResult[1]=9;
		}
		if(result.value.get(7)==true)
		{
			intResult[1]=17;
		}
		if(result.value.get(8)==true)
		{
			intResult[1]=21;
		}
		if(result.value.get(9)==true)
		{
			intResult[1]=27;
		}
		if(result.value.get(10)==true)
		{
			intResult[1]=33;
		}
		if(result.value.get(11)==true)
		{
			intResult[1]=39;
		}
		for(int i=12;i<44;i++)
		{
			intResult[i-10]=(result.value.get(i)?1:0)*1;
		}
		
		
		for(int i=44;i<result.maxBitSize;i++)
		{
			if(result.value.get(i))
				intResult[34]=i-43;
		}		
		*/
		for(int i=0;i<result.maxBitSize;i++)
		{
				intResult[i]=result.value.get(i)?1:0;
		}	
		return intResult;
	}

	private static int[] convertBitToIntBits(BitResult result) {
		// TODO Auto-generated method stub
		//System.out.println(result.toString());
		int[] intResult=new int[result.maxBitSize];
		
		for(int i=0;i<result.maxBitSize;i++)
		{
			intResult[i]=result.value.get(i)?1:0;
				
		}		
		return intResult;
	}

	public static void saveContainers(Hist[] results, Hist[] db) throws FileNotFoundException {
		// TODO Auto-generated method stub
		PrintWriter out=null;
		//Vector[] motifs=new Vector[results.length];
		String path=argumentsDictionary.subHistDir;
		FileUtils.deleteFiles(path);
		StringBuffer cgScript=new StringBuffer("");
		for(int i=0;i<results.length;i++)
		{
			Vector<Graph> motif=new Vector<Graph>();
			StringBuffer s=new StringBuffer("");
			if(results[i]==null)
				continue;
			int gid=0;
			int lastgid=-1;
			Vector<Integer> nodes=new Vector<Integer>();
			
			for(int j=0;j<db.length;j++)
			{
				if(db[j].contains(results[i]))
				{
					s.append(db[j].id+"\n");
					//System.out.println(db[j].id);
					String[] parts=db[j].id.split("-");
					gid=new Integer(parts[1]);
					
					//System.out.println(gid+"-"+node);

					motif.add(BuildGraph.graphdb[j]);

				}
			}
		
			StringBuffer graphs=new StringBuffer("");
			int size=0;
			//if(motif.size()<=1)
				//continue;
			int numGraphs=0;
			for(int j=0;j<motif.size();j++)
			{
				Graph subgraph=motif.elementAt(j);

					size++;
					
					if(subgraph!=null)
					{
						graphs.append(subgraph.toString("CG")+"\n");
						numGraphs++;
					}
				
			}
			if(numGraphs<=5)
				continue;
			PrintWriter gout=new PrintWriter(path+results[i].id+".cg");
			out=new PrintWriter(path+results[i].id+".txt");
			//cgScript.append("../gSpan "+results[i].id+ ".cg -s"+Math.round(numGraphs*0.8)+" -o \n");
			cgScript.append("../fsg "+results[i].id+ ".cg -s80 -m5 -x\n");
			gout.print(graphs.toString());
			gout.close();
			out.println(s);
			out.close();
		}
		/*
		String[] names=FileUtils.getFilesWithExt("cg",path);
		
		for(int i=0;i<names.length;i++)
		{
			String name=names[i];
			//int freq=new Integer(name.substring(name.lastIndexOf("-")+1,name.lastIndexOf(".")));
			//int support=new Double(freq*0.8).intValue();
			cgScript.append("../fsg "+name+ " -s"+numGraphs+" -m 5 -x\n");
		}
		*/
		FileUtils.writeToFile(path+"findFreqGraphs.sh",cgScript.toString()+"mkdir fp\nmv *.fp fp\n");
		/*int size=0;
		for(int i=0;i<motifs.length;i++)
		{
			size+=motifs[i].size();
		}
		System.out.println(size);*/
	}

	public static void saveHists(Hist[] H, String hist_file) throws FileNotFoundException {
		// TODO Auto-generated method stub
	    PrintWriter out = new PrintWriter(argumentsDictionary.ids.substring(0,argumentsDictionary.ids.lastIndexOf("."))+"/"+hist_file);
	    for (int i = 0; i < H.length; i++) {
	      if(H[i]==null)
		      continue;
	      out.print(H[i].id+":");
	      //System.out.println(H[i].id);
	      for (int j = 0; j < H[i].hist.length - 1; j++) {
	        out.print(H[i].hist[j] + " ");
	      }
	      out.println(H[i].hist[H[i].hist.length - 1]);
	    }
	    out.close();
		
	}
	
	public static Hist[] readFingerPrints(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Graph[] graphdb=BuildGraph.loadGraphs(fileName);
		vertexDictionary.print();
		Vector<Hist> buf = new Vector<Hist> ();
		double time=System.currentTimeMillis();
		//RandomWalk r=new RandomWalkV1(argumentsDictionary.restart);
		StringBuffer smartsPattern=new StringBuffer("");
		for(int i=0;i<graphdb.length;i++)
		{
			if(!vertexDictionary.labels.get(graphdb[i].V[0].label).equals("a"))
				smartsPattern.append(BuildGraph.graphdb[i].getSmiles().getSmileRep()+" "+BuildGraph.graphdb[i].id+"\n");
		}
		String smilesFileName=fileName.substring(0,fileName.lastIndexOf("."))+".smiles";
		FileUtils.writeToFile(smilesFileName, smartsPattern.toString());

		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SMILES");
	    String fingerprint_fileName=argumentsDictionary.graph.substring(0,argumentsDictionary.graph.lastIndexOf("."))+".fpt";

		
		Scanner sc=new Scanner(new File(fingerprint_fileName));
		String graphid="";
		graphid=sc.nextLine().split(" ")[0].substring(1);//skip first line by default
		String val="";
		int linenum=0;
		while(sc.hasNext())
		{
			String line=sc.nextLine();
			if(line.charAt(0)=='P')
				continue;
			if(line.charAt(0)=='>')
			{
				buf.add(new Hist(graphid+"-"+(linenum++),convertToBinary(val)));
				val="";
				graphid=line.split(" ")[0].substring(1);//skip first line by default
			}
			else
				for (String word:line.split(" "))
					for (int i=0;i<word.length();i++)
					{
						String digit=new String(word.substring(i,i+1));
						if(digit.equals("a"))
							digit="10";
						if(digit.equals("b"))
							digit="11";
						if(digit.equals("c"))
							digit="12";
						if(digit.equals("d"))
							digit="13";
						if(digit.equals("e"))
							digit="14";
						if(digit.equals("f"))
							digit="15";
														
						String binValue=Integer.toBinaryString((Integer.parseInt(digit))).trim();
						int binLength=binValue.length();
						for(int j=0;j<4-binLength;j++)
							binValue="0"+binValue;
						val+=binValue;
						//System.out.println(digit+" : "+binValue);
					}
		}
		
		return buf.toArray(new Hist[0]);
	}

	private static int[] convertToBinary(String val) {
		// TODO Auto-generated method stub
		int[] bin=new int[val.length()];
		for(int i=0;i<val.length();i++)
			bin[i]=(val.charAt(i)=='1')?1:0;
			
		return bin;
	}

}
