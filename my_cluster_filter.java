import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Instance;
import weka.core.Instances;


public class my_cluster_filter {
	public static ArrayList<cluster> clist = new ArrayList<cluster>();
	public static Instances data;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {	
		big_old_cluster_filter();
	}
	
	/** overall function
	*/
	public static void big_old_cluster_filter() throws FileNotFoundException, IOException{
		String f1 = "acl1_seed_1000.filter.arff";
		data = new Instances(new FileReader(new File("data_arffs/"+f1)));
		System.out.println("data initialized. num instances: " + data.numInstances());
		
		cluster c;
		
		//fill clusterlist
		for(int i=0; i < data.numInstances(); i++){
			c = new cluster(data);
			c.add(i);
			clist.add(c);//problem...not being properly added...
			
			//c.print();
			//System.out.println("num clusters: "+ clist.size());
			//print_all_clusters();
		}
		//print_all_clusters();
		glob_until(500);
	}
	
	/** here, k is the final number of clusters that there are.
	 * assumption: clist is already initialized 
	 * @throws IOException 
	 * @throws FileNotFoundException */
	private static void glob_until(int k){
		//then combine clusters, one by one, until you are left with only k clusters.
		while(clist.size() >= k){
			System.out.println("clist size: " + clist.size() + " total Error: " + total_error());
			glob();
			//print_all_clusters();
			
		}
		print_all_clusters();
	}
	
	/**one iteration of the agglomerative or k-means algorithm
	 * this combines the closest two clusters in the list*/
	private static void glob(){ 
		int [] pair = closest_pair();
		cluster c = clist.get(pair[0]);
		cluster d = clist.get(pair[1]);
		//System.out.println("pair: "+Arrays.toString(pair));
		c.absorb(d);
		//c.print();
		clist.remove(pair[1]);
		//System.out.println("clist size: " + clist.size());
	} 
	
	//////////////////////////////LOWER LEVEL HELPER FUNCTIONS ////////////////////
	
	/**returns the indices in the clist of the closest two clusters*/
	private static int [] closest_pair(){
		int [] pair = new int[2];
		double min_dist=Long.MAX_VALUE, dist=0;
		for(int i = 0; i < clist.size();i++){
			cluster c = clist.get(i);
			int j = 0;
			for(;j<clist.size();j++){
				if(i==j) continue;
				dist = c.dist(clist.get(j));
				if(dist < min_dist){
					min_dist = dist;
					pair[0]=i;
					pair[1]=j;
				}
			}
		}
		return pair;
	}
	

	/**gets the hyper volume of an instance
	 * NOTE: I have made an adjustment to the formula...
	 * in order to avoid the common case of zero hyper volume from one
	 * of the dimensions being flat, each side will get +1. This is somewhat
	 * arbitrary, and artificial, but oh well.*/
	private static double getInstanceVol(int index){
		Instance inst = data.instance(index);
		double s1 = inst.value(1)-inst.value(0)+1;
		double s2 = inst.value(3)-inst.value(2)+1;
		double s3 = inst.value(5)-inst.value(4)+1;
		double s4 = inst.value(7)-inst.value(6)+1;
		return s1*s2*s3*s4;
	}
	//////////////////////////////EVALUATION and DEBUGGING ///////////////////////
	
	private static void print_all_clusters(){
		for(int i=0;i<clist.size();i++){
			System.out.print(i + " ");
			clist.get(i).print();
		}
	}
	
	public static double total_error(){
		double sumInstanceVols=0,sumClusterVols=0;
		for(int i=0; i<data.numAttributes();i++ ){
			sumInstanceVols += getInstanceVol(i);
		}
		for(cluster c : clist){
			sumClusterVols += c.getHvol();
		}
		System.out.print("cluster vols: " + sumClusterVols +  " instanceVols: " + sumInstanceVols);
		return ((sumClusterVols - sumInstanceVols)/sumClusterVols);
	}

}
