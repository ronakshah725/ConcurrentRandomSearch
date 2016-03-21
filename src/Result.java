import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Result {
	String path;
	double elapsed;
	long byteCount;
	Status status;
	
	public Result(double elapsed2, long byte_cnt, Status status, String fname) {
		this.elapsed = elapsed2;
		this.byteCount = byte_cnt;
		this.status = status;
		this.path = fname;
	}

	@Override
	public String toString() {
		
		String fileName = path.substring(path.indexOf('/')+1);
		
		return fileName + "\t\t" + TimeUnit.NANOSECONDS.toSeconds((long) elapsed) + "\t\t" + byteCount + "\t\t" + status ;
	}
}


//to sort in descending order
class ElapsedComparator implements Comparator<Result> {

	@Override
	public int compare(Result r1, Result r2) {
		
		return r1.elapsed < r2.elapsed ? 1 : r1.elapsed == r2.elapsed ? 0 : -1;
	}
}

enum Status{
	
	SUCCESS, 
	TIMEOUT, 
	FAILURE
}
