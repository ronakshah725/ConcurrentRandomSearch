Author: Ronak Shah
Email: 	ronakshah725@gmail.com

#Stream Search

The program is a java program to search a string 'xAd' in random or psedorandom stream of data in a given Timeout.
To search n random streams of data, n different threads are spawned, one for each stream.
This is done by using ExecutorService library of Java 8. The Threads are all Callable (class SearchCallable in the program), and
return a Future Object which holds the search Result. 
The main threads accumulates the Result objects and logs them, also calculates average time in seconds, to read a KiloByte of data.
See Enclosed Documentation folder for implementation details in the form of generated javadoc

#Algorithm:

- Make a Deterministic Finite Automata to search the tokens from the stream, 
  from initial state 'x' to Goal state 'd'
- If found in given timeout, stop searching and return the results
- Else if timeout occurred, set status as TIMEOUT and return result.
- If whole file is read or some error occurs while processing, return FAILURE status

#Steps to Run

1. Use tar to unarchive
	$tar -xvf xad.tar

2. Gotot the Final directory Make executable
	$chmod +x xad.sh

3. See Help : 
	 $./xad.sh -h

4. Place any random files in the sub-directory 'random'. 3 ways to use random files:
	[IMPORATANT: This is necessary for program to work correctly]
		
		a. Random files can be generated using option [-r n] 
			
			$./xad.sh -r 5 
			
			will generate 5 random files in random directory
		 	using random data from /dev/urandom 
		
		b. Random files used during testing can be downloaded from '' 
		
		c. You can put any file in random directory and program will process it

5. Run 
	b.With custom timeout:
	$./xad.sh -t 5

	a.With default timeout:
	$./xad.sh

	[Side Note: to see working of timeouts correctly, use a big timeout and then a small timeout]

6. Verify Correctness
	-v option to verify search using grep
	$./xad.sh -v



