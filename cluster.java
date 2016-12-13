import weka.core.Instance;
import weka.core.Instances;
import java.util.*;

public class cluster {
	
	public ArrayList<Integer> index_list;
	public static Instances ds;//dataset pointer
	public double [] centroid;
	public double [] bounds;
	
	public cluster(Instances data){
		index_list = new ArrayList<Integer>();
		ds = data;
		centroid = new double[4];
		bounds= new double[8];
	
	}
	
	

	/***/
	public void add(int index){
		index_list.add(index);
		set_bounds();
		set_centroid();
	}
	
	//PROBLEM somewhere here...figure out why this won't work...
	public void absorb(cluster other){
		if(other==null) return;
		for(Integer i : other.index_list){
			add(i);
		}
		//index_list.addAll(other.index_list);
	
	}
	
	/**calculate the euclidean distance between centroids*/
	private double mean_dist(cluster other){
		double dist = 0;
		for(int k=0;k<centroid.length;k++){
			dist+=Math.sqrt(Math.pow(centroid[k], 2) + Math.pow(other.centroid[k],2));
		}
		return dist;
	}
	
	/**calculates the distance between the closest two points in two clusters*/
	public double min_dist(cluster other){ 
		if(isOverlapping(other)) return 0;
		double min=Integer.MAX_VALUE;
		for(int j=0;j<4;j++){
			double this_min = bounds[(2*j)+1];
			double this_max = bounds[(2*j)+1];
			double other_min = other.bounds[(2*j)];
			double other_max = other.bounds[(2*j)+1];
			double d1 = Math.min(Math.abs(this_max - other_min), Math.abs(other_max - this_min));
			min = Math.min(d1, min);
		}
		return min;
	}

	public void set_centroid(){
		for(int j =0 ; j < 4 ; j++){
			for(int i : index_list){
				Instance inst = ds.instance(i);
				centroid[j]+=inst.value((j*2)+1)+inst.value(j*2);
			}
			centroid[j]/=(2*index_list.size());
		}
	}
	public void set_bounds(){
		for(int j = 0 ; j < bounds.length;j++){
			if(j%2==0) bounds[j]=getMin(j);
			else bounds[j]=getMax(j);
		}	
	}
	
	/**MOST IMPORTANT DEFINING FUNCTION for the clustering algorithm
	 * */
	public double dist(cluster other){
		return mean_dist(other);
	}
	
	//NOT DONE, doesn't work yet
	public boolean isOverlapping(cluster other){
		//if(getHvol()==0 || other.getHvol()==0) return false;
		for(int j=0;j<4;j++){
			double this_min = bounds[(2*j)+1];
			double this_max = bounds[(2*j)+1];
			double other_min = other.bounds[(2*j)];
			double other_max = other.bounds[(2*j)+1];
			if((this_max >= other_min) && (this_max <= other_max))  return true;
			if((other_max >= this_min) && (other_max <= this_max))  return true;
		}
		return false;
	}
	
	public double getMin(int att_index){
		double min = Integer.MAX_VALUE;
		for(int i : index_list){
			Instance inst = ds.instance(i);
			min = Math.min(min,inst.value(att_index));
		}
		return min;
	}
	public double getMax(int att_index){
		double max = Integer.MIN_VALUE;
		for(int i : index_list){
			Instance inst = ds.instance(i);
			max = Math.max(max,inst.value(att_index));
		}
		return max;
	}	
	
	/**get the hypervolume of the index list*/
	public double getHvol(){
		double s1 = getMax(1)-getMin(0);
		double s2 = getMax(3)-getMin(2);
		double s3 = getMax(5)-getMin(4);
		double s4 = getMax(7)-getMin(6);
		return s1*s2*s3*s4;
	}
	
	public void print(){
		System.out.println("centroid: "+Arrays.toString(centroid) + " bounds: " + Arrays.toString(bounds));
		System.out.println("index list: " + index_list.toString());
	}

	
}
