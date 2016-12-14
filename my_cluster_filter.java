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
		}
		
		
		//double orig = sum_orig_rule_vols();
		//double Hvols = sum_cluster_vols();
		//System.out.println("total Hvols: " + Hvols + " total rule vols: " + orig);
		
		//print_all_clusters();
		
		double [][] results = glob_until(clist.size() - 30);
		print_results(results);
		
		
	}
	
	
	
	/** here, k is the final number of clusters that there are.
	 * assumption: clist is already initialized 
	 * @throws IOException 
	 * @throws FileNotFoundException */
	private static double [][] glob_until(int k){
		
		//initialize a 2d double array for number of clusters vs. total error (volume), accuracy,
		//and cost, where cost = error*(num clusters)
		int num_iterations = clist.size() - k + 1;
		double [][] results = new double[6][num_iterations];
		
		//combine clusters, one by one, until you are left with only k clusters.
		//NOTICE: the condition in the for loop doesn't use the for loop's iterator
		//results[4][0]=0;
		for(int j=0 ; clist.size() > k ; j++){
			if(j%50==0) System.out.println("clist size: " + clist.size());// + " total Error: " + total_err());
			
			results[0][j] = clist.size();
			results[1][j] = total_err();
			results[2][j] = total_acc();
			results[3][j] = results[0][j]*clist.size();//cost function
			if(j>0) results[4][j] = results[1][j]-results[1][j-1];//dError/d(iteration)
			//logarithm of results...
			if(results[4][j] > 0) results[5][j]=Math.log10(results[4][j]);
			glob();
		}
		
		//print_all_clusters();
		return results;
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
		double vol = inst.value(1)-inst.value(0) + 1;
		vol *= (inst.value(3)-inst.value(2) + 1);
		vol *= (inst.value(5)-inst.value(4) + 1);
		vol *= (inst.value(7)-inst.value(6) + 1);
//		double s1 = inst.value(1)-inst.value(0) + 1;
//		double s2 = (inst.value(3)-inst.value(2) + 1);
//		double s3 = (inst.value(5)-inst.value(4) + 1);
//		double s4 = (inst.value(7)-inst.value(6) + 1);		
		//System.out.println(inst.toString());
		//System.out.println("s1,s2,s3,s4: " + s1 + "," + s2 + "," + s3 + "," + s4);
		//return s1*s2*s3*s4;
		return vol;
	}
	
	
	/**gets the sum of the hypervolumes taken up by all of 
	 * the clusters. At the start, this should equal the
	 * sum of the original individual rule volumes*/
	private static double sum_cluster_vols(){
		double sumClusterVols=0;
		for(cluster c : clist){
			sumClusterVols += c.getHvol();
			//System.out.println("cluster Hvol: " + Hvol);
		}
		return sumClusterVols;
	}
	
	/**gets the sum of the hypervolumes of each rule*/
	private static double sum_orig_rule_vols(){
		double sum_orig_vols=0;
		for(int i=0; i< data.numInstances();i++ ){
			sum_orig_vols += getInstanceVol(i);
		}
		return sum_orig_vols;
	}
	
	//////////////////////////////EVALUATION and DEBUGGING ///////////////////////
	
	private static void print_all_clusters(){
		for(int i=0;i<clist.size();i++){
			System.out.print(i + " ");
			clist.get(i).print();
		}
	}
	
	private static void print_results(double [][] results){
		System.out.println("errors" + Arrays.toString(results[1]));
		System.out.println("d Errors/dt : " + Arrays.toString(results[4]));
		System.out.println("log10( d Errors/dt) :" + Arrays.toString(results[5]));
		System.out.println("total rule hyper volume: " + sum_orig_rule_vols());
	}
	
	public static double total_err(){
		double orig = sum_orig_rule_vols();
		double Hvols = sum_cluster_vols();
		return Hvols - orig;
	}
	
	public static double total_acc(){
		double TE = total_err();
		double Hvols = sum_cluster_vols();
		return TE/Hvols;
	}

}
