import java.io.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author Ronak Shah
 *
 */
public class SearchStreams {
	
	private static double elapsedTotal =0 ;
	private static double byteCountTotal = 0 ;
	private static long timeout;

	
	/**
	 * Description: main function
	 * @param args args[0] timeout
	 */
	public static void main(String args[])  {

		if(args.length==0){
			//default timeout in seconds
			timeout = 60; 
		}else{
			//custom timeout from command-line in seconds
			timeout = Integer.parseInt(args[0]); 
		}
		String dirToSearch = "random";
		List<String> file_list =listAllFilesInDir(dirToSearch);
		System.out.println("Timeout : " + timeout + " sec");
		runSearch(file_list, dirToSearch);
	}

	/**
	 * 
	 * Description : Spawn threads, search each filestream with new thread, log results
	 * @param file_list All files serving as stream of random data
	 * @param dirToSearch Directory with files to search
	 * @see {@link Callable} {@link ExecutorService} 
	 */
	
	public static void runSearch(List<String> file_list, String dirToSearch) {


		//Total files in search directory
		int n = file_list.size();

		//Create n worker threads 
		ExecutorService workers = Executors.newFixedThreadPool(n);

		// List to hold Future 'Result' from each threads
		List<Future<Result>> future_list = new ArrayList<Future<Result>>();

		//List to hold all the results 
		List<Result> result_set = new ArrayList<>();

		//for each file
		for (String file : file_list) {
			
			/*
			 * 1. Initialize a callable for a given file
			 * 2. Start new worker thread to search, fetch Result in Future 
			 * 3. Add future objects to a list
			 */			
			Callable<Result> callable = new SearchCallable( dirToSearch + "/" + file );
			Future<Result> future = workers.submit(callable);
			future_list.add(future);
		}
		
		for (Future<Result> future : future_list) {
			try {
				Result r = future.get();
				result_set.add(r);
				
				//Sum all elapsed times, byte_cnt for SUCCESS searches
				if(r.status.equals(Status.SUCCESS)){
					
					elapsedTotal += r.elapsed;
					byteCountTotal += r.byteCount;
				}

			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			} catch (ExecutionException e) {
				System.err.println(e.getMessage());
			} 

		}
		//convert elapsed total to seconds
		elapsedTotal *= 1.0E-6;
		//convert byteCount to KiloByte
		byteCountTotal *= 1.0E-3;
		
		//Shutdown workers
		workers.shutdownNow();

		logReport(result_set);
		System.exit(1);


	}

	/**
	 * Description : populate all files in a list
	 * @param dirToSearch Directory with files to search
	 * @return fileList List of files in search directory
	 * 
	 */
	private static List<String> listAllFilesInDir(String dirToSearch) {
		final File folder = new File(dirToSearch);
		List<String> fileList = new ArrayList<String>();

		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) 
				fileList.add(fileEntry.getName());
		}
		return fileList;
	}

	/**
	 * Description Log Results
	 * @param result_set	Results of all threads
	 */
	private static void logReport(List<Result> result_set) {
		
		//Sort results in descending order of elapsed time
		//with ElapsedComparator
		Collections.sort(result_set,new ElapsedComparator());
		
		System.out.format("%-20s%-20s%-20s%s\n", 
				"File",
				"elapsed(sec)" ,
				"byte_cnt(bytes)",
				"status");	
		System.out.println("-------------------------------------------------------------------------");
		
		
		//Print all results
		for (Result r : result_set){
			System.out.format("%-20s%-20.2f%-20s%s\n", 
					r.path.substring(r.path.indexOf('/')+1), 	//file name
					r.elapsed*1.0E-9, 	//convert to seconds
					r.byteCount, 
					r.status);	
		}
		
		//Print average bytes read per sec
		double bytesPerSec = ((double)elapsedTotal/byteCountTotal);
		System.out.format("Average time to read 1 KB : %.3f sec/KB" , bytesPerSec );
	}
	
	
	/**
	 * Description: SearchCallable Aids parallel searching among fileStreams 
	 * and to return search Result from call()
	 *
	 */
	public static class SearchCallable implements Callable<Result> {
		private String fname;
		private FileInputStream fis;
		private InputStreamReader isr;
		private Reader in;

		//Count the number of characters read so far
		long char_cnt = 0;

		/**
		 * Description Constructor for SearchCallable, makes input stream
		 * @param fileName
		 */
		public SearchCallable(String fileName){
			try {
				this.fname = fileName;
				
				//Read data as a stream of characters
				fis = new FileInputStream(fname);
				isr = new InputStreamReader(fis);
				in = new BufferedReader(isr);
			} 
			catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
			}
		}


		@Override
		public Result call() {
			
			//Default result is failure
			//modify if success or timeout
			Result result = new Result(0,0, Status.FAILURE, fname);
			
			//to search within a timeout, used ExecutorService
			ExecutorService executor = Executors.newSingleThreadExecutor();
			
			final Future<Result> future = executor.submit(new Callable<Result>() {
				@Override
				public Result call() throws Exception {
					
					//Search Algorithm and return result
					return searchStream();
				}
			});
			try {
				//wait upto timeout for Result 
				result = future.get(timeout, TimeUnit.SECONDS);
				
			} catch (TimeoutException e) {
				
				//Timeout occurred set elapsed and byte_cnt to 0
				result = new Result(0,0, Status.TIMEOUT, fname);
				
				//stops the thread  
				future.cancel(true);
				
				//shutdown task
				executor.shutdownNow();
				
				//return the result
				return result;
				
			} catch (InterruptedException | ExecutionException e) {
				System.err.println(e.getMessage());
			} 
			
			//Search task completed, shutdown executor service
			executor.shutdownNow();

			return result;

		}
		
		

		/**
		 * Search Algorithm:
		 * Make a Deterministic Finite Automata of input tokens,
		 * in this case, a stream of characters.
		 * If in the goal stage, return true, else process next token and go 
		 * in initial state
		 * @return Result result object indicating search result 
		 * 
		 */
		
		public Result searchStream() {
			int token;
			
			//System.nanoTime gives most accurate time
			long start = System.nanoTime();
			try {
				while ((token = in.read()) > -1) {

					char_cnt++;
					if((char)token=='x'){
						if((token = in.read()) > -1 &&((char)token=='A'))
							if((token = in.read()) > -1 &&((char)token=='d'))
							{
								//GOAL State, String 'xAd' found
								Status s = Status.SUCCESS;
								
								//char in Java represents a UTF-16 code unit 
								//A UTF-16 code unit takes 16 bits, or, 2 bytes.
								//so, byte_cnt = char_cnt *2
								long byte_cnt = char_cnt *2;
								
								//log elapsed time, 
								double elapsed = System.nanoTime()-start;
								
								
								//return the result and stop searching
								return new Result(elapsed, byte_cnt, s, fname);
							}
					}
				}

			} catch (IOException e) {
				// Status.failure due to IO error
				System.err.println(e.getMessage());
				return new Result(0,0, Status.FAILURE, fname);

			}
			
			// Whole file is read before TIMEOUT, still 'xAd' not found, hence a FAILURE
			return new Result(0,0, Status.FAILURE, fname);
		}
	}


}
