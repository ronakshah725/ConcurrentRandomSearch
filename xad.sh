#!/bin/bash



usage="./$(basename "$0") [-h] [-r n] [-t n] [-v] \n
\n
OPTIONS:
\n\n
    -h  Display Help text\n
    -t  Timeout value USAGE:[-t n] where n is the timeout in seconds\n
    -r  Generate Random Files, place them in dir 'random' USAGE:[-r n] where n is the number of random files\n
    -v  Verify search with grep tool\n\n	

-java program to concurrently search pseudo-random stream of data for string 'xAd'\n
\n
-pseudorandom stream of data is obtained by 'base64'& head utility using the Linux/Unix's \n
   /dev/urandom file (stores environment noise from device drivers such as sound cards or mouse movement on Unix/Linux Sytems)\n
   You can generate n random files by -r option\n 
\n
-pseudorandom data is stored in a directory called 'files' \n
   The program spawns seperate threads which searches each individual file's streamed-in data to find string 'xAd' \n
   in a specified timeout given by option -t or in default timeout of 60 seconds\n
\n
-The main program fetches the results from each thread and generates a console report\n
\n\n"



files="random"

if [ "$1" == ""  ]; then

	java -jar xad.jar
	echo -e "\nDone."
	exit 0

elif [ "$1" == "-h" ]; then

	echo -e "Usage:" $usage
	exit 0

elif [ "$1" == "-t" ]; then
	
	echo -e "Running with timeout of $2 seconds\n"
	java -jar xad.jar $2
	echo -e "\nDone."
	exit 0

elif [[ "$1" == "-v" ]]; then

	echo "Verifying with command $ :grep -rl 'xAd' random "
	echo -e "\nFiles containing 'xAd':"
	grep -rl 'xAd' $files 
	echo "Done."

elif [ "$1"=="-r" ]; then
	
	echo "Generate $2 random files and place them in '$files' directory"

	if [[ -e $files ]]; then

		for ((  i=1; i<=$2; i++  ))
		do
	    	base64 /dev/urandom | head -c 200000000 > $files/random_file$i.txt
		done

		echo "Done."
	
	else
	
		echo "$files directory does not exist, please create it and run xad.sh -r n to create n random files"
		exit 0 
	
	fi

	

fi





