package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import ctree.util.Opt;
 

public class VectorQuantization {
	
	int col;
	int row;
	int numberOfCentroids  = 20;
	int numberOfIteration = 100;
	//cluster[] clusterArr = new cluster[numberOfCentroids+row];
	cluster[] clusterArr;
	int[][] points;
	String SYMBOL_FILE = "clusters.txt";//"covRgSymbols_patternmatch_500";//"C:/ImagePattern/SIFT Implementation/sift/DSymbol10000"

	public int[] cluster(){
		 
		 for(int loop = 0; loop < numberOfIteration ; loop++)
		{
			 System.out.print("Iteration Number:"+loop+" ");
			 
			 for(int vec = 0 ; vec < row ; vec++)
			 {
				 double dist = 999999999;
				 int nearestCluster = 0;
				 for(int clCount=0; clCount < numberOfCentroids ; clCount++)
				 {
					 //System.out.println("point: "+points[vec]);
					 //System.out.println("Cluster: "+clusterArr[clCount].centerV);
					 double tempDist = TanimotoMatrix.tanimotoDistance(points[vec], clusterArr[clCount].centerV);
					 if(tempDist < dist) {
						 dist = tempDist;
						 nearestCluster = clCount;
					 }
				 }
				 imagePoint ip = new imagePoint();
				 ip.image =1;
				 ip.pointIdx = vec;
				 clusterArr[nearestCluster].add(ip);
			 }
			
			 
			 if(loop < numberOfIteration-1){
				 for(int clCount=0; clCount < numberOfCentroids ; clCount++)
				 { 
					 clusterArr[clCount].recomputeCenter(points);
				 }
			 }
		}
		 System.out.println();
		int[] vecSymbol = new int[row]; 
		try {
			
			for(int clCount=0; clCount < numberOfCentroids ; clCount++)
			 { 
				 if(clusterArr[clCount].members.size()==0)
					 continue;
				 System.out.println("ClutserId: "+clCount+" AvgDist: "+clusterArr[clCount].avgDistance(points,100)+" , "+
						 " LongestDist: "+clusterArr[clCount].largestDistance(points)+" , "+
						 "Size: "+clusterArr[clCount].members.size());
				 Iterator itr = clusterArr[clCount].members.iterator();
				 while(itr.hasNext())
				 {
					 imagePoint ip = (imagePoint)itr.next();
					 vecSymbol[ip.pointIdx] = clCount;
				 }
			}
		
			BufferedWriter bw = new BufferedWriter(new FileWriter(SYMBOL_FILE));
			for(int i = 0 ; i < row ; i++)
			{
				bw.write(vecSymbol[i]+"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vecSymbol;
	}
	
	
	
	class imagePoint{
		int image;
		int pointIdx;
	}
	
	class cluster {
		
		 int size;
		 double[] centerV ;
	     LinkedList members;
	     
		 public cluster(int col){
	    	 centerV = new double[col];
	    	 size = col;
	    	 members = new LinkedList();
	     }
		 
		 public void add(imagePoint ip)
		 {
			members.add(ip); 
		 }
		 
		 public double withinGroupVariance()
		 {
			 Iterator it=members.iterator();
			 double dist=0;
			 while(it.hasNext())
			 {
				 imagePoint ip=(imagePoint) it.next();
				 dist+=Math.pow(TanimotoMatrix.tanimotoDistance(points[ip.pointIdx], centerV),2);
			 }
			 return dist/members.size();
			 
		 }
		 public void recomputeCenter(int[][] points){
			 Iterator itr = members.iterator();
			 Arrays.fill(centerV,0);

			 
			 while(itr.hasNext())
			 {
				 imagePoint ip = (imagePoint)itr.next();
				 int idx = ip.pointIdx;
				 for(int col=0; col<size; col++)
				 {
					 centerV[col] += points[idx][col];
				 }
			 }
			 
			 int memberSize = members.size();
			 
			 for(int i = 0; i<size; i++)
			 {
				 centerV[i] =(centerV[i]/memberSize);
			 }
			 members = new LinkedList();
		 }
		 
		 public double largestDistance(int[][] points){
			 Iterator itr = members.iterator();
			 double dist = 0;
			 while(itr.hasNext())
			 {
				 imagePoint ip = (imagePoint)itr.next();
				 int idx = ip.pointIdx;

				 double tempDist=TanimotoMatrix.tanimotoDistance(points[idx], centerV);
				 if(tempDist > dist) {
					 dist = tempDist;
				} 
			 }
			 return dist;
		 }
		 public double avgDistance(int[][] points,int sampleSize){
			 Iterator itr1 = members.iterator();	 
			 int[][] memberPoints=new int[members.size()][points[0].length];
			 int pos=0;
			 while(itr1.hasNext())
			 {
				 imagePoint ip1 = (imagePoint)itr1.next();
				 int idx1 = ip1.pointIdx;
				 memberPoints[pos++]=points[idx1];
			 }
			 return computeAvg(memberPoints,sampleSize);
		 }
	}
	
	public void init()
	{
		
		row=points.length;
		col=points[0].length;
		clusterArr= new cluster[numberOfCentroids+col];
    	cluster c;
    	int vectorNumber = 0;
    	double threshold=computeAvg(points,300);
    	System.out.println("Number of Centroids: "+numberOfCentroids);
    	System.out.println("Number of Iterations: "+numberOfIteration);
    	System.out.println("Theshold: "+threshold);
    	System.out.println("Number of points: "+row);
    	int[][] minmax = new int[2][col];
    	Arrays.fill(minmax[0], 99999999);
    	Arrays.fill(minmax[1], -99999999);

    	for(int i= 0; i< numberOfCentroids && vectorNumber < row; )
		 {
    		boolean acceptable = true;
    		double distance = 0;
    		for(int j=0 ; j< i; j++){
    			distance = TanimotoMatrix.tanimotoDistance(points[vectorNumber],clusterArr[j].centerV);
    			//System.out.println("Distance:"+distance);
    			if(distance < threshold){
    				acceptable = false;
    				break;
    			}
    		}
    		if(acceptable){
    		 c = new cluster(col);
			 for(int j = 0 ; j< col ; j++)
					 c.centerV[j] = points[vectorNumber][j];
			 clusterArr[i] = c;
			 i++;
			 vectorNumber++;
			}else{
    			vectorNumber++;
    		}
    	}

    	
    	for(int i=0; i< col; i++){
    		c = new cluster(col);
    		for(int j = 0 ; j< col ; j++)
				 c.centerV[j] = minmax[0][j];
    		c.centerV[i] = minmax[1][i];
		    clusterArr[numberOfCentroids+i] = c;
    	}
    	cluster();
    	
	}
	
	private void readData() {
		// TODO Auto-generated method stub
		String[] contents=FileUtils.getFileContents("bin.txt").split("\n");
		points=new int[contents.length-1][new Integer(contents[0])];
		for(int j=1;j<contents.length;j++)
		{
			String line=contents[j].trim();
			if (line.equals(""))
				continue;
			String[] col=line.split(" ");
			for(int i=1;i<col.length;i++)
			{
				points[j-1][new Integer(col[i].split(":")[0])]=new Integer(col[i].split(":")[1]);
			}
			
		}
	}

	private double[] computeClusterQuality() {
		// TODO Auto-generated method stub
		double bc=0;
		double wc=0;
		int nonZero=0;
		double[] mean=new double[col];
		for(int i=0;i<numberOfCentroids;i++)
		{
			cluster c=clusterArr[i];
			if(c.members.size()==0)
				continue;
			nonZero++;
			wc+=c.withinGroupVariance();
			for(int j=0;j<c.centerV.length;j++)
				mean[j]+=c.centerV[j];
		}
		wc=wc/nonZero;
		
		for(int i=0;i<mean.length;i++)
		{
			mean[i]=mean[i]/nonZero;
		}
		//System.out.println(Arrays.toString(mean));
		for(int i=0;i<numberOfCentroids;i++)
		{
			cluster c=clusterArr[i];
			if(c.members.size()==0)
				continue;
			//System.out.println("---------------------------------------------");
			//System.out.println(Arrays.toString(c.centerV));
			bc+=Math.pow(TanimotoMatrix.tanimotoDistance(c.centerV, mean), 2);
			//System.out.println(bc+"\n---------------------------------------------");

		}
		bc=bc/nonZero;
		double[] data=new double[3];
		data[0]=nonZero;
		data[1]=wc;
		data[2]=bc;
		return data;
	
	}

	private double computeAvg(int[][] points,int sampleSize) {
		// TODO Auto-generated method stub
		int[][] samples=new int[sampleSize][col];
		double avg=0;
		int count=0;
		for(int i=0;i<sampleSize && i<points.length;i++)
		{
			int pos=(int)(Math.random()*points.length);
			samples[i]=points[pos];	
		}
		for(int i=0;i<samples.length;i++)
		{
			for (int j=i+1;j<samples.length;j++)
			{
				avg+=TanimotoMatrix.tanimotoDistance(samples[i], samples[j]);
				count++;
			}
		}
		
		//System.out.println("Avg. distance: "+avg/count);
		return avg/count;
	}

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Vector Quantization");
		VectorQuantization vq = new VectorQuantization();
		//vq.vectorQuantize();
		Opt opt=new Opt(args);
		vq.readData();
		vq.numberOfIteration=opt.getInt("iteration",100);
		if(opt.hasOpt("determineK"))
		{
			vq.readData();
			int range1=opt.getInt("range1",2);
			int range2=opt.getInt("range2",30);
			StringBuffer result=new StringBuffer("");
			for(int i=range1;i<=range2;i++)
			{
				vq.numberOfCentroids=i;
				vq.init();
				double[] quality=vq.computeClusterQuality();
				result.append(quality[0]+","+quality[1]+","+quality[2]/(quality[2]+quality[1])+"\n");
			}
			FileUtils.writeToFile("clusterAnalysis.txt", result.toString());
		}
		else
		{
			vq.numberOfCentroids=opt.getInt("centroids",10);
			vq.init();
			vq.reportQuality();
		}
	}

	private void reportQuality() {
		// TODO Auto-generated method stub
		double[] quality=computeClusterQuality();
		System.out.println("Non Zero Clusters : "+quality[0]);
		System.out.println("WC/RSS : "+quality[1]);
		System.out.println("BC : "+quality[2]);
		System.out.println("PVE : "+quality[2]/(quality[2]+quality[1]));
	}
	
}
