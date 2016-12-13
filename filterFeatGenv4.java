import java.io.File;
import java.util.Scanner;
import java.util.regex.Pattern;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * most up to date version.
 * 
 * To create arff files from different inputs,
 * edit the main function at the bottom of the file.
 * */
public class filterFeatGenv4 {

	static String[] features = new String[] { "srcIPstart", "srcIPend", "dstIPstart",
		"dstIPend", "srcPortStart", "srcPortEnd", "dstPortStart",
		"dstPortEnd" };
	private static FastVector plus_minus;

	static {	
		plus_minus = new FastVector(2);
		plus_minus.addElement("+");
		plus_minus.addElement("-");
	}

	/**
	 * Overall function to read in input line by line input: the input text file
	 * output:Instances object, to be written to an arff file
	 * */
	public static Instances readData(String fileName) throws Exception {
		Instances instances = initializeAttributes();
		Scanner scanner = new Scanner(new File(fileName));

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			Instance instance = makeInstance(instances, line);
			instances.add(instance);
		}
		scanner.close();
		return instances;
	}

	/**
	 * called only once by readData() function, at beginning list out each
	 * attribute. In this case, ip addresses, and port numbers.
	 * */
	private static Instances initializeAttributes() {
		/** set up numerical attributes */
		FastVector attributes = new FastVector(features.length);

		for (String featureName : features) {
			attributes.addElement(new Attribute(featureName));
		}
		Attribute classLabel = new Attribute("Class", plus_minus);
		attributes.addElement(classLabel);

		/** initialize dataset (Instances object) */
		String nameOfDataset = "filters";
		Instances ds = new Instances(nameOfDataset, attributes, 0);
		ds.setClass(classLabel);
		return ds;
	}

	/**
	 * called iteratively by the readData() function This function is the meat
	 * and potatoes of this class.
	 * */
	private static Instance makeInstance(Instances ds, String inputLine) {
	
		double [] vals=input_to_doubles(inputLine);
		
		//Instance inst = new Instance(1,vals);
		Instance inst = new Instance(features.length+1);
		inst.setDataset(ds);

		
		for (int featureId = 0; featureId < features.length; featureId++) {
		    Attribute att = ds.attribute(features[featureId]);
		    inst.setValue(att, vals[featureId]);
		}
		/*all instances have classification of "+"
		 * later, virtual instances will be calculated (not read in)
		 * and will have a label of "-"*/
		inst.setClassValue("+");

		return inst;
	}

	//////////////////////////HELPER FUNCTIONS////////////////////////////
	
	/**helper function to makeInstance() function
	 * this does the main work of reading and interpreting 
	 * input strings into double values*/
	private static double[] input_to_doubles(String inputLine){
		inputLine = inputLine.trim();
		String[] parts = inputLine.split("\t");
		
		/* for debugging. print all parts, to ensure that input was properly split */
		//for (int k = 0; k < parts.length; k++) {System.out.print(" parts[" + k + "]=" + parts[k]);}System.out.println("|");
		
		double[] vals = new double[features.length];// 8 features
		/**/
		double srcip = ip_to_dec(parts[0].trim());
		int smask = Integer.parseInt(parts[1]);
		double dstip = ip_to_dec(parts[2].trim());
		int dmask = Integer.parseInt(parts[3]);
		
		vals[0] = ip_range_min((long)srcip,smask);
		vals[1] = ip_range_max((long)srcip,smask);
		vals[2] = ip_range_min((long)dstip,dmask);
		vals[3] = ip_range_max((long)dstip,dmask);
		
		String[] src_port_range = parts[4].split(":");
		String[] dst_port_range = parts[5].split(":");

		vals[4] = Long.parseLong(src_port_range[0].trim());
		vals[5] = Long.parseLong(src_port_range[1].trim());
		vals[6] = Long.parseLong(dst_port_range[0].trim());
		vals[7] = Long.parseLong(dst_port_range[1].trim());
		
		//System.out.println("vals: " + Arrays.toString(vals));
		
		return vals;
	}
	/**assumption: ip_dec is a proper ipv4 address properly converted to decimal
	 * assumption: mask is an int between 0 and 32
	 * */
	private static long ip_range_min(long ip_dec, int mask){
		if(mask==32) return ip_dec;//from an off-by-one error
		if(mask==0) return 0;//off-by-one-error
		long min=ip_dec;
		int rem = 32-mask;
		return (ip_dec&(((1<<mask) -1)<<rem));
	}
	
	/**assumption: ip_dec is a proper ipv4 address properly converted to decimal
	 * assumption: mask is an int between 0 and 32
	 * */
	private static long ip_range_max(long ip_dec, int mask){
		if(mask==32) return ip_dec;//from an off-by-one error
		if(mask==0) return (long) Math.pow(2,32)-1;//off-by-one-error
		long min=ip_range_min(ip_dec,mask);
		int rem = 32-mask;
		return min + ((1<<rem) -2);
	}
	
	/**converts decimal representation of an ip address to ip format.
	 * assumption: a proper ip address has been converted beforehand
	 * 
	 * written on the fly in like 5 minutes, appears to work,
	 * but don't trust this function too much...*/
	private static String dec_to_ip(long dec){
		//each letter represents an octet
		int a = (int) (dec>>24);//octet 1
		int b = (int) ((dec>>16)&255);//octet 2
		int c = (int) ((dec>>8)&255);//octet 3
		int d = (int) (dec&255);//octet 4
		return "" + a + "." + b + "." + c + "." + d;
	}
	
	/**helper function to input_to_doubles() func.
	 * converts ipv4 address to decimal format input: string of the form
	 * a.b.c.d, where each field is a number from 0 to 255. output: a double
	 * format number corresponding with the decimal representation of that ip
	 * address. could either return double or long
	 * 
	 * note: this function CANNOT return an int (must be long int), otherwise
	 * it will give an incorrect result, since an int doesn't have enough bits
	 * to properly store all possible numbers, since both ints and ipv4 addresses
	 * are 32 bits.
	 * 
	 * For future use with ipv6 addresses, the number returned must be an integer
	 * with MORE THAN 128 bits. (cannot be 128 bits exactly).  
	 * */
	private static long ip_to_dec(String ip) {
		long o1, o2, o3, o4;// octets
		String[] octets = ip.split(Pattern.quote("."));
		o1 = Long.parseLong(octets[0]);
		o2 = Long.parseLong(octets[1]);
		o3 = Long.parseLong(octets[2]);
		o4 = Long.parseLong(octets[3]);
		long num = (o1 * 256 * 256 * 256) + (o2 * 256 * 256) + (o3 * 256) + o4;
		return num;
	}
	/**
	 * source code for this method:
	 * http://stackoverflow.com/questions/4917326/how-to-iterate-over-the-files-of-a-certain-directory-in-java
	 * */
	private static void directory_loop() throws Exception{
		String path = "/Users/maxbass/Desktop/cs450/data/synth_filters_edited/";
		File dir = new File(path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String fn = child.getName();
				Instances data = readData(path+fn);
				ArffSaver saver = new ArffSaver();
				saver.setInstances(data);
				saver.setFile(new File("data_arffs/"+fn+".arff"));
				saver.writeBatch();
				//System.out.println(fn);
		    }
		}
	}
	
	
	// //////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws Exception {

		// String f1 = "synthetic_filters/acl1_seed_1000.filter";
		String path= "/Users/maxbass/Desktop/cs450/data/synth_filters_edited/";
		String f1="ipc2_seed_1000.filter";
		Instances data = readData(path+f1);
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File("data_arffs/"+f1+".arff"));
		saver.writeBatch();
		directory_loop();
	}

	
}
