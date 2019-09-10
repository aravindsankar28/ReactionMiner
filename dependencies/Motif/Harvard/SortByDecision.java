package Harvard;

import java.io.File;
import java.util.PriorityQueue;
import java.util.TreeSet;

import tool.FileUtils;
import ctree.util.Opt;

public class SortByDecision {

	
	public static void main(String[] args)
	{
		Opt opt=new Opt(args);
		String partition=opt.getString("partition");
		String folder=partition.substring(0,partition.lastIndexOf("/"));
		String[] parts=FileUtils.getFileContents(partition).trim().split("\n");
		TreeSet<RankedDecision> pq=new TreeSet<RankedDecision>();
		//System.out.println("Partition: "+partition);
		for (String part:parts)
		{
			//System.out.println("Part: "+part);
			String decision=folder+"/dec/"+part+".txt.part";
			//System.out.println("Decision: "+decision);
			String[] ids=FileUtils.getFileContents(folder+"/ids/"+part+".sdf.ids").trim().split("\n");
			int chunk=10000;
			int c=0;
			int partNum=0;
			String[] dec=FileUtils.getFileContents(decision+partNum+".dec").trim().split("\n");
			for (String id:ids)
			{
				float score=new Float(dec[c].trim().split(" ")[2]);
				RankedDecision t=new RankedDecision(part+"_"+(partNum*chunk+c+1)+"_"+id,score);
				pq.add(t);
				c++;
				if (c==chunk)
				{
					partNum++;
					c=0;
					dec=FileUtils.getFileContents(decision+partNum+".dec").trim().split("\n");
				}
			}
		}
		for (RankedDecision t:pq)
		{
			System.out.println(t.id+"\t\t\t\t\t\t"+t.score);
		}
	}
}
