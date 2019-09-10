package tool;
 
import graph.Edge;
import graph.Graph;
import graph.Vertex;
 
import java.io.FileNotFoundException;
import java.io.PrintWriter;
 
import Dictionary.vertexDictionary;
 
public class CloseGraphFormatConverter {
        public static void main(String[] args) throws FileNotFoundException
        {
        		String fileName="aido99_all";
                Graph[] graphdb=BuildGraph.loadGraphs(fileName+".txt");
                StringBuffer s=new StringBuffer("");
                
                //s.append("graph G {\nnode[shape=plaintext width=.05 height=.1 fontsize=12]\nedge[len=1 ]\ngraph[size=5,5]\n");
                
                PrintWriter out=new PrintWriter(fileName+".cfg");
                
                for(int i=0;i<graphdb.length;i++)
                {

                        s.append(graphdb[i].toString("CG")+"\n");
                        /*
                        for(int j=0;j<e.length;j++)
                        {
 
                                int node1=e[j].node1;
                                int node2=e[j].node2;
                                //String nodeLabel1=(String)vertexDictionary.labels.get(v[node1].label)+"_"+node1;
                                //String nodeLabel2=(String)vertexDictionary.labels.get(v[node2].label)+"_"+node2;
                                s.append("e "+node1+" "+node2+" "+"0"+"\n");
                                
                        }
 						*/
 
 
 
                }
                out.println(s.toString().trim());
                out.close();
 
 
        }
}